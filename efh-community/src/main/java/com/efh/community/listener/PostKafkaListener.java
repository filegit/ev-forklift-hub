package com.efh.community.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka消息监听器
 */
@Slf4j
@Component
public class PostKafkaListener {

    /**
     * 监听帖子创建消息
     */
    @KafkaListener(topics = "post-create-topic", groupId = "efh-community-group")
    public void handlePostCreate(String message) {
        log.info("收到帖子创建消息: {}", message);
        // 处理帖子创建后的业务逻辑，如：更新ES索引、发送通知等
    }

    /**
     * 监听帖子点赞消息
     */
    @KafkaListener(topics = "post-like-topic", groupId = "efh-community-group")
    public void handlePostLike(String message) {
        log.info("收到帖子点赞消息: {}", message);
        // 处理点赞逻辑
    }
}
