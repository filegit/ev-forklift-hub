package com.efh.user.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.efh.common.exception.BusinessException;
import com.efh.user.config.SmsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 阿里云号码认证服务 - 短信认证（个人开发者免企业资质）
 * 文档: https://help.aliyun.com/zh/pnvs/use-cases/sms-verify-for-individual-developers
 */
@Slf4j
@Component
public class AliyunPnvsSmsSender {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SmsProperties smsProperties;

    public AliyunPnvsSmsSender(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    /** 发送验证码（验证码由阿里云生成并下发） */
    public void sendVerificationCode(String phone) {
        validateConfig();

        try {
            CommonRequest request = buildRequest("SendSmsVerifyCode");
            request.putQueryParameter("PhoneNumber", phone);
            request.putQueryParameter("SignName", smsProperties.getSignName());
            request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());
            request.putQueryParameter("TemplateParam", buildTemplateParam());

            CommonResponse response = getClient().getCommonResponse(request);
            JsonNode body = MAPPER.readTree(response.getData());
            String code = body.path("Code").asText("");
            String message = body.path("Message").asText("");

            if (!"OK".equalsIgnoreCase(code)) {
                log.error("短信认证发送失败: phone={}, code={}, message={}",
                        maskPhone(phone), code, message);
                throw new BusinessException("短信发送失败：" + message);
            }

            log.info("短信认证发送成功: phone={}", maskPhone(phone));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("短信认证发送异常: phone={}", maskPhone(phone), e);
            throw new BusinessException("短信发送失败，请稍后重试");
        }
    }

    /** 核验验证码（与 SendSmsVerifyCode 配套，TemplateParam 须含 ##code##） */
    public void checkVerificationCode(String phone, String verifyCode) {
        validateConfig();

        try {
            CommonRequest request = buildRequest("CheckSmsVerifyCode");
            request.putQueryParameter("PhoneNumber", phone);
            request.putQueryParameter("VerifyCode", verifyCode.trim());

            CommonResponse response = getClient().getCommonResponse(request);
            JsonNode body = MAPPER.readTree(response.getData());
            String code = body.path("Code").asText("");
            String message = body.path("Message").asText("");
            String verifyResult = body.path("Model").path("VerifyResult").asText("");

            if (!"OK".equalsIgnoreCase(code)) {
                log.warn("短信认证核验接口失败: phone={}, code={}, message={}",
                        maskPhone(phone), code, message);
                throw new BusinessException("验证码校验失败，请重新获取");
            }
            if (!"PASS".equalsIgnoreCase(verifyResult)) {
                throw new BusinessException("短信验证码错误或已过期");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("短信认证核验异常: phone={}", maskPhone(phone), e);
            throw new BusinessException("验证码校验失败，请稍后重试");
        }
    }

    private IAcsClient getClient() {
        DefaultProfile profile = DefaultProfile.getProfile(
                smsProperties.getRegionId(),
                smsProperties.getAccessKeyId(),
                smsProperties.getAccessKeySecret());
        return new DefaultAcsClient(profile);
    }

    private CommonRequest buildRequest(String action) {
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dypnsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction(action);
        return request;
    }

    private String buildTemplateParam() throws Exception {
        int minutes = Math.max(1, smsProperties.getCodeExpireSeconds() / 60);
        return MAPPER.writeValueAsString(new TemplateParam("##code##", String.valueOf(minutes)));
    }

    private void validateConfig() {
        if (!StringUtils.hasText(smsProperties.getAccessKeyId())
                || !StringUtils.hasText(smsProperties.getAccessKeySecret())) {
            throw new BusinessException("短信服务未配置 AccessKey，请在服务器设置 SMS_ACCESS_KEY_ID / SMS_ACCESS_KEY_SECRET");
        }
        if (!StringUtils.hasText(smsProperties.getSignName())) {
            throw new BusinessException("短信服务未配置签名，请在控制台复制赠送签名并设置 SMS_SIGN_NAME");
        }
        if (!StringUtils.hasText(smsProperties.getTemplateCode())) {
            throw new BusinessException("短信服务未配置模板，请在控制台复制赠送模板 CODE 并设置 SMS_TEMPLATE_CODE");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static class TemplateParam {
        private final String code;
        private final String min;

        TemplateParam(String code, String min) {
            this.code = code;
            this.min = min;
        }

        public String getCode() {
            return code;
        }

        public String getMin() {
            return min;
        }
    }
}
