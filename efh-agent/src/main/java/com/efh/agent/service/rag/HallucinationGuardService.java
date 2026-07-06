package com.efh.agent.service.rag;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.RagChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG 幻觉缓解（问题14）
 * 1. 低相关资料时不让 LLM 自由发挥
 * 2. 检测回答与检索片段的关键词重叠率
 * 3. 低重叠时追加免责声明并弱化断言
 */
@Slf4j
@Service
public class HallucinationGuardService {

    @Autowired
    private AgentProperties agentProperties;

    public String guard(String question, String answer, List<RagChunk> chunks, boolean llmUsed) {
        if (answer == null) return "";
        if (!llmUsed) return answer;

        if (chunks == null || chunks.isEmpty()) {
            return "【提示：未检索到可靠资料，以下回答仅供参考，请以官方文档或社区核实为准】\n\n" + answer;
        }

        double overlap = groundingOverlap(answer, chunks);
        double min = agentProperties.getRag().getMinGroundingOverlap();
        if (overlap < min) {
            log.warn("幻觉风险: overlap={}, question={}", overlap, question);
            return answer + "\n\n---\n⚠️ **资料支撑不足**（与检索内容匹配度 " + String.format("%.0f%%", overlap * 100)
                    + "），建议结合知识库原文或社区帖子进一步核实，避免仅凭 AI 回答操作设备。";
        }
        return answer;
    }

    public String buildGroundedSystemPrompt() {
        return "你是新能源叉车领域助手。必须严格基于【参考资料】回答，不得编造数据、型号、步骤。\n"
                + "若资料不足，明确说「资料中未找到」。\n"
                + "回答末尾用 [1][2] 标注引用编号。禁止输出与叉车无关或危险违规操作。";
    }

    private double groundingOverlap(String answer, List<RagChunk> chunks) {
        Set<String> sourceTokens = new HashSet<>();
        for (RagChunk c : chunks) {
            sourceTokens.addAll(tokenize(c.getContent()));
            sourceTokens.addAll(tokenize(c.getTitle()));
        }
        if (sourceTokens.isEmpty()) return 0;

        Set<String> answerTokens = new HashSet<>(tokenize(answer));
        answerTokens.removeIf(t -> t.length() < 2);
        if (answerTokens.isEmpty()) return 0;

        long hit = answerTokens.stream().filter(sourceTokens::contains).count();
        return (double) hit / answerTokens.size();
    }

    private List<String> tokenize(String text) {
        if (text == null) return Collections.emptyList();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[\\s\\p{Punct}，。！？、；：]+"))
                .filter(t -> t.length() >= 2)
                .collect(Collectors.toList());
    }
}
