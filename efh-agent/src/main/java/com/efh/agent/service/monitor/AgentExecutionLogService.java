package com.efh.agent.service.monitor;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.TokenUsage;
import com.efh.agent.service.tool.ToolContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent 执行链路日志（问题10）：每步写入 agent_execution_log
 */
@Slf4j
@Service
public class AgentExecutionLogService {

    @Autowired
    @Qualifier("agentJdbcTemplate")
    private JdbcTemplate agentJdbc;

    @Autowired
    private AgentProperties agentProperties;

    private int stepCounter = 0;

    public void resetSteps() { stepCounter = 0; }

    public void logStep(ToolContext ctx, String stepType, String agentName, String toolName,
                        String input, String output, long durationMs, boolean success, String error) {
        if (!agentProperties.getMonitor().isLogEnabled()) return;
        try {
            agentJdbc.update(
                    "INSERT INTO agent_execution_log (trace_id,user_id,session_id,step_order,agent_name,tool_name,step_type,input_text,output_text,duration_ms,success,error_msg) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    ctx.getTraceId(), ctx.getUserId(), ctx.getSessionId(), ++stepCounter,
                    agentName, toolName, stepType,
                    truncate(input, 2000), truncate(output, 2000),
                    durationMs, success ? 1 : 0, error);
        } catch (Exception e) {
            log.debug("执行日志写入跳过(DB未就绪): {}", e.getMessage());
        }
    }

    public void logTokenUsage(ToolContext ctx, TokenUsage usage) {
        if (!agentProperties.getMonitor().isLogEnabled() || usage == null) return;
        try {
            agentJdbc.update(
                    "INSERT INTO agent_execution_log (trace_id,user_id,session_id,step_order,agent_name,step_type,prompt_tokens,completion_tokens,cost_yuan,success) VALUES (?,?,?,?,?,?,?,?,?,1)",
                    ctx.getTraceId(), ctx.getUserId(), ctx.getSessionId(), ++stepCounter,
                    "LlmAgent", "TOKEN", usage.getPromptTokens(), usage.getCompletionTokens(), usage.getCostYuan());
        } catch (Exception e) {
            log.debug("Token 日志跳过: {}", e.getMessage());
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
