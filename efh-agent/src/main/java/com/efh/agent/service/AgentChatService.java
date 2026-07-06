package com.efh.agent.service;

import com.efh.agent.service.agent.AgentOrchestrator;
import com.efh.agent.vo.ChatRequestVO;
import com.efh.agent.vo.ChatResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 对外 Chat 门面，委托多 Agent 编排器 */
@Service
public class AgentChatService {

    @Autowired
    private AgentOrchestrator agentOrchestrator;

    public ChatResponseVO chat(Long userId, ChatRequestVO request) {
        return agentOrchestrator.orchestrate(userId, request);
    }

    public SseEmitter chatStream(Long userId, ChatRequestVO request) {
        return agentOrchestrator.orchestrateStream(userId, request);
    }
}
