package com.efh.agent.service;

import com.efh.agent.config.AgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class DocumentTextService {

    private final Tika tika = new Tika();

    @Autowired
    private AgentProperties agentProperties;

    public String extractText(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return "";
        }
        try {
            Path path = Paths.get(agentProperties.getKnowledgeUploadDir()).resolve(relativePath).normalize();
            if (!Files.exists(path)) {
                return "";
            }
            String lower = path.getFileName().toString().toLowerCase();
            if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".json")) {
                byte[] bytes = Files.readAllBytes(path);
                return new String(bytes, StandardCharsets.UTF_8);
            }
            return tika.parseToString(path.toFile());
        } catch (Exception e) {
            log.warn("文档解析失败: path={}", relativePath, e);
            return "";
        }
    }

    public String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLen) {
            return normalized;
        }
        return normalized.substring(0, maxLen) + "...";
    }
}
