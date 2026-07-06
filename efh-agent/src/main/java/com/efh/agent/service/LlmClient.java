package com.efh.agent.service;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.RagChunk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class LlmClient {

    @Autowired
    private AgentProperties agentProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(agentProperties.getLlm().getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    public ChatResult generateAnswer(String question, List<RagChunk> chunks) {
        if (!agentProperties.isLlmReady()) {
            return ChatResult.fallback(buildFallbackAnswer(question, chunks));
        }
        try {
            String prompt = buildPrompt(question, chunks);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", agentProperties.getLlm().getModel());
            body.put("temperature", 0.3);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(message("system", "你是新能源叉车领域的技术助手。请仅根据提供的参考资料回答，不要编造。若资料不足请明确说明。回答使用中文，条理清晰，并在末尾列出参考来源编号。"));
            messages.add(message("user", prompt));
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(agentProperties.getLlm().getApiKey());
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String url = buildChatCompletionsUrl(agentProperties.getLlm().getBaseUrl());
            log.info("调用 LLM: url={}, model={}", url, agentProperties.getLlm().getModel());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("LLM 调用失败: status={}, body={}", response.getStatusCode(), response.getBody());
                return ChatResult.fallback(buildFallbackAnswer(question, chunks));
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().trim().isEmpty()) {
                log.warn("LLM 返回空内容: {}", response.getBody());
                return ChatResult.fallback(buildFallbackAnswer(question, chunks));
            }
            return ChatResult.llm(content.asText().trim());
        } catch (Exception e) {
            log.warn("LLM 调用异常: {}", e.getMessage(), e);
            return ChatResult.fallback(buildFallbackAnswer(question, chunks));
        }
    }

    public static class ChatResult {
        private final String answer;
        private final boolean llmUsed;

        private ChatResult(String answer, boolean llmUsed) {
            this.answer = answer;
            this.llmUsed = llmUsed;
        }

        static ChatResult llm(String answer) {
            return new ChatResult(answer, true);
        }

        static ChatResult fallback(String answer) {
            return new ChatResult(answer, false);
        }

        public String getAnswer() {
            return answer;
        }

        public boolean isLlmUsed() {
            return llmUsed;
        }
    }

    /** 兼容 OpenAI 与阿里云百炼 compatible-mode（base_url 已含 /v1） */
    static String buildChatCompletionsUrl(String baseUrl) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.endsWith("/v1")) {
            return base + "/chat/completions";
        }
        if (base.contains("compatible-mode")) {
            return base + "/chat/completions";
        }
        return base + "/v1/chat/completions";
    }

    private Map<String, String> message(String role, String content) {
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
            if (!chunk.isUnlocked()) {
                sb.append("（未解锁，仅摘要）");
            }
            sb.append("\n").append(chunk.getContent()).append("\n\n");
        }
        sb.append("请基于以上资料回答用户问题。");
        return sb.toString();
    }

    private String buildFallbackAnswer(String question, List<RagChunk> chunks) {
        if (chunks.isEmpty()) {
            return "未在知识库和社区中找到与「" + question + "」相关的资料。\n\n" +
                    "建议：\n1. 换关键词重试\n2. 到社区发帖提问\n3. 在知识库搜索或解锁相关文档";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("根据平台资料，为您找到以下相关内容（当前未配置 LLM，以下为检索摘要）：\n\n");
        int i = 1;
        for (RagChunk chunk : chunks) {
            sb.append(i++).append(". ");
            if ("knowledge".equals(chunk.getType())) {
                sb.append("【知识库】");
            } else if ("post".equals(chunk.getType())) {
                sb.append("【社区帖】");
            } else {
                sb.append("【社区评论】");
            }
            sb.append(chunk.getTitle()).append("\n");
            sb.append(chunk.getContent()).append("\n");
            if ("knowledge".equals(chunk.getType()) && !chunk.isUnlocked()) {
                sb.append("（该文档需解锁后查看完整内容）\n");
            }
            sb.append("\n");
        }
        sb.append("提示：请检查 LLM 配置（API Key、接口地址、模型名）后重启 efh-agent。");
        return sb.toString();
    }
}
