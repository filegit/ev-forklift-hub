package com.efh.user.service;

import com.efh.user.entity.User;

public interface SmsService {

    /**
     * 发送登录短信验证码，返回演示模式下生成的验证码（生产环境返回 null）
     */
    String sendLoginCode(User user);

    /**
     * 校验登录验证码，成功后删除
     */
    void verifyLoginCode(String username, String code);
}
