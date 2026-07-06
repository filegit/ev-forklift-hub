package com.efh.common.utils;

import com.efh.common.exception.BusinessException;

/**
 * 从网关透传请求头解析当前登录用户
 */
public final class UserContextUtil {

    private UserContextUtil() {
    }

    public static Long requireUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            throw new BusinessException(401, "未授权");
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "未授权");
        }
    }
}
