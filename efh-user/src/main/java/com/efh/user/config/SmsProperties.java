package com.efh.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "efh.sms")
public class SmsProperties {

    /** 演示模式：不真实发短信，验证码写入 Redis 并在接口响应中返回（便于测试） */
    private boolean mock = true;

    /** 验证码有效期（秒） */
    private int codeExpireSeconds = 300;

    /** 同一用户发送间隔（秒） */
    private int sendIntervalSeconds = 60;

    /** 阿里云短信（mock=false 时使用，需在环境变量或配置中填写） */
    private String accessKeyId = "";
    private String accessKeySecret = "";
    private String signName = "叉车社区";
    private String templateCode = "SMS_000000";
}
