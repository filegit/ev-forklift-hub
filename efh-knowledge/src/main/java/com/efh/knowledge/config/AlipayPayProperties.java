package com.efh.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "knowledge.payment.alipay")
public class AlipayPayProperties {

    private boolean enabled = false;
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private boolean sandbox = true;

    public String getGatewayUrl() {
        return sandbox
                ? "https://openapi-sandbox.dl.alipaydev.com/gateway.do"
                : "https://openapi.alipay.com/gateway.do";
    }

    public boolean isConfigured() {
        return enabled && appId != null && !appId.isEmpty()
                && privateKey != null && !privateKey.isEmpty()
                && alipayPublicKey != null && !alipayPublicKey.isEmpty();
    }
}
