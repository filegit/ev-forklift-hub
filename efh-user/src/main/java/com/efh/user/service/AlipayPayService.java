package com.efh.user.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.efh.common.exception.BusinessException;
import com.efh.user.config.AlipayPayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AlipayPayService {

    @Autowired
    private AlipayPayProperties properties;

    public String createPagePayForm(String payNo, BigDecimal amount, String subject) {
        ensureConfigured();
        try {
            AlipayClient client = buildClient();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(properties.getNotifyUrl());
            request.setReturnUrl(properties.getReturnUrl() + "?payNo=" + payNo);
            request.setBizContent(String.format(
                    "{\"out_trade_no\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\","
                            + "\"total_amount\":\"%s\",\"subject\":\"%s\"}",
                    payNo,
                    amount.toPlainString(),
                    subject
            ));
            return client.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new BusinessException("创建支付宝支付失败: " + e.getMessage());
        }
    }

    public boolean verifyNotify(Map<String, String> params) {
        ensureConfigured();
        try {
            return AlipaySignature.rsaCheckV1(params, properties.getAlipayPublicKey(), "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            return false;
        }
    }

    public String getTradeStatus(Map<String, String> params) {
        return params.get("trade_status");
    }

    public String getTradeNo(Map<String, String> params) {
        return params.get("trade_no");
    }

    public String getOutTradeNo(Map<String, String> params) {
        return params.get("out_trade_no");
    }

    private AlipayClient buildClient() {
        return new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getPrivateKey(),
                "json",
                "UTF-8",
                properties.getAlipayPublicKey(),
                "RSA2"
        );
    }

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new BusinessException("支付宝未配置，请设置 ALIPAY_APP_ID / ALIPAY_PRIVATE_KEY / ALIPAY_PUBLIC_KEY");
        }
    }
}
