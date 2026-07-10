package com.efh.agent.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent planner.
 *
 * The planner converts one user question into deterministic execution steps.
 * The LLM can still synthesize the final answer, but the business route is
 * controlled by this layer so the system is explainable and testable.
 */
@Slf4j
@Service
public class PlannerAgent {

    public List<String> plan(String question) {
        return plan(question, "GENERAL_QA");
    }

    public List<String> plan(String question, String intent) {
        List<String> steps = new ArrayList<>();
        String q = question == null ? "" : question;

        boolean needKnowledge = containsAny(q, "知识库", "文档", "手册", "规范", "培训", "故障", "保养");
        boolean needCommunity = containsAny(q, "社区", "帖子", "评论", "讨论", "网友", "经验");
        boolean needCompare = containsAny(q, "对比", "比较", "哪个好", "步骤", "怎么办", "如何", "怎么");

        steps.add("SAFETY_CHECK");
        if ("ORDER_QUERY".equals(intent)) {
            steps.add("TOOL_ORDER_QUERY");
        } else if ("TICKET_CREATE".equals(intent)) {
            steps.add("TOOL_SERVICE_TICKET_CREATE");
            steps.add("RAG_KNOWLEDGE");
        } else if ("USER_PROFILE".equals(intent)) {
            steps.add("TOOL_USER_PROFILE");
        } else if ("HANDOFF".equals(intent)) {
            steps.add("HANDOFF_TO_HUMAN");
        } else if ("MULTIMODAL_DIAGNOSIS".equals(intent)) {
            steps.add("VISION_ANALYSIS");
            steps.add("RAG_KNOWLEDGE");
        } else if (needKnowledge && needCommunity) {
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
        log.debug("Agent plan: question={}, intent={}, steps={}", question, intent, steps);
        return steps;
    }

    private boolean containsAny(String q, String... keys) {
        for (String k : keys) {
            if (q.contains(k)) return true;
        }
        return false;
    }
}
