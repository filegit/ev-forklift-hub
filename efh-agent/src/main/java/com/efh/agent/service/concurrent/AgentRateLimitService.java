package com.efh.agent.service.concurrent;

import com.efh.agent.config.AgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 并发限流（问题2）：Redis 滑动窗口，按用户维度限制 QPS
 */
@Slf4j
@Service
public class AgentRateLimitService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AgentProperties agentProperties;

    public void checkRateLimit(Long userId) {
        Long uid = userId == null ? 0L : userId;
        String key = "agent:rate:" + uid + ":" + (System.currentTimeMillis() / 60000);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 2, TimeUnit.MINUTES);
        }
        int max = agentProperties.getSecurity().getMaxRequestsPerMinute();
        if (count != null && count > max) {
            throw new IllegalStateException("请求过于频繁，请稍后再试（每分钟最多 " + max + " 次）");
        }
    }
}
