package com.efh.parts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "parts.payment.alipay")
public class AlipayPayProperties {

    /** 是否启用支付宝 */
    private boolean enabled = false;

    /** 应用 AppId */
    private String appId;

    /** 应用私钥（PKCS8） */
    private String privateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 异步通知地址（需公网可达） */
    private String notifyUrl = "http://localhost/parts/api/parts/pay/alipay/notify";

    /** 同步跳转地址（前端支付结果页） */
    private String returnUrl = "http://localhost/pay/result";

    /** 是否沙箱环境 */
    private boolean sandbox = true;

    public String getGatewayUrl() {
        return sandbox
                ? "https://openapi-sandbox.dl.alipaydev.com/gateway.do"
                : "https://openapi.alipay.com/gateway.do";
    }

    public boolean isConfigured() {
        return enabled
                && appId != null && !appId.isEmpty()
                && privateKey != null && !privateKey.isEmpty()
                && alipayPublicKey != null && !alipayPublicKey.isEmpty();
    }
}
