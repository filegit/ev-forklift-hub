package com.efh.agent.service;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.feign.UserFeignClient;
import com.efh.agent.feign.dto.UserBriefDTO;
import com.efh.agent.model.RagChunk;
import com.efh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagRetrievalService {

    private static final int ACCESS_FREE = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final String UNLOCK_ALIPAY = "alipay";

    @Autowired
    @Qualifier("knowledgeJdbcTemplate")
    private JdbcTemplate knowledgeJdbc;

    @Autowired
    @Qualifier("communityJdbcTemplate")
    private JdbcTemplate communityJdbc;

    @Autowired
    private DocumentTextService documentTextService;

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    private UserFeignClient userFeignClient;

    public List<RagChunk> retrieve(String question, Long userId, String scope) {
        List<String> keywords = tokenize(question);
        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        List<RagChunk> chunks = new ArrayList<>();
        if (!"community".equals(scope)) {
            chunks.addAll(searchKnowledge(keywords, userId));
        }
        if (!"knowledge".equals(scope)) {
            chunks.addAll(searchCommunityPosts(keywords));
            chunks.addAll(searchCommunityComments(keywords));
        }

        chunks.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        List<RagChunk> limited = chunks.stream().limit(agentProperties.getMaxChunks() * 2).collect(Collectors.toList());
        return rerankService.rerankAndFilter(question, limited);
    }

    @Autowired
    private com.efh.agent.service.rag.RerankService rerankService;

    private List<RagChunk> searchKnowledge(List<String> keywords, Long userId) {
        List<Map<String, Object>> docs = knowledgeJdbc.queryForList(
                "SELECT id, title, summary, access_type, file_path FROM knowledge_doc WHERE status = ? ORDER BY create_time DESC LIMIT 80",
                STATUS_PUBLISHED
        );
        boolean admin = isAdmin(userId);
        Set<Long> unlockedIds = loadUnlockedDocIds(userId);

        List<RagChunk> chunks = new ArrayList<>();
        for (Map<String, Object> doc : docs) {
            Long id = toLong(doc.get("id"));
            String title = stringVal(doc.get("title"));
            String summary = stringVal(doc.get("summary"));
            int accessType = toInt(doc.get("access_type"));
            String filePath = stringVal(doc.get("file_path"));

            boolean unlocked = accessType == ACCESS_FREE || admin || unlockedIds.contains(id);
            StringBuilder text = new StringBuilder();
            text.append(title).append(" ").append(summary);

            if (unlocked) {
                String fileText = documentTextService.extractText(filePath);
                if (!fileText.isEmpty()) {
                    text.append(" ").append(fileText);
                }
            }

            double score = scoreText(text.toString(), keywords);
            if (score <= 0) {
                continue;
            }

            RagChunk chunk = new RagChunk();
            chunk.setType("knowledge");
            chunk.setId(id);
            chunk.setTitle(title);
            chunk.setUnlocked(unlocked);
            if (unlocked) {
                chunk.setContent(documentTextService.truncate(text.toString(), agentProperties.getMaxChunkChars()));
            } else {
                chunk.setContent("【未解锁文档，仅摘要】" + documentTextService.truncate(title + " " + summary, 200));
                chunk.setScore(score * 0.6);
            }
            if (unlocked) {
                chunk.setScore(score);
            }
            chunks.add(chunk);
        }
        return chunks;
    }

    private List<RagChunk> searchCommunityPosts(List<String> keywords) {
        List<Map<String, Object>> posts = communityJdbc.queryForList(
                "SELECT id, title, content FROM post_0 WHERE status = 1 ORDER BY create_time DESC LIMIT 100"
        );
        List<RagChunk> chunks = new ArrayList<>();
        for (Map<String, Object> post : posts) {
            String title = stringVal(post.get("title"));
            String content = stringVal(post.get("content"));
            double score = scoreText(title + " " + content, keywords);
            if (score <= 0) {
                continue;
            }
            RagChunk chunk = new RagChunk();
            chunk.setType("post");
            chunk.setId(toLong(post.get("id")));
            chunk.setTitle(title);
            chunk.setContent(documentTextService.truncate(content, agentProperties.getMaxChunkChars()));
            chunk.setUnlocked(true);
            chunk.setScore(score);
            chunks.add(chunk);
        }
        return chunks;
    }

    private List<RagChunk> searchCommunityComments(List<String> keywords) {
        List<Map<String, Object>> comments = communityJdbc.queryForList(
                "SELECT c.id, c.content, c.post_id, p.title AS post_title FROM comment_0 c " +
                        "LEFT JOIN post_0 p ON c.post_id = p.id WHERE p.status = 1 ORDER BY c.create_time DESC LIMIT 120"
        );
        List<RagChunk> chunks = new ArrayList<>();
        for (Map<String, Object> row : comments) {
            String content = stringVal(row.get("content"));
            String postTitle = stringVal(row.get("post_title"));
            double score = scoreText(content + " " + postTitle, keywords);
            if (score <= 0) {
                continue;
            }
            RagChunk chunk = new RagChunk();
            chunk.setType("comment");
            chunk.setId(toLong(row.get("id")));
            chunk.setTitle("评论：" + documentTextService.truncate(postTitle, 40));
            chunk.setContent(documentTextService.truncate(content, agentProperties.getMaxChunkChars()));
            chunk.setUnlocked(true);
            chunk.setScore(score * 0.85);
            chunks.add(chunk);
        }
        return chunks;
    }

    private Set<Long> loadUnlockedDocIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        List<Map<String, Object>> rows = knowledgeJdbc.queryForList(
                "SELECT doc_id, unlock_type, third_trade_no FROM knowledge_unlock WHERE user_id = ?",
                userId
        );
        Set<Long> ids = new HashSet<>();
        for (Map<String, Object> row : rows) {
            String unlockType = stringVal(row.get("unlock_type"));
            String tradeNo = stringVal(row.get("third_trade_no"));
            if (UNLOCK_ALIPAY.equals(unlockType) && (tradeNo == null || tradeNo.isEmpty())) {
                continue;
            }
            ids.add(toLong(row.get("doc_id")));
        }
        return ids;
    }

    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        try {
            Result<UserBriefDTO> result = userFeignClient.getUserBrief(String.valueOf(userId));
            return result != null && result.getCode() == 200
                    && result.getData() != null
                    && result.getData().getUserType() != null
                    && result.getData().getUserType() == agentProperties.getAdminUserType();
        } catch (Exception e) {
            return false;
        }
    }

    private double scoreText(String text, List<String> keywords) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        double score = 0;
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase(Locale.ROOT))) {
                score += kw.length() >= 4 ? 2.0 : 1.0;
            }
        }
        return score;
    }

    private List<String> tokenize(String question) {
        if (question == null) {
            return Collections.emptyList();
        }
        String cleaned = question.replaceAll("[\\p{Punct}，。！？、；：\"'（）【】《》\\s]+", "").trim();
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> stop = new HashSet<>(Arrays.asList("的", "了", "是", "在", "和", "与", "吗", "呢", "啊", "怎么", "如何", "什么", "为什么"));
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        tokens.add(cleaned);
        for (String part : cleaned.split("\\s+")) {
            if (part.length() >= 2 && !stop.contains(part)) {
                tokens.add(part);
            }
        }
        for (int i = 0; i < cleaned.length() - 1; i++) {
            String bi = cleaned.substring(i, i + 2);
            if (!stop.contains(bi)) {
                tokens.add(bi);
            }
        }
        if (cleaned.length() >= 4) {
            tokens.add(cleaned.substring(0, Math.min(6, cleaned.length())));
        }
        return new ArrayList<>(tokens);
    }

    private String buildLikePattern(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        String longest = keywords.stream().max(Comparator.comparingInt(String::length)).orElse(null);
        if (longest == null || longest.length() < 2) {
            return null;
        }
        return "%" + longest + "%";
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        return Long.parseLong(v.toString());
    }

    private int toInt(Object v) {
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return Integer.parseInt(v.toString());
    }

    private String stringVal(Object v) {
        return v == null ? "" : v.toString();
    }
}
