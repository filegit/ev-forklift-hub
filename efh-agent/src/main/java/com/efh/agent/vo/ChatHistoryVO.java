package com.efh.agent.vo;

import com.efh.agent.model.ChatMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatHistoryVO {
    private String sessionId;
    private String summary;
    private List<ChatMessage> messages = new ArrayList<>();
}
