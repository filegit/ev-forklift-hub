package com.efh.agent.service;

import com.efh.agent.service.agent.AgentOrchestrator;
import com.efh.agent.service.memory.ConversationContextStore;
import com.efh.agent.vo.ChatRequestVO;
import com.efh.agent.vo.ChatHistoryVO;
import com.efh.agent.vo.ChatResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 对外 Chat 门面，委托多 Agent 编排器 */
@Service
public class AgentChatService {

    @Autowired
    private AgentOrchestrator agentOrchestrator;
    @Autowired
    private ConversationContextStore contextStore;

    public ChatResponseVO chat(Long userId, ChatRequestVO request) {
        return agentOrchestrator.orchestrate(userId, request);
    }

    public SseEmitter chatStream(Long userId, ChatRequestVO request) {
        return agentOrchestrator.orchestrateStream(userId, request);
    }

    public ChatHistoryVO history(Long userId, String sessionId) {
        com.efh.agent.model.ConversationContext ctx = contextStore.load(userId, sessionId);
        ChatHistoryVO vo = new ChatHistoryVO();
        vo.setSessionId(ctx.getSessionId());
        vo.setSummary(ctx.getSummary());
        vo.setMessages(ctx.getMessages());
        return vo;
    }
}
