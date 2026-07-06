package com.efh.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "efh.sms")
public class SmsProperties {

    /** 演示模式：不真实发短信，验证码写入 Redis 并在接口响应中返回（便于测试） */
    private boolean mock = false;

    /** 验证码有效期（秒） */
    private int codeExpireSeconds = 300;

    /** 同一用户发送间隔（秒） */
    private int sendIntervalSeconds = 60;

    /** 阿里云短信区域 */
    private String regionId = "cn-hangzhou";

    /** 阿里云 AccessKey（号码认证控制台创建，推荐 RAM 子账号） */
    private String accessKeyId = "";

    /** 阿里云 AccessKey Secret */
    private String accessKeySecret = "";

    /** 控制台「赠送签名配置」中选择的签名名称 */
    private String signName = "";

    /** 控制台「赠送模板配置」中选择的模板 CODE（须含 ${code} 变量） */
    private String templateCode = "";
}
