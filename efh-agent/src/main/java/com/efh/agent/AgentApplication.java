package com.efh.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.efh.agent.feign")
@ComponentScan(basePackages = {"com.efh.agent", "com.efh.common"})
public class AgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
        System.out.println("========== AI Agent 服务启动成功 ==========");
    }
}
