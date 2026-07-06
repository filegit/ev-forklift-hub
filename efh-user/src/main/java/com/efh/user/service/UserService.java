package com.efh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.user.entity.User;
import com.efh.user.vo.LoginVO;
import com.efh.user.vo.RegisterVO;

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
     * 发送登录短信验证码
     * @return 演示模式下的验证码（便于测试）
     */
    String sendLoginSmsCode(String username);
    
    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);
}
