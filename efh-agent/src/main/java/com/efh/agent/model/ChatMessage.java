package com.efh.agent.model;

import lombok.Data;

import java.io.Serializable;

/** 单条对话消息，用于短期/长期记忆 */
@Data
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String role;
    private String content;
    private int tokenCount;
    private long timestamp;

    public static ChatMessage of(String role, String content, int tokens) {
        ChatMessage m = new ChatMessage();
        m.setRole(role);
        m.setContent(content);
        m.setTokenCount(tokens);
        m.setTimestamp(System.currentTimeMillis());
        return m;
    }
}
