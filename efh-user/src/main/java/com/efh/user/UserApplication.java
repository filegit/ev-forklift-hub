package com.efh.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@MapperScan("com.efh.user.mapper")
@ComponentScan(basePackages = {"com.efh.user", "com.efh.common"})
public class UserApplication {
    public static void main(String[] args) {
        // JVM参数优化
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", 
                String.valueOf(Runtime.getRuntime().availableProcessors() * 2));
        
        SpringApplication.run(UserApplication.class, args);
        System.out.println("========== 用户服务启动成功 ==========");
    }
}
