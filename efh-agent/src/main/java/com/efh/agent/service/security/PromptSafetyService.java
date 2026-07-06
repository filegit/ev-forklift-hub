package com.efh.agent.service.security;

import com.efh.agent.config.AgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Prompt 安全与非法指令拦截（问题8）
 */
@Slf4j
@Service
public class PromptSafetyService {

    private static final Pattern[] INJECTION_PATTERNS = {
            Pattern.compile("ignore (all )?previous", Pattern.CASE_INSENSITIVE),
            Pattern.compile("忽略(以上|之前|先前)"),
            Pattern.compile("system prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("你现在是(?!叉车)"),
            Pattern.compile("(drop|delete|truncate)\\s+table", Pattern.CASE_INSENSITIVE),
            Pattern.compile("执行(系统|shell|命令)"),
    };

    @Autowired
    private AgentProperties agentProperties;

    public void validateUserInput(String question) {
        if (!agentProperties.getSecurity().isPromptGuardEnabled()) return;
        if (question == null || question.trim().isEmpty()) {
            throw new SecurityException("问题不能为空");
        }
        String q = question.toLowerCase(Locale.ROOT);
        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(q).find()) {
                log.warn("拦截可疑 Prompt: {}", question);
                throw new SecurityException("检测到非法或越权指令，已拒绝处理");
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
