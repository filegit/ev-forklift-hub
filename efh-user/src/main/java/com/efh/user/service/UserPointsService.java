package com.efh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efh.user.entity.UserPoints;

/**
 * 用户积分服务接口
 */
public interface UserPointsService extends IService<UserPoints> {
    
    /**
     * 获取用户积分
     */
    UserPoints getUserPoints(Long userId);
    
    /**
     * 增加积分
     */
    void addPoints(Long userId, Integer points, String reason);
    
    /**
     * 消耗积分
     */
    void consumePoints(Long userId, Integer points, String reason);
    
    /**
     * 检查积分是否足够
     */
    boolean hasEnoughPoints(Long userId, Integer points);
}
