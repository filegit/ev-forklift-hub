package com.efh.agent.service.tool;

import com.efh.agent.service.monitor.AgentExecutionLogService;
import com.efh.agent.service.security.PromptSafetyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Tool executor with auth checks, timeout, retry, and trace logging.
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
    @Autowired
    @Qualifier("agentTaskExecutor")
    private Executor agentTaskExecutor;

    public String execute(String toolName, ToolContext ctx, Map<String, Object> args) {
        long start = System.currentTimeMillis();
        if (promptSafetyService.isToolBlocked(toolName)) {
            executionLogService.logStep(ctx, "TOOL", "ToolAgent", toolName, String.valueOf(args), "BLOCKED", System.currentTimeMillis() - start, false, "工具被安全策略禁用");
            throw new SecurityException("工具 " + toolName + " 已被安全策略禁用");
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
            String result = executeWithTimeoutAndRetry(tool, ctx, safeArgs);
            executionLogService.logStep(ctx, "TOOL", "ToolAgent", toolName, safeArgs.toString(), result, System.currentTimeMillis() - start, true, null);
            return result;
        } catch (Exception e) {
            executionLogService.logStep(ctx, "TOOL", "ToolAgent", toolName, String.valueOf(args), e.getMessage(), System.currentTimeMillis() - start, false, e.getMessage());
            throw new RuntimeException("工具执行失败: " + e.getMessage(), e);
        }
    }

    private String executeWithTimeoutAndRetry(AgentTool tool, ToolContext ctx, Map<String, Object> args) throws Exception {
        Exception last = null;
        for (int i = 0; i < 2; i++) {
            try {
                return callWithTimeout(() -> tool.execute(ctx, args), 5000);
            } catch (Exception e) {
                last = e;
                log.debug("Tool retry {} failed: {}", i + 1, e.getMessage());
            }
        }
        throw last;
    }

    private String callWithTimeout(Callable<String> callable, long timeoutMs) throws Exception {
        FutureTask<String> task = new FutureTask<>(callable);
        agentTaskExecutor.execute(task);
        try {
            return task.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            task.cancel(true);
            throw e;
        }
    }
}
