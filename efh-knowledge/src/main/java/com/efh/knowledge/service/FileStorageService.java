package com.efh.knowledge.service;

import com.efh.common.exception.BusinessException;
import com.efh.knowledge.config.KnowledgeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired
    private KnowledgeProperties properties;

    public String store(MultipartFile file) {
        try {
            Path dir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String ext = extractExt(file.getOriginalFilename());
            String stored = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(stored);
            file.transferTo(target.toFile());
            return stored;
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
    }

    public Path resolve(String storedName) {
        return Paths.get(properties.getUploadDir()).toAbsolutePath().normalize().resolve(storedName);
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException ignored) {
        }
    }

    private String extractExt(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
