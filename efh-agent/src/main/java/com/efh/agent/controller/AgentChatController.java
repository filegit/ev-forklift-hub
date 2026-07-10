package com.efh.agent.controller;

import com.efh.agent.service.AgentChatService;
import com.efh.agent.service.monitor.AgentMetricsService;
import com.efh.agent.service.security.RequireSafety;
import com.efh.agent.vo.ChatHistoryVO;
import com.efh.agent.vo.ChatRequestVO;
import com.efh.agent.vo.ChatResponseVO;
import com.efh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @RequireSafety
    public Result<ChatResponseVO> chat(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @Validated @RequestBody ChatRequestVO request) {
        Long userId = parseUserId(userIdHeader);
        log.info("Agent chat: userId={}, sessionId={}, scope={}", userId, request.getSessionId(), request.getScope());
        return Result.success(agentChatService.chat(userId, request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireSafety
    public SseEmitter chatStream(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                 @Validated @RequestBody ChatRequestVO request) {
        Long userId = parseUserId(userIdHeader);
        return agentChatService.chatStream(userId, request);
    }

    @GetMapping("/chat/history/{sessionId}")
    public Result<ChatHistoryVO> history(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                         @PathVariable String sessionId) {
        Long userId = parseUserId(userIdHeader);
        return Result.success(agentChatService.history(userId, sessionId));
    }

    @GetMapping("/metrics")
    public Result<AgentMetricsService.AgentMetricsSnapshot> metrics() {
        return Result.success(agentMetricsService.snapshot());
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI Agent service is running: RAG + tools + memory + SSE + guardrails + observability.");
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
