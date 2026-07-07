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
 * 阿里云号码认证 - 短信认证 API
 * 发送: SendSmsVerifyCode  https://help.aliyun.com/document_detail/2573695.html
 * 核验: CheckSmsVerifyCode  https://help.aliyun.com/document_detail/2573696.html
 */
@Slf4j
@Component
public class AliyunPnvsSmsSender {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SmsProperties smsProperties;

    public AliyunPnvsSmsSender(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    /**
     * 发送短信验证码。
     * TemplateParam 使用 {"code":"##code##"} 时须同时传 CodeType，验证码由阿里云生成，后续可用 CheckSmsVerifyCode 核验。
     */
    public void sendVerificationCode(String phone) {
        validateConfig();

        try {
            CommonRequest request = buildRequest("SendSmsVerifyCode");
            putCommonPhoneParams(request, phone);
            request.putQueryParameter("SignName", smsProperties.getSignName());
            request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());
            // 模板仅含 ${code} 时只传 code；含 ${min} 时可传 {"code":"##code##","min":"5"}
            request.putQueryParameter("TemplateParam", smsProperties.getTemplateParam());
            request.putQueryParameter("CodeType", String.valueOf(smsProperties.getCodeType()));
            request.putQueryParameter("CodeLength", String.valueOf(smsProperties.getCodeLength()));
            // ValidTime 单位：秒（文档默认 300）
            request.putQueryParameter("ValidTime", String.valueOf(smsProperties.getCodeExpireSeconds()));
            request.putQueryParameter("Interval", String.valueOf(smsProperties.getSendIntervalSeconds()));

            log.info("SendSmsVerifyCode sign={}, template={}, phone={}",
                    smsProperties.getSignName(), smsProperties.getTemplateCode(), maskPhone(phone));

            CommonResponse response = getClient().getCommonResponse(request);
            JsonNode body = MAPPER.readTree(response.getData());
            if (!isOk(body)) {
                throw mapApiError(body, phone);
            }

            log.info("SendSmsVerifyCode 成功: phone={}", maskPhone(phone));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("SendSmsVerifyCode 异常: phone={}", maskPhone(phone), e);
            throw new BusinessException("短信发送失败，请稍后重试");
        }
    }

    /**
     * 核验短信验证码，Model.VerifyResult=PASS 表示成功。
     */
    public void checkVerificationCode(String phone, String verifyCode) {
        validateConfig();

        try {
            CommonRequest request = buildRequest("CheckSmsVerifyCode");
            putCommonPhoneParams(request, phone);
            request.putQueryParameter("VerifyCode", verifyCode.trim());
            request.putQueryParameter("CaseAuthPolicy", "1");

            CommonResponse response = getClient().getCommonResponse(request);
            JsonNode body = MAPPER.readTree(response.getData());
            if (!isOk(body)) {
                throw mapApiError(body, phone);
            }

            String verifyResult = body.path("Model").path("VerifyResult").asText("");
            if (!"PASS".equalsIgnoreCase(verifyResult)) {
                throw new BusinessException("短信验证码错误或已过期");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("CheckSmsVerifyCode 异常: phone={}", maskPhone(phone), e);
            throw new BusinessException("验证码校验失败，请稍后重试");
        }
    }

    private void putCommonPhoneParams(CommonRequest request, String phone) {
        request.putQueryParameter("CountryCode", smsProperties.getCountryCode());
        request.putQueryParameter("PhoneNumber", phone);
        if (StringUtils.hasText(smsProperties.getSchemeName())) {
            request.putQueryParameter("SchemeName", smsProperties.getSchemeName());
        }
    }

    private boolean isOk(JsonNode body) {
        return "OK".equalsIgnoreCase(body.path("Code").asText(""));
    }

    private BusinessException mapApiError(JsonNode body, String phone) {
        String code = body.path("Code").asText("");
        String message = body.path("Message").asText("");
        log.error("PNVS API 失败: phone={}, code={}, message={}, body={}",
                maskPhone(phone), code, message, body.toString());

        if ("biz.FREQUENCY".equalsIgnoreCase(code) || message.toLowerCase().contains("frequency")) {
            return new BusinessException("发送过于频繁，请 60 秒后再试");
        }
        if (message.contains("签名") || message.contains("模版") || message.contains("模板")
                || "isv.INVALID_PARAMETERS".equalsIgnoreCase(code)) {
            return new BusinessException(
                    "短信签名或模板无效。请到号码认证控制台 → 短信认证 → 参数配置 → 赠送签名配置，"
                            + "复制列表中的签名名称（不要用旧方案名），模板用 100001，"
                            + "当前配置 sign=" + smsProperties.getSignName()
                            + " template=" + smsProperties.getTemplateCode());
        }
        return new BusinessException("短信发送失败：" + message);
    }

    private BusinessException apiError(String prefix, JsonNode body, String phone) {
        String code = body.path("Code").asText("");
        String message = body.path("Message").asText("");
        log.error("{}: phone={}, code={}, message={}", prefix, maskPhone(phone), code, message);
        return new BusinessException(prefix + "：" + message);
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

    private void validateConfig() {
        if (!StringUtils.hasText(smsProperties.getAccessKeyId())
                || !StringUtils.hasText(smsProperties.getAccessKeySecret())) {
            throw new BusinessException("短信服务未配置 AccessKey");
        }
        if (!StringUtils.hasText(smsProperties.getSignName())) {
            throw new BusinessException("短信服务未配置签名");
        }
        if (!StringUtils.hasText(smsProperties.getTemplateCode())) {
            throw new BusinessException("短信服务未配置模板");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
