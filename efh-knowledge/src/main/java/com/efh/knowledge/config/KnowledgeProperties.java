package com.efh.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "knowledge")
public class KnowledgeProperties {
    private String uploadDir = "./uploads/knowledge";
    private int adminUserType = 9;
}
