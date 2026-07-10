package com.efh.agent.service.agent;

import lombok.Data;
import org.springframework.stereotype.Service;

/**
 * Lightweight intent classifier for the forklift AI assistant.
 *
 * It keeps the project usable without an LLM, while still giving the Agent
 * engine a clear route: knowledge QA, order query, ticket creation, handoff,
 * user profile, or multimodal diagnosis.
 */
@Service
public class IntentClassifier {

    public IntentResult classify(String question, boolean hasImage) {
        String q = question == null ? "" : question.trim().toLowerCase();
        if (hasImage) {
            return new IntentResult("MULTIMODAL_DIAGNOSIS", 0.90);
        }
        if (containsAny(q, "人工", "客服", "转人工", "联系售后", "投诉", "human", "service")) {
            return new IntentResult("HANDOFF", 0.88);
        }
        if (containsAny(q, "工单", "报修", "维修预约", "创建维修", "上门维修", "故障申报", "ticket")) {
            return new IntentResult("TICKET_CREATE", 0.86);
        }
        if (containsAny(q, "订单", "物流", "快递", "发货", "运单", "支付状态", "po", "pay", "order")) {
            return new IntentResult("ORDER_QUERY", 0.84);
        }
        if (containsAny(q, "我的信息", "用户信息", "会员", "等级", "历史问题", "profile")) {
            return new IntentResult("USER_PROFILE", 0.78);
        }
        if (containsAny(q, "手册", "知识库", "文档", "故障", "怎么", "如何", "原因", "维修", "保养", "电池", "叉车")) {
            return new IntentResult("KNOWLEDGE_QA", 0.80);
        }
        return new IntentResult("GENERAL_QA", 0.60);
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    @Data
    public static class IntentResult {
        private final String intent;
        private final double confidence;
    }
}
