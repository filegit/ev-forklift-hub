package com.efh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.user.entity.User;
import com.efh.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
public class UserProfileController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取个人信息
     */
    @GetMapping
    public Result<User> getProfile(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        User user = userService.getById(userId);
        return Result.success(user);
    }
    
    /**
     * 更新个人信息
     */
    @PutMapping
    public Result<?> updateProfile(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                   @RequestBody User user) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        user.setId(userId);
        userService.updateById(user);
        return Result.success(null);
    }
}
