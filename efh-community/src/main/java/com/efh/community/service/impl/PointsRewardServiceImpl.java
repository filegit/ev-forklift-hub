package com.efh.community.service.impl;

import com.efh.common.result.Result;
import com.efh.community.feign.UserFeignClient;
import com.efh.community.service.PointsRewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PointsRewardServiceImpl implements PointsRewardService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public void reward(Long userId, int points, String reason) {
        if (userId == null || points <= 0) {
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("points", points);
            body.put("reason", reason);
            Result<Void> result = userFeignClient.addPoints(String.valueOf(userId), body);
            if (result == null || result.getCode() != 200) {
                log.warn("积分奖励失败: userId={}, points={}, reason={}", userId, points, reason);
            }
        } catch (Exception e) {
            log.warn("积分奖励调用异常: userId={}, points={}, reason={}", userId, points, reason, e);
        }
    }
}
