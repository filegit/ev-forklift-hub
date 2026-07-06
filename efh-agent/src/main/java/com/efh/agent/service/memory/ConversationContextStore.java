package com.efh.agent.service.memory;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.ChatMessage;
import com.efh.agent.model.ConversationContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 多轮对话上下文存储（问题1/12）
 * - 短期：Redis List JSON，所有 Java 实例共享
 * - 长期：MySQL agent_conversation.summary
 * - 分布式锁：同一 userId+sessionId 读写串行，避免多实例混乱
 */
@Slf4j
@Service
public class ConversationContextStore {

    private static final String REDIS_CTX_PREFIX = "agent:ctx:";
    private static final String REDIS_LOCK_PREFIX = "agent:lock:ctx:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    @Qualifier("agentJdbcTemplate")
    private JdbcTemplate agentJdbc;

    @Autowired
    private AgentProperties agentProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String resolveSessionId(String sessionId) {
        return sessionId == null || sessionId.trim().isEmpty()
                ? UUID.randomUUID().toString().replace("-", "")
                : sessionId.trim();
    }

    public ConversationContext load(Long userId, String sessionId) {
        Long uid = userId == null ? 0L : userId;
        String sid = resolveSessionId(sessionId);
        String key = REDIS_CTX_PREFIX + uid + ":" + sid;

        ConversationContext ctx = new ConversationContext();
        ctx.setUserId(uid);
        ctx.setSessionId(sid);

        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null && !json.isEmpty()) {
                List<ChatMessage> messages = objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {});
                ctx.setMessages(messages);
            }
        } catch (Exception e) {
            log.warn("加载 Redis 短期记忆失败: {}", e.getMessage());
        }

        try {
            List<String> summaries = agentJdbc.query(
                    "SELECT summary FROM agent_conversation WHERE user_id = ? AND session_id = ? AND summary IS NOT NULL",
                    (rs, rowNum) -> rs.getString(1), uid, sid);
            if (!summaries.isEmpty()) {
                ctx.setSummary(summaries.get(0));
            }
        } catch (Exception e) {
            log.debug("长期记忆未初始化: {}", e.getMessage());
        }
        return ctx;
    }

    public void save(ConversationContext ctx) {
        if (ctx == null) return;
        Long uid = ctx.getUserId() == null ? 0L : ctx.getUserId();
        String sid = ctx.getSessionId();
        String lockKey = REDIS_LOCK_PREFIX + uid + ":" + sid;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(agentProperties.getSessionLockSeconds(), TimeUnit.SECONDS);
            trimShortTerm(ctx);
            persistRedis(uid, sid, ctx.getMessages());
            persistMysql(uid, sid, ctx.getSummary(), ctx.getMessages());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void appendMessage(Long userId, String sessionId, ChatMessage message) {
        ConversationContext ctx = load(userId, sessionId);
        ctx.getMessages().add(message);
        save(ctx);
    }

    private void trimShortTerm(ConversationContext ctx) {
        int max = agentProperties.getShortTermMaxMessages();
        List<ChatMessage> list = ctx.getMessages();
        if (list.size() > max) {
            ctx.setMessages(new ArrayList<>(list.subList(list.size() - max, list.size())));
        }
    }

    private void persistRedis(Long userId, String sessionId, List<ChatMessage> messages) {
        try {
            String key = REDIS_CTX_PREFIX + userId + ":" + sessionId;
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(messages), 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入 Redis 短期记忆失败: {}", e.getMessage());
        }
    }

    private void persistMysql(Long userId, String sessionId, String summary, List<ChatMessage> messages) {
        try {
            agentJdbc.update(
                    "INSERT INTO agent_conversation (user_id, session_id, summary, message_count, update_time) VALUES (?,?,?,?,NOW()) " +
                            "ON DUPLICATE KEY UPDATE summary=COALESCE(VALUES(summary), summary), message_count=?, update_time=NOW()",
                    userId, sessionId, summary, messages.size(), messages.size());
        } catch (Exception e) {
            log.debug("MySQL 会话元数据写入跳过: {}", e.getMessage());
        }
    }

    public void updateSummary(Long userId, String sessionId, String summary) {
        try {
            agentJdbc.update(
                    "INSERT INTO agent_conversation (user_id, session_id, summary, update_time) VALUES (?,?,?,NOW()) " +
                            "ON DUPLICATE KEY UPDATE summary=VALUES(summary), update_time=NOW()",
                    userId, sessionId, summary);
        } catch (Exception e) {
            log.warn("更新长期记忆摘要失败: {}", e.getMessage());
        }
    }
}
