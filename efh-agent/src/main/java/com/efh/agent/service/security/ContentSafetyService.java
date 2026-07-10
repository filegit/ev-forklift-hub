package com.efh.agent.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Output guardrail: block unsafe content and mask sensitive information.
 */
@Slf4j
@Service
public class ContentSafetyService {

    private static final Set<String> BLOCKED_OUTPUT = new HashSet<>(Arrays.asList(
            "绕过安全", "爆炸物", "非法改装", "删除数据库", "root密码"
    ));
    private static final Pattern PHONE = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");
    private static final Pattern ID_CARD = Pattern.compile("(\\d{6})\\d{8}(\\d{3}[0-9Xx])");

    public String sanitizeOutput(String answer) {
        if (answer == null) return "";
        String lower = answer.toLowerCase(Locale.ROOT);
        for (String bad : BLOCKED_OUTPUT) {
            if (lower.contains(bad.toLowerCase(Locale.ROOT))) {
                log.warn("Blocked unsafe output keyword: {}", bad);
                return "抱歉，该问题涉及不安全或违规内容，无法提供回答。请咨询正规维修渠道。";
            }
        }
        return maskSensitive(answer);
    }

    public String maskSensitive(String text) {
        if (text == null) return "";
        String masked = PHONE.matcher(text).replaceAll("$1****$2");
        masked = ID_CARD.matcher(masked).replaceAll("$1********$2");
        return masked;
    }
}
