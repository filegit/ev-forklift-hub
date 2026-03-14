package com.efh.common.aspect;

import com.efh.common.annotation.DistributedLockable;
import com.efh.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 */
@Slf4j
@Aspect
@Component
public class DistributedLockAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.efh.common.annotation.DistributedLockable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLockable lockable = signature.getMethod().getAnnotation(DistributedLockable.class);

        String lockKey = lockable.key();
        long waitTime = lockable.waitTime();
        long leaseTime = lockable.leaseTime();

        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("获取锁失败，请稍后重试");
            }
            log.debug("获取分布式锁成功: {}", lockKey);
            return joinPoint.proceed();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁: {}", lockKey);
            }
        }
    }
}
