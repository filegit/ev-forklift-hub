package com.efh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efh.common.result.Result;
import com.efh.user.entity.PointsExchange;
import com.efh.user.entity.UserPoints;
import com.efh.user.service.PointsExchangeService;
import com.efh.user.service.UserPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户积分控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/points")
public class UserPointsController {
    
    @Autowired
    private UserPointsService userPointsService;
    
    @Autowired
    private PointsExchangeService pointsExchangeService;
    
    /**
     * 获取用户积分
     */
    @GetMapping
    public Result<UserPoints> getUserPoints(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        UserPoints points = userPointsService.getUserPoints(userId);
        return Result.success(points);
    }
    
    /**
     * 兑换积分
     */
    @PostMapping("/exchange/{exchangeId}")
    public Result<?> exchange(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                              @PathVariable Long exchangeId) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        pointsExchangeService.exchange(userId, exchangeId);
        return Result.success(null);
    }
    
    /**
     * 获取兑换记录
     */
    @GetMapping("/exchanges")
    public Result<IPage<PointsExchange>> getExchanges(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        IPage<PointsExchange> exchanges = pointsExchangeService.getUserExchanges(userId, new Page<>(page, size));
        return Result.success(exchanges);
    }
}
