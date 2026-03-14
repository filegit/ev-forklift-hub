package com.efh.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面 - JVM性能监控
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    @Around("execution(* com.efh..service..*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取JVM内存信息
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long afterMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = afterMemory - beforeMemory;
            
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            long executionTime = endTime - startTime;
            
            // 记录性能数据
            if (executionTime > 1000) { // 超过1秒记录警告
                log.warn("慢方法检测 - {}.{} 执行时间: {}ms, 内存使用: {}KB", 
                        className, methodName, executionTime, memoryUsed / 1024);
            } else {
                log.debug("方法执行 - {}.{} 执行时间: {}ms, 内存使用: {}KB", 
                        className, methodName, executionTime, memoryUsed / 1024);
            }
        }
    }
}
