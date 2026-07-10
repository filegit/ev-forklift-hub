package com.efh.agent.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatResponseVO {
    private String answer;
    private boolean llmUsed;
    private List<ChatSourceVO> sources = new ArrayList<>();

    private String intent;
    private double intentConfidence;
    private List<String> plan = new ArrayList<>();
    private List<String> stageEvents = new ArrayList<>();
    private List<String> toolCalls = new ArrayList<>();

    /** Conversation id for multi-turn memory. */
    private String sessionId;
    /** Trace id matched with agent_execution_log. */
    private String traceId;

    private int promptTokens;
    private int completionTokens;
    private double costYuan;
}
