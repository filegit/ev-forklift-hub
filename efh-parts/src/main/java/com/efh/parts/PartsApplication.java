package com.efh.parts;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 配件服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@MapperScan("com.efh.parts.mapper")
@ComponentScan(basePackages = {"com.efh.parts", "com.efh.common"})
public class PartsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartsApplication.class, args);
        System.out.println("========== 配件服务启动成功 ==========");
    }
}
