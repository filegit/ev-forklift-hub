package com.efh.user.service.impl;

import com.efh.common.exception.BusinessException;
import com.efh.user.config.SmsProperties;
import com.efh.user.entity.User;
import com.efh.user.service.AliyunPnvsSmsSender;
import com.efh.user.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private static final String CODE_KEY_PREFIX = "sms:login:code:";
    private static final String INTERVAL_KEY_PREFIX = "sms:login:interval:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private AliyunPnvsSmsSender aliyunPnvsSmsSender;

    @Override
    public String sendLoginCode(User user) {
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!StringUtils.hasText(user.getPhone())) {
            throw new BusinessException("该账号未绑定手机号，无法发送验证码");
        }

        String intervalKey = INTERVAL_KEY_PREFIX + user.getUsername();
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(intervalKey))) {
            throw new BusinessException("发送过于频繁，请稍后再试");
        }

        stringRedisTemplate.opsForValue().set(intervalKey, "1",
                smsProperties.getSendIntervalSeconds(), TimeUnit.SECONDS);

        if (smsProperties.isMock()) {
            String code = generateCode();
            String codeKey = CODE_KEY_PREFIX + user.getUsername();
            stringRedisTemplate.opsForValue().set(codeKey, code,
                    smsProperties.getCodeExpireSeconds(), TimeUnit.SECONDS);
            log.info("[SMS-MOCK] 登录验证码 username={} phone={} code={}",
                    user.getUsername(), maskPhone(user.getPhone()), code);
            return code;
        }

        try {
            aliyunPnvsSmsSender.sendVerificationCode(user.getPhone());
        } catch (Exception e) {
            stringRedisTemplate.delete(intervalKey);
            throw e;
        }
        return null;
    }

    @Override
    public void verifyLoginCode(String username, String phone, String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("请输入短信验证码");
        }

        if (smsProperties.isMock()) {
            String codeKey = CODE_KEY_PREFIX + username;
            String cached = stringRedisTemplate.opsForValue().get(codeKey);
            if (!StringUtils.hasText(cached)) {
                throw new BusinessException("验证码已过期，请重新获取");
            }
            if (!cached.equals(code.trim())) {
                throw new BusinessException("短信验证码错误");
            }
            stringRedisTemplate.delete(codeKey);
            return;
        }

        aliyunPnvsSmsSender.checkVerificationCode(phone, code);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
