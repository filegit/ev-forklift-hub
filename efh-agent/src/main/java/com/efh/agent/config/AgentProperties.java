package com.efh.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent 全量配置项，对应 application.yml 中 efh.agent.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "efh.agent")
public class AgentProperties {

    private String knowledgeUploadDir = "./uploads/knowledge";
    private int maxChunks = 6;
    private int maxChunkChars = 900;
    private int adminUserType = 9;
    private boolean stageEventsEnabled = true;

    /** 短期记忆：Redis 中保留的最近消息轮数 */
    private int shortTermMaxMessages = 20;
    /** 触发摘要压缩的 token 阈值（近似） */
    private int contextTokenLimit = 3000;
    /** 分布式会话锁等待秒数 */
    private int sessionLockSeconds = 5;

    private Llm llm = new Llm();
    private Rag rag = new Rag();
    private Security security = new Security();
    private Monitor monitor = new Monitor();
    private Vector vector = new Vector();
    private Langfuse langfuse = new Langfuse();
    private Multimodal multimodal = new Multimodal();
    private Cache cache = new Cache();
    private Datasource datasource = new Datasource();

    @Data
    public static class Llm {
        private boolean enabled = false;
        private String apiKey = "";
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String model = "qwen-plus";
        private String embeddingModel = "text-embedding-v3";
        private int timeoutMs = 120000;
        /** 每 1K prompt token 成本（元），用于成本统计 */
        private double promptCostPer1k = 0.002;
        private double completionCostPer1k = 0.006;
    }

    @Data
    public static class Rag {
        /** 相似度阈值：低于此分数的检索结果被过滤（问题13） */
        private double minSimilarityThreshold = 0.25;
        /** 重排后保留的最大 chunk 数 */
        private int rerankTopK = 6;
        /** 关键词与向量分数权重 */
        private double keywordWeight = 0.4;
        private double vectorWeight = 0.6;
        /** 幻觉检测：回答与资料重叠率低于此值则追加免责声明（问题14） */
        private double minGroundingOverlap = 0.08;
    }

    @Data
    public static class Security {
        /** 是否启用 Prompt 注入检测（问题8） */
        private boolean promptGuardEnabled = true;
        /** 单用户每分钟最大请求数（问题2 并发限流） */
        private int maxRequestsPerMinute = 30;
        /** 禁止调用的危险工具名 */
        private String blockedTools = "shell_exec,sql_write,delete_data";
    }

    @Data
    public static class Monitor {
        private boolean logEnabled = true;
        private boolean metricsEnabled = true;
    }

    @Data
    public static class Langfuse {
        private boolean enabled = false;
        private String baseUrl = "";
        private String publicKey = "";
        private String secretKey = "";
    }

    @Data
    public static class Multimodal {
        private boolean enabled = false;
        private String visionModel = "qwen-vl-plus";
        private int maxImages = 3;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private int ttlSeconds = 300;
    }

    @Data
    public static class Vector {
        private boolean enabled = true;
        /** 向量维度（本地 fallback 哈希向量） */
        private int dimension = 128;
        private int indexBatchSize = 50;
    }

    @Data
    public static class Datasource {
        private Db knowledge = new Db();
        private Db community = new Db();
        private Db agent = new Db();
    }

    @Data
    public static class Db {
        private String jdbcUrl;
        private String username = "root";
        private String password = "123456";
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
    }

    public boolean isLlmReady() {
        return llm.isEnabled() && llm.getApiKey() != null && !llm.getApiKey().trim().isEmpty();
    }
}
