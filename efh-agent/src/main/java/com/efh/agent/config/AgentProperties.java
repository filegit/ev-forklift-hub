package com.efh.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "efh.agent")
public class AgentProperties {
    private String knowledgeUploadDir = "./uploads/knowledge";
    private int maxChunks = 6;
    private int maxChunkChars = 900;
    private int adminUserType = 9;
    private Llm llm = new Llm();

    @Data
    public static class Llm {
        private boolean enabled = false;
        private String apiKey = "";
        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
        private int timeoutMs = 60000;
    }

    public boolean isLlmReady() {
        return llm.isEnabled() && llm.getApiKey() != null && !llm.getApiKey().trim().isEmpty();
    }
}
