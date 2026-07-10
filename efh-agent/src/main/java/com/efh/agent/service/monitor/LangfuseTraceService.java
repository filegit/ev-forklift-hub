package com.efh.agent.service.monitor;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.vo.ChatResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LangfuseTraceService {

    @Autowired
    private AgentProperties agentProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    public void trace(Long userId, String input, ChatResponseVO response, long latencyMs) {
        AgentProperties.Langfuse cfg = agentProperties.getLangfuse();
        if (!cfg.isEnabled() || cfg.getBaseUrl() == null || cfg.getBaseUrl().trim().isEmpty()) {
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("id", response.getTraceId());
            body.put("name", "ev-forklift-agent-chat");
            body.put("userId", userId == null ? "anonymous" : String.valueOf(userId));
            body.put("input", input);
            body.put("output", response.getAnswer());
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("intent", response.getIntent());
            metadata.put("latencyMs", latencyMs);
            metadata.put("promptTokens", response.getPromptTokens());
            metadata.put("completionTokens", response.getCompletionTokens());
            metadata.put("costYuan", response.getCostYuan());
            metadata.put("toolCalls", response.getToolCalls());
            body.put("metadata", metadata);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (cfg.getPublicKey() != null && !cfg.getPublicKey().isEmpty()) {
                String raw = cfg.getPublicKey() + ":" + cfg.getSecretKey();
                headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
            }
            String url = cfg.getBaseUrl().replaceAll("/$", "") + "/api/public/traces";
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.debug("Langfuse trace skipped: {}", e.getMessage());
        }
    }
}
