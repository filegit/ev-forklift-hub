package com.efh.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.efh.knowledge.feign")
@MapperScan("com.efh.knowledge.mapper")
@ComponentScan(basePackages = {"com.efh.knowledge", "com.efh.common"})
public class KnowledgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeApplication.class, args);
        System.out.println("========== 知识库服务启动成功 ==========");
    }
}
