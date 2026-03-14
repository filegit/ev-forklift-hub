package com.efh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efh.common.exception.BusinessException;
import com.efh.user.entity.UserPoints;
import com.efh.user.mapper.UserPointsMapper;
import com.efh.user.service.UserPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户积分服务实现
 */
@Slf4j
@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService {
    
    @Override
    public UserPoints getUserPoints(Long userId) {
        LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPoints::getUserId, userId);
        
        UserPoints points = this.getOne(wrapper);
        
        if (points == null) {
            // 首次获取，创建积分记录
            points = new UserPoints();
            points.setUserId(userId);
            points.setTotalPoints(0);
            points.setUsedPoints(0);
            points.setAvailablePoints(0);
            this.save(points);
        }
        
        return points;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer points, String reason) {
        log.info("增加积分: userId={}, points={}, reason={}", userId, points, reason);
        
        UserPoints userPoints = this.getUserPoints(userId);
        userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() + points);
        
        this.updateById(userPoints);
        log.info("积分增加成功: userId={}, totalPoints={}", userId, userPoints.getTotalPoints());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumePoints(Long userId, Integer points, String reason) {
        log.info("消耗积分: userId={}, points={}, reason={}", userId, points, reason);
        
        UserPoints userPoints = this.getUserPoints(userId);
        
        if (userPoints.getAvailablePoints() < points) {
            throw new BusinessException("积分不足");
        }
        
        userPoints.setUsedPoints(userPoints.getUsedPoints() + points);
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() - points);
        
        this.updateById(userPoints);
        log.info("积分消耗成功: userId={}, availablePoints={}", userId, userPoints.getAvailablePoints());
    }
    
    @Override
    public boolean hasEnoughPoints(Long userId, Integer points) {
        UserPoints userPoints = this.getUserPoints(userId);
        return userPoints.getAvailablePoints() >= points;
    }
}
