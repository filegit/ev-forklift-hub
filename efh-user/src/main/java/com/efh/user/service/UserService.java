package com.efh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.user.entity.User;
import com.efh.user.vo.LoginVO;
import com.efh.user.vo.RegisterVO;
import com.efh.user.vo.SmsLoginVO;

public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    String register(RegisterVO registerVO);

    
    /**
     * 用户登录
     */
    String login(LoginVO loginVO);

    /**
     * 验证码登录（手机号 + 短信验证码）
     */
    String loginBySms(SmsLoginVO vo);

    /**
     * 向手机号发送登录验证码
     */
    String sendLoginSmsByPhone(String phone);
    
    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);
}
