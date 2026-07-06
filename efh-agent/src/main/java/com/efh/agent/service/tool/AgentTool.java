package com.efh.agent.service.tool;

import java.util.Map;

/** Agent 工具接口（问题3 Function Call） */
public interface AgentTool {
    String name();
    String description();
    /** 是否必须登录 */
    boolean requiresAuth();
    /** 是否仅管理员 */
    boolean adminOnly();
    String execute(ToolContext ctx, Map<String, Object> args) throws Exception;
}
