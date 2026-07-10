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
            log.warn("Load Redis conversation memory failed: {}", e.getMessage());
        }

        if (ctx.getMessages().isEmpty()) {
            ctx.setMessages(loadMysqlMessages(uid, sid));
        }

        try {
            List<String> summaries = agentJdbc.query(
                    "SELECT summary FROM agent_conversation WHERE user_id = ? AND session_id = ? AND summary IS NOT NULL",
                    (rs, rowNum) -> rs.getString(1), uid, sid);
            if (!summaries.isEmpty()) {
                ctx.setSummary(summaries.get(0));
            }
        } catch (Exception e) {
            log.debug("Long-term memory is not initialized: {}", e.getMessage());
        }
        return ctx;
    }

    public void save(ConversationContext ctx) {
        if (ctx == null) {
            return;
        }
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
            log.warn("Write Redis conversation memory failed: {}", e.getMessage());
        }
    }

    private void persistMysql(Long userId, String sessionId, String summary, List<ChatMessage> messages) {
        try {
            agentJdbc.update(
                    "INSERT INTO agent_conversation (user_id, session_id, summary, message_count, update_time) VALUES (?,?,?,?,NOW()) " +
                            "ON DUPLICATE KEY UPDATE summary=COALESCE(VALUES(summary), summary), message_count=?, update_time=NOW()",
                    userId, sessionId, summary, messages.size(), messages.size());
            persistMysqlMessages(userId, sessionId, messages);
        } catch (Exception e) {
            log.debug("Write MySQL conversation metadata skipped: {}", e.getMessage());
        }
    }

    private List<ChatMessage> loadMysqlMessages(Long userId, String sessionId) {
        try {
            Long conversationId = findConversationId(userId, sessionId);
            if (conversationId == null) {
                return new ArrayList<>();
            }
            return agentJdbc.query(
                    "SELECT role, content, UNIX_TIMESTAMP(create_time) * 1000 FROM agent_message WHERE conversation_id = ? ORDER BY id ASC",
                    (rs, rowNum) -> {
                        String content = rs.getString(2);
                        ChatMessage message = new ChatMessage();
                        message.setRole(rs.getString(1));
                        message.setContent(content);
                        message.setTokenCount(estimateTokens(content));
                        message.setTimestamp(rs.getLong(3));
                        return message;
                    }, conversationId);
        } catch (Exception e) {
            log.debug("Read MySQL conversation messages skipped: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void persistMysqlMessages(Long userId, String sessionId, List<ChatMessage> messages) {
        if (messages == null) {
            return;
        }
        try {
            Long conversationId = findConversationId(userId, sessionId);
            if (conversationId == null) {
                return;
            }
            agentJdbc.update("DELETE FROM agent_message WHERE conversation_id = ?", conversationId);
            for (ChatMessage message : messages) {
                agentJdbc.update(
                        "INSERT INTO agent_message (conversation_id, role, content, create_time) VALUES (?,?,?,FROM_UNIXTIME(? / 1000))",
                        conversationId, message.getRole(), message.getContent(), message.getTimestamp());
            }
        } catch (Exception e) {
            log.debug("Write MySQL conversation messages skipped: {}", e.getMessage());
        }
    }

    private Long findConversationId(Long userId, String sessionId) {
        List<Long> ids = agentJdbc.query(
                "SELECT id FROM agent_conversation WHERE user_id = ? AND session_id = ? LIMIT 1",
                (rs, rowNum) -> rs.getLong(1), userId, sessionId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private int estimateTokens(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return Math.max(1, content.length() / 2);
    }

    public void updateSummary(Long userId, String sessionId, String summary) {
        try {
            agentJdbc.update(
                    "INSERT INTO agent_conversation (user_id, session_id, summary, update_time) VALUES (?,?,?,NOW()) " +
                            "ON DUPLICATE KEY UPDATE summary=VALUES(summary), update_time=NOW()",
                    userId, sessionId, summary);
        } catch (Exception e) {
            log.warn("Update long-term memory summary failed: {}", e.getMessage());
        }
    }
}
