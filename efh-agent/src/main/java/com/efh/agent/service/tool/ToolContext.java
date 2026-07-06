package com.efh.agent.service.tool;

import lombok.Data;

@Data
public class ToolContext {
    private Long userId;
    private String sessionId;
    private String traceId;
    private String scope;
    private boolean admin;
}
