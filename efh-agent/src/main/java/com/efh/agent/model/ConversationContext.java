package com.efh.agent.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 会话上下文：短期消息 + 长期摘要（问题1/5/12） */
@Data
public class ConversationContext {
    private Long userId;
    private String sessionId;
    private String summary;
    private List<ChatMessage> messages = new ArrayList<>();
}
