package com.efh.agent.controller;

import com.efh.agent.service.AgentChatService;
import com.efh.agent.service.monitor.AgentMetricsService;
import com.efh.agent.vo.ChatRequestVO;
import com.efh.agent.vo.ChatResponseVO;
import com.efh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    @Autowired
    private AgentChatService agentChatService;

    @Autowired
    private AgentMetricsService agentMetricsService;

    @PostMapping("/chat")
    public Result<ChatResponseVO> chat(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @Validated @RequestBody ChatRequestVO request) {
        Long userId = parseUserId(userIdHeader);
        log.info("Agent 问答: userId={}, sessionId={}, scope={}", userId, request.getSessionId(), request.getScope());
        return Result.success(agentChatService.chat(userId, request));
    }

    /** SSE 流式输出（问题9） */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                   @Validated @RequestBody ChatRequestVO request) {
        Long userId = parseUserId(userIdHeader);
        return agentChatService.chatStream(userId, request);
    }

    /** 监控指标：QPS、耗时、Token 成本（问题11） */
    @GetMapping("/metrics")
    public Result<AgentMetricsService.AgentMetricsSnapshot> metrics() {
        return Result.success(agentMetricsService.snapshot());
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI Agent 服务运行正常（增强版：RAG+多Agent+SSE+向量+记忆）");
    }

    private Long parseUserId(String header) {
        if (header == null || header.trim().isEmpty()) return null;
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
