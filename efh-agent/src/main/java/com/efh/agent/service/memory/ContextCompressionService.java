package com.efh.agent.service.memory;

import com.efh.agent.config.AgentProperties;
import com.efh.agent.model.ChatMessage;
import com.efh.agent.model.ConversationContext;
import com.efh.agent.model.TokenUsage;
import com.efh.agent.service.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 长对话上下文摘要压缩（问题15）
 * 当历史 token 超过 contextTokenLimit 时，将较早消息压缩为 summary 写入长期记忆
 */
@Slf4j
@Service
public class ContextCompressionService {

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private ConversationContextStore contextStore;

    public ConversationContext compressIfNeeded(ConversationContext ctx) {
        if (ctx == null || ctx.getMessages().isEmpty()) {
            return ctx;
        }
        int totalTokens = ctx.getMessages().stream().mapToInt(ChatMessage::getTokenCount).sum();
        if (totalTokens <= agentProperties.getContextTokenLimit()) {
            return ctx;
        }

        List<ChatMessage> messages = ctx.getMessages();
        int keepRecent = Math.max(4, agentProperties.getShortTermMaxMessages() / 2);
        if (messages.size() <= keepRecent) {
            return ctx;
        }

        List<ChatMessage> oldPart = messages.subList(0, messages.size() - keepRecent);
        List<ChatMessage> recentPart = new ArrayList<>(messages.subList(messages.size() - keepRecent, messages.size()));

        String oldDialog = oldPart.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String newSummary = llmClient.summarizeDialog(oldDialog, ctx.getSummary());
        ctx.setSummary(newSummary);
        ctx.setMessages(recentPart);
        contextStore.updateSummary(ctx.getUserId(), ctx.getSessionId(), newSummary);
        log.info("上下文已压缩: sessionId={}, 保留最近{}条, summaryLen={}", ctx.getSessionId(), keepRecent, newSummary.length());
        return ctx;
    }

    /** 构建送入 LLM 的消息列表（含长期摘要 + 短期多轮） */
    public List<ChatMessage> buildLlmHistory(ConversationContext ctx) {
        List<ChatMessage> history = new ArrayList<>();
        if (ctx.getSummary() != null && !ctx.getSummary().trim().isEmpty()) {
            history.add(ChatMessage.of("system", "【历史对话摘要】" + ctx.getSummary(), estimateTokens(ctx.getSummary())));
        }
        history.addAll(ctx.getMessages());
        return history;
    }

    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return Math.max(1, text.length() / 2);
    }
}
