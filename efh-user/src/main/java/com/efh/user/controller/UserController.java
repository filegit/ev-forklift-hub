package com.efh.user.controller;

import com.efh.common.result.Result;
import com.efh.common.utils.JwtUtil;
import com.efh.user.entity.User;
import com.efh.user.service.UserService;
import com.efh.user.vo.LoginVO;
import com.efh.user.vo.RegisterVO;
import com.efh.user.vo.SendLoginSmsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 提供用户注册、登录、信息查询等接口
 * 
 * @author EFH Team
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     * 
     * POST /user/api/register
     * 
     * 请求体示例：
     * {
     *   "username": "testuser",
     *   "password": "123456",
     *   "nickname": "测试用户",
     *   "phone": "13800138000"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "注册成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiJ9..."
     *   }
     * }
     */
    @PostMapping("/register")
    public Result<Map<String, String>> register(@Validated @RequestBody RegisterVO registerVO) {
        log.info("用户注册请求: username={}", registerVO.getUsername());
        
        // 调用服务层进行注册
        String token = userService.register(registerVO);
        
        // 构造响应数据
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        
        log.info("用户注册成功: username={}", registerVO.getUsername());
        return Result.success(data);
    }
    
    /**
     * 用户登录
     * 
     * POST /user/api/login
     * 
     * 请求体示例：
     * {
     *   "username": "testuser",
     *   "password": "123456"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "登录成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiJ9..."
     *   }
     * }
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Validated @RequestBody LoginVO loginVO) {
        log.info("用户登录请求: username={}", loginVO.getUsername());
        
        // 调用服务层进行登录
        String token = userService.login(loginVO);
        
        // 构造响应数据
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        
        log.info("用户登录成功: username={}", loginVO.getUsername());
        return Result.success(data);
    }

    /**
     * 发送登录短信验证码
     *
     * POST /user/api/sms/login
     * { "username": "admin" }
     */
    @PostMapping("/sms/login")
    public Result<Map<String, String>> sendLoginSms(@Validated @RequestBody SendLoginSmsVO vo) {
        log.info("发送登录验证码: username={}", vo.getUsername());
        String mockCode = userService.sendLoginSmsCode(vo.getUsername());
        Map<String, String> data = new HashMap<>();
        if (mockCode != null) {
            data.put("mockCode", mockCode);
            data.put("message", "演示模式：验证码为 " + mockCode);
        } else {
            data.put("message", "验证码已发送到绑定手机，请注意查收");
        }
        return Result.success(data);
    }
    
    /**
     * 获取当前用户信息
     * 
     * GET /user/api/info
     * 
     * 请求头：
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...（网关会验证）
     * X-User-Id: {userId}（网关传递）
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "id": 1,
     *     "username": "testuser",
     *     "nickname": "测试用户",
     *     "phone": "13800138000",
     *     "status": 1
     *   }
     * }
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        // 从网关传递的请求头中获取用户ID
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            log.error("用户ID为空，请求未通过网关或网关未传递用户ID");
            return Result.error(401, "未授权");
        }
        
        log.info("获取用户信息请求: userId={}", userId);
        
        // 调用服务层获取用户信息
        User user = userService.getUserInfo(userId);
        
        return Result.success(user);
    }
    
    /**
     * 测试接口 - 验证服务是否正常
     * 
     * GET /user/api/test
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("用户服务运行正常！");
    }
}
