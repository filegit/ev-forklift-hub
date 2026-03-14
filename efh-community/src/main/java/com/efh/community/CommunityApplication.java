package com.efh.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 社区服务启动类
 * 暂时不启用 Elasticsearch Repository，避免启动失败
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@MapperScan("com.efh.community.mapper")
@ComponentScan(basePackages = {"com.efh.community", "com.efh.common"})
// 暂时不启用 Elasticsearch Repository
// @EnableElasticsearchRepositories(basePackages = "com.efh.community.repository")
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
        System.out.println("========== 社区服务启动成功 ==========");
    }
}
