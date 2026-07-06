package com.efh.agent.service.tool;

import com.efh.agent.service.monitor.AgentExecutionLogService;
import com.efh.agent.service.security.PromptSafetyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具执行器（问题3/8）：鉴权 + 越权拦截 + 链路日志
 */
@Slf4j
@Service
public class ToolExecutor {

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private PromptSafetyService promptSafetyService;

    @Autowired
    private AgentExecutionLogService executionLogService;

    public String execute(String toolName, ToolContext ctx, Map<String, Object> args) {
        long start = System.currentTimeMillis();
        if (promptSafetyService.isToolBlocked(toolName)) {
            executionLogService.logStep(ctx, "TOOL", "ToolAgent", toolName, args.toString(), "BLOCKED", System.currentTimeMillis() - start, false, "工具被禁止");
            throw new SecurityException("工具 " + toolName + " 已被安全策略禁止");
        }
        AgentTool tool = toolRegistry.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("未知工具: " + toolName);
        }
        if (tool.requiresAuth() && ctx.getUserId() == null) {
            throw new SecurityException("该工具需要登录");
        }
        if (tool.adminOnly() && !ctx.isAdmin()) {
            throw new SecurityException("该工具仅管理员可用");
        }
        try {
            Map<String, Object> safeArgs = args == null ? new HashMap<>() : args;
            String result = tool.execute(ctx, safeArgs);
            executionLogService.logStep(ctx, "TOOL", "ToolAgent", toolName, safeArgs.toString(), result, System.currentTimeMillis() - start, true, null);
            return result;
        } catch (Exception e) {
            executionLogService.logStep(ctx, "TOOL", "ToolAgent", toolName, String.valueOf(args), e.getMessage(), System.currentTimeMillis() - start, false, e.getMessage());
            throw new RuntimeException("工具执行失败: " + e.getMessage(), e);
        }
    }
}
