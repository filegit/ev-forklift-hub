package com.efh.agent.service.rag;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.RagChunk;
import com.efh.agent.service.vector.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检索结果重排 + 相似度阈值过滤（问题13）
 * 综合分 = keywordWeight * keywordScore + vectorWeight * vectorSimilarity
 */
@Slf4j
@Service
public class RerankService {

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    private VectorStoreService vectorStoreService;

    public List<RagChunk> rerankAndFilter(String question, List<RagChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return chunks;

        double kwW = agentProperties.getRag().getKeywordWeight();
        double vecW = agentProperties.getRag().getVectorWeight();
        double threshold = agentProperties.getRag().getMinSimilarityThreshold();

        double maxKw = chunks.stream().mapToDouble(RagChunk::getScore).max().orElse(1.0);
        if (maxKw <= 0) maxKw = 1.0;

        for (RagChunk chunk : chunks) {
            double normalizedKw = chunk.getScore() / maxKw;
            double vectorSim = vectorStoreService.cosineSimilarity(question, chunk);
            chunk.setVectorScore(vectorSim);
            double combined = kwW * normalizedKw + vecW * vectorSim;
            chunk.setScore(combined);
            vectorStoreService.upsert(chunk, chunk.getContent());
        }

        List<RagChunk> filtered = chunks.stream()
                .filter(c -> c.getScore() >= threshold)
                .sorted(Comparator.comparingDouble(RagChunk::getScore).reversed())
                .limit(agentProperties.getRag().getRerankTopK())
                .collect(Collectors.toList());

        log.debug("重排过滤: 输入{}条, 阈值{}, 输出{}条", chunks.size(), threshold, filtered.size());
        return filtered;
    }
}
