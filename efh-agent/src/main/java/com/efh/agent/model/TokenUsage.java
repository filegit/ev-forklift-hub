package com.efh.agent.model;

import lombok.Data;

/** LLM Token 用量与成本（问题11） */
@Data
public class TokenUsage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private double costYuan;

    public static TokenUsage empty() {
        return new TokenUsage();
    }

    public void add(TokenUsage other) {
        if (other == null) return;
        this.promptTokens += other.promptTokens;
        this.completionTokens += other.completionTokens;
        this.totalTokens += other.totalTokens;
        this.costYuan += other.costYuan;
    }
}
