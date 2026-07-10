package com.efh.agent.service;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.ChatMessage;
import com.efh.agent.model.RagChunk;
import com.efh.agent.model.TokenUsage;
import com.efh.agent.service.rag.HallucinationGuardService;
import com.efh.agent.service.tool.ToolRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible LLM client with sync generation, SSE streaming, fallback,
 * tool schema exposure, and token cost calculation.
 */
@Slf4j
@Service
public class LlmClient {

    @Autowired
    private AgentProperties agentProperties;
    @Autowired
    private HallucinationGuardService hallucinationGuardService;
    @Autowired
    private ToolRegistry toolRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(agentProperties.getLlm().getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    public ChatResult generateAnswer(String question, List<RagChunk> chunks, List<ChatMessage> history) {
        if (!agentProperties.isLlmReady()) {
            return ChatResult.fallback(buildFallbackAnswer(question, chunks));
        }
        try {
            List<Map<String, String>> messages = buildMessages(question, chunks, history);
            Map<String, Object> body = buildBody(messages, false);
            ResponseEntity<String> response = postChat(body);
            return parseChatResponse(response.getBody(), question, chunks);
        } catch (Exception e) {
            log.warn("LLM call failed: {}", e.getMessage());
            return ChatResult.fallback(buildFallbackAnswer(question, chunks));
        }
    }

    public String streamAnswer(String question, List<RagChunk> chunks, List<ChatMessage> history, SseEmitter emitter) {
        if (!agentProperties.isLlmReady()) {
            String fallback = buildFallbackAnswer(question, chunks);
            sendSse(emitter, fallback);
            return fallback;
        }
        try {
            List<Map<String, String>> messages = buildMessages(question, chunks, history);
            Map<String, Object> body = buildBody(messages, true);
            body.put("stream", true);

            HttpURLConnection conn = openStreamConnection(body);
            StringBuilder full = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) break;
                    JsonNode node = objectMapper.readTree(data);
                    String delta = node.path("choices").path(0).path("delta").path("content").asText("");
                    if (!delta.isEmpty()) {
                        full.append(delta);
                        emitter.send(SseEmitter.event().data(delta));
                    }
                }
            }
            return full.toString();
        } catch (Exception e) {
            log.warn("SSE stream failed: {}", e.getMessage());
            String fallback = buildFallbackAnswer(question, chunks);
            sendSse(emitter, fallback);
            return fallback;
        }
    }

    public String summarizeDialog(String oldDialog, String existingSummary) {
        if (!agentProperties.isLlmReady()) {
            String prefix = existingSummary == null ? "" : existingSummary + "\n";
            return prefix + oldDialog.substring(0, Math.min(500, oldDialog.length()));
        }
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(msg("system", "请将以下对话压缩为简洁中文摘要，保留关键事实、用户偏好、业务结论，不超过300字。"));
            if (existingSummary != null && !existingSummary.isEmpty()) {
                messages.add(msg("user", "已有摘要:\n" + existingSummary + "\n\n新对话:\n" + oldDialog));
            } else {
                messages.add(msg("user", oldDialog));
            }
            ResponseEntity<String> resp = postChat(buildBody(messages, false));
            return objectMapper.readTree(resp.getBody()).path("choices").path(0).path("message").path("content").asText("").trim();
        } catch (Exception e) {
            return oldDialog.substring(0, Math.min(400, oldDialog.length()));
        }
    }

    public TokenUsage calcUsage(JsonNode usageNode) {
        TokenUsage u = TokenUsage.empty();
        if (usageNode == null || usageNode.isMissingNode()) return u;
        u.setPromptTokens(usageNode.path("prompt_tokens").asInt(0));
        u.setCompletionTokens(usageNode.path("completion_tokens").asInt(0));
        u.setTotalTokens(usageNode.path("total_tokens").asInt(u.getPromptTokens() + u.getCompletionTokens()));
        double cost = u.getPromptTokens() / 1000.0 * agentProperties.getLlm().getPromptCostPer1k()
                + u.getCompletionTokens() / 1000.0 * agentProperties.getLlm().getCompletionCostPer1k();
        u.setCostYuan(cost);
        return u;
    }

    private List<Map<String, String>> buildMessages(String question, List<RagChunk> chunks, List<ChatMessage> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(msg("system", hallucinationGuardService.buildGroundedSystemPrompt()));
        if (history != null) {
            for (ChatMessage m : history) {
                if ("user".equals(m.getRole()) || "assistant".equals(m.getRole()) || "system".equals(m.getRole())) {
                    messages.add(msg(m.getRole(), m.getContent()));
                }
            }
        }
        messages.add(msg("user", buildPrompt(question, chunks)));
        return messages;
    }

    private Map<String, Object> buildBody(List<Map<String, String>> messages, boolean withTools) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", agentProperties.getLlm().getModel());
        body.put("temperature", 0.3);
        body.put("messages", messages);
        if (withTools && agentProperties.isLlmReady()) {
            body.put("tools", toolRegistry.toOpenAiTools());
            body.put("tool_choice", "auto");
        }
        return body;
    }

    private ResponseEntity<String> postChat(Map<String, Object> body) throws Exception {
        return restTemplate.exchange(buildChatCompletionsUrl(agentProperties.getLlm().getBaseUrl()),
                HttpMethod.POST, new HttpEntity<>(body, buildHeaders()), String.class);
    }

    private HttpURLConnection openStreamConnection(Map<String, Object> body) throws Exception {
        URL url = new URL(buildChatCompletionsUrl(agentProperties.getLlm().getBaseUrl()));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + agentProperties.getLlm().getApiKey());
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.getOutputStream().write(objectMapper.writeValueAsBytes(body));
        return conn;
    }

    private ChatResult parseChatResponse(String body, String question, List<RagChunk> chunks) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        TokenUsage usage = calcUsage(root.path("usage"));
        JsonNode message = root.path("choices").path(0).path("message");
        if (message.has("tool_calls")) {
            return ChatResult.llm("[工具调用模式] 模型已触发工具，当前系统会优先使用后端 Agent 编排结果。", usage);
        }
        String content = message.path("content").asText("").trim();
        if (content.isEmpty()) {
            return ChatResult.fallback(buildFallbackAnswer(question, chunks));
        }
        return ChatResult.llm(content, usage);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(agentProperties.getLlm().getApiKey());
        headers.set("User-Agent", "Mozilla/5.0 EV-Forklift-Hub-Agent/1.0");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private void sendSse(SseEmitter emitter, String text) {
        try {
            emitter.send(SseEmitter.event().data(text));
        } catch (Exception ignored) {
        }
    }

    static String buildChatCompletionsUrl(String baseUrl) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (base.endsWith("/v1") || base.contains("compatible-mode")) return base + "/chat/completions";
        return base + "/v1/chat/completions";
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private String buildPrompt(String question, List<RagChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户问题：").append(question).append("\n\n参考资料：\n");
        int i = 1;
        for (RagChunk chunk : chunks) {
            sb.append("[").append(i++).append("] ")
                    .append(chunk.getType()).append("《").append(chunk.getTitle()).append("》");
            if (!chunk.isUnlocked()) sb.append("（未解锁，仅摘要）");
            sb.append("\n").append(chunk.getContent()).append("\n\n");
        }
        sb.append("请基于以上资料回答，并标注引用编号。若资料不足，明确说明未找到可靠依据，不要编造。");
        return sb.toString();
    }

    String buildFallbackAnswer(String question, List<RagChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "未找到与“" + question + "”相关的资料，请换关键词，或到社区发布问题。";
        }
        StringBuilder sb = new StringBuilder("【检索摘要模式】\n\n");
        int i = 1;
        for (RagChunk chunk : chunks) {
            sb.append(i++).append(". ").append(chunk.getTitle()).append("\n")
                    .append(chunk.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    @Getter
    public static class ChatResult {
        private final String answer;
        private final boolean llmUsed;
        private final TokenUsage usage;

        private ChatResult(String answer, boolean llmUsed, TokenUsage usage) {
            this.answer = answer;
            this.llmUsed = llmUsed;
            this.usage = usage == null ? TokenUsage.empty() : usage;
        }

        public static ChatResult llm(String answer, TokenUsage usage) {
            return new ChatResult(answer, true, usage);
        }

        public static ChatResult fallback(String answer) {
            return new ChatResult(answer, false, TokenUsage.empty());
        }
    }
}
