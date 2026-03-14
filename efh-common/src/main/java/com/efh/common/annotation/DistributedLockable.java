package com.efh.common.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLockable {

    /**
     * 锁的key
     */
    String key();

    /**
     * 等待时间（秒）
     */
    long waitTime() default 3;

    /**
     * 锁定时间（秒）
     */
    long leaseTime() default 10;
}
