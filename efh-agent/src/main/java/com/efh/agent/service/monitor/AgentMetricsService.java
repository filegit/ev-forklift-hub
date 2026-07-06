package com.efh.agent.service.monitor;

import com.efh.agent.model.TokenUsage;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent 监控指标（问题11）：耗时、QPS、Token 成本（内存聚合，可对接 Prometheus）
 */
@Service
public class AgentMetricsService {

    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong totalErrors = new AtomicLong();
    private final AtomicLong totalLatencyMs = new AtomicLong();
    private final AtomicLong totalPromptTokens = new AtomicLong();
    private final AtomicLong totalCompletionTokens = new AtomicLong();
    private final AtomicLong totalCostMicroYuan = new AtomicLong();

    public void recordRequest(long latencyMs, boolean success, TokenUsage usage) {
        totalRequests.incrementAndGet();
        totalLatencyMs.addAndGet(latencyMs);
        if (!success) totalErrors.incrementAndGet();
        if (usage != null) {
            totalPromptTokens.addAndGet(usage.getPromptTokens());
            totalCompletionTokens.addAndGet(usage.getCompletionTokens());
            totalCostMicroYuan.addAndGet((long) (usage.getCostYuan() * 1_000_000));
        }
    }

    public AgentMetricsSnapshot snapshot() {
        long n = Math.max(1, totalRequests.get());
        AgentMetricsSnapshot s = new AgentMetricsSnapshot();
        s.setTotalRequests(totalRequests.get());
        s.setTotalErrors(totalErrors.get());
        s.setQps(totalRequests.get() / Math.max(1, (System.currentTimeMillis() / 1000 / 60))); // 近似
        s.setAvgLatencyMs(totalLatencyMs.get() / n);
        s.setTotalPromptTokens(totalPromptTokens.get());
        s.setTotalCompletionTokens(totalCompletionTokens.get());
        s.setTotalCostYuan(totalCostMicroYuan.get() / 1_000_000.0);
        return s;
    }

    @Data
    public static class AgentMetricsSnapshot {
        private long totalRequests;
        private long totalErrors;
        private long qps;
        private long avgLatencyMs;
        private long totalPromptTokens;
        private long totalCompletionTokens;
        private double totalCostYuan;
    }
}
