package com.efh.agent.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatResponseVO {
    private String answer;
    private boolean llmUsed;
    private List<ChatSourceVO> sources = new ArrayList<>();
    /** 会话ID，客户端下次请求带回 */
    private String sessionId;
    /** 链路追踪ID，对应 agent_execution_log（问题10） */
    private String traceId;
    private int promptTokens;
    private int completionTokens;
    private double costYuan;
}
