package com.efh.agent.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 复杂任务规划 Agent（问题7）
 * 规则 + 关键词：将复合问题拆成 RAG → Tool → 综合 三步
 */
@Slf4j
@Service
public class PlannerAgent {

    public List<String> plan(String question) {
        List<String> steps = new ArrayList<>();
        if (question == null) return steps;

        boolean needKnowledge = containsAny(question, "知识库", "文档", "手册", "规范", "培训");
        boolean needCommunity = containsAny(question, "社区", "帖子", "评论", "讨论", "网友");
        boolean needCompare = containsAny(question, "对比", "比较", "哪个好", "步骤", "怎么办", "如何");

        steps.add("SAFETY_CHECK");
        if (needKnowledge && needCommunity) {
            steps.add("PARALLEL_RAG_ALL");
            steps.add("TOOL_KNOWLEDGE_SEARCH");
            steps.add("TOOL_COMMUNITY_SEARCH");
        } else if (needKnowledge) {
            steps.add("RAG_KNOWLEDGE");
        } else if (needCommunity) {
            steps.add("RAG_COMMUNITY");
        } else {
            steps.add("RAG_ALL");
        }
        if (needCompare) {
            steps.add("TOOL_ENRICH");
        }
        steps.add("LLM_SYNTHESIS");
        steps.add("HALLUCINATION_GUARD");
        steps.add("SAVE_MEMORY");
        log.debug("任务规划: question={}, steps={}", question, steps);
        return steps;
    }

    private boolean containsAny(String q, String... keys) {
        for (String k : keys) {
            if (q.contains(k)) return true;
        }
        return false;
    }
}
