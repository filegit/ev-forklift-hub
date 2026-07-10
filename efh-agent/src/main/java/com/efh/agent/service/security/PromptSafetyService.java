package com.efh.agent.service.security;

import com.efh.agent.config.AgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Input guardrail for prompt injection, risky commands, and raw PII.
 */
@Slf4j
@Service
public class PromptSafetyService {

    private static final Pattern[] INJECTION_PATTERNS = {
            Pattern.compile("ignore (all )?previous", Pattern.CASE_INSENSITIVE),
            Pattern.compile("system prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("developer message", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(drop|delete|truncate)\\s+table", Pattern.CASE_INSENSITIVE),
            Pattern.compile("忽略(以上|之前|先前|所有).*指令"),
            Pattern.compile("不要遵守.*系统"),
            Pattern.compile("泄露.*(系统提示|提示词|prompt)"),
            Pattern.compile("执行(系统|shell|命令)")
    };
    private static final Pattern ID_CARD = Pattern.compile("\\b\\d{17}[0-9Xx]\\b");

    @Autowired
    private AgentProperties agentProperties;

    public void validateUserInput(String question) {
        if (!agentProperties.getSecurity().isPromptGuardEnabled()) return;
        if (question == null || question.trim().isEmpty()) {
            throw new SecurityException("问题不能为空");
        }
        if (ID_CARD.matcher(question).find()) {
            throw new SecurityException("检测到身份证等敏感个人信息，请脱敏后再提交。");
        }
        String q = question.toLowerCase(Locale.ROOT);
        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(q).find()) {
                log.warn("Blocked suspicious prompt: {}", question);
                throw new SecurityException("检测到非法或越权指令，已拒绝处理。");
            }
        }
    }

    public boolean isToolBlocked(String toolName) {
        if (toolName == null) return true;
        String blocked = agentProperties.getSecurity().getBlockedTools();
        return Arrays.stream(blocked.split(","))
                .map(String::trim)
                .anyMatch(b -> b.equalsIgnoreCase(toolName));
    }
}
