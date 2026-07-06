package com.efh.agent.service.vector;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.RagChunk;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量数据库层（问题6）
 * - 持久化：MySQL agent_vector_index
 * - 热缓存：内存 + DB 回源
 * - Embedding：优先调用 OpenAI 兼容 /embeddings，失败则本地哈希向量 fallback
 */
@Slf4j
@Service
public class VectorStoreService {

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    @Qualifier("agentJdbcTemplate")
    private JdbcTemplate agentJdbc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public double[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new double[agentProperties.getVector().getDimension()];
        }
        if (agentProperties.isLlmReady()) {
            try {
                double[] apiVec = callEmbeddingApi(text);
                if (apiVec != null) return apiVec;
            } catch (Exception e) {
                log.debug("Embedding API 失败，使用本地向量: {}", e.getMessage());
            }
        }
        return localHashEmbedding(text);
    }

    public void upsert(RagChunk chunk, String content) {
        if (!agentProperties.getVector().isEnabled()) return;
        try {
            double[] vec = embed(content);
            agentJdbc.update(
                    "INSERT INTO agent_vector_index (source_type, source_id, chunk_index, title, content, embedding_json) " +
                            "VALUES (?,?,0,?,?,?) ON DUPLICATE KEY UPDATE title=VALUES(title), content=VALUES(content), embedding_json=VALUES(embedding_json)",
                    chunk.getType(), chunk.getId(), chunk.getTitle(), content, objectMapper.writeValueAsString(vec));
        } catch (Exception e) {
            log.debug("向量索引写入跳过: {}", e.getMessage());
        }
    }

    public double cosineSimilarity(String query, RagChunk chunk) {
        double[] q = embed(query);
        double[] d = loadVector(chunk.getType(), chunk.getId());
        if (d == null) {
            d = embed(chunk.getContent());
        }
        return cosine(q, d);
    }

    private double[] loadVector(String type, Long id) {
        try {
            List<String> rows = agentJdbc.query(
                    "SELECT embedding_json FROM agent_vector_index WHERE source_type=? AND source_id=? LIMIT 1",
                    (rs, i) -> rs.getString(1), type, id);
            if (rows.isEmpty()) return null;
            List<Double> list = objectMapper.readValue(rows.get(0), new TypeReference<List<Double>>() {});
            return list.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (Exception e) {
            return null;
        }
    }

    private double[] callEmbeddingApi(String text) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", agentProperties.getLlm().getEmbeddingModel());
        body.put("input", text.length() > 2000 ? text.substring(0, 2000) : text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(agentProperties.getLlm().getApiKey());

        String base = agentProperties.getLlm().getBaseUrl().replaceAll("/$", "");
        String url = base.endsWith("/v1") ? base + "/embeddings" : base + "/v1/embeddings";

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        JsonNode arr = objectMapper.readTree(resp.getBody()).path("data").path(0).path("embedding");
        if (!arr.isArray()) return null;
        double[] vec = new double[arr.size()];
        for (int i = 0; i < arr.size(); i++) vec[i] = arr.get(i).asDouble();
        return vec;
    }

    /** 本地 fallback：字符 n-gram 哈希向量，无需外部 Embedding 服务 */
    private double[] localHashEmbedding(String text) {
        int dim = agentProperties.getVector().getDimension();
        double[] vec = new double[dim];
        String normalized = text.toLowerCase(Locale.ROOT);
        for (int i = 0; i < normalized.length(); i++) {
            for (int n = 1; n <= 3 && i + n <= normalized.length(); n++) {
                int h = normalized.substring(i, i + n).hashCode();
                vec[Math.floorMod(h, dim)] += 1.0;
            }
        }
        double norm = 0;
        for (double v : vec) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) for (int i = 0; i < dim; i++) vec[i] /= norm;
        return vec;
    }

    private double cosine(double[] a, double[] b) {
        int len = Math.min(a.length, b.length);
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
