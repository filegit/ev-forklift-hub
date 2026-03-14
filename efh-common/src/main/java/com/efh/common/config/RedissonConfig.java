package com.efh.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 * 用于配置分布式锁、分布式集合等功能
 * 
 * @author EFH Team
 */
@Configuration
public class RedissonConfig {

    /**
     * Redis 服务器地址
     * 格式: redis://host:port
     * 默认值: redis://127.0.0.1:6379
     */
    @Value("${redisson.address:redis://127.0.0.1:6379}")
    private String address;

    /**
     * Redis 数据库索引
     * 默认值: 0
     */
    @Value("${redisson.database:0}")
    private int database;

    /**
     * 连接超时时间（毫秒）
     * 默认值: 3000ms (3秒)
     */
    @Value("${redisson.timeout:3000}")
    private int timeout;

    /**
     * 连接池大小
     * 默认值: 64
     */
    @Value("${redisson.connection-pool-size:64}")
    private int connectionPoolSize;

    /**
     * 最小空闲连接数
     * 默认值: 10
     */
    @Value("${redisson.connection-minimum-idle-size:10}")
    private int connectionMinimumIdleSize;

    /**
     * Redis 密码（可选）
     * 如果 Redis 没有设置密码，可以不配置此项
     */
    @Value("${redisson.password:#{null}}")
    private String password;

    /**
     * 创建 RedissonClient Bean
     * 
     * @return RedissonClient 实例
     */
    @Bean
    public RedissonClient redissonClient() {
        // 创建配置对象
        Config config = new Config();
        
        // 配置单机模式
        config.useSingleServer()
                .setAddress(address)                                    // 设置 Redis 地址
                .setPassword(password)                                  // 设置密码（如果有）
                .setDatabase(database)                                  // 设置数据库索引
                .setTimeout(timeout)                                    // 设置超时时间
                .setConnectionPoolSize(connectionPoolSize)              // 设置连接池大小
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize); // 设置最小空闲连接数
        
        // 创建并返回 RedissonClient 实例
        return Redisson.create(config);
    }
}
