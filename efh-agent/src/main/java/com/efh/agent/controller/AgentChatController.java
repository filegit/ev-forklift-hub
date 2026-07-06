package com.efh.agent.controller;

import com.efh.agent.service.AgentChatService;
import com.efh.agent.vo.ChatRequestVO;
import com.efh.agent.vo.ChatResponseVO;
import com.efh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    @Autowired
    private AgentChatService agentChatService;

    @PostMapping("/chat")
    public Result<ChatResponseVO> chat(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @Validated @RequestBody ChatRequestVO request) {
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        log.info("Agent 问答: userId={}, scope={}, question={}", userId, request.getScope(), request.getQuestion());
        return Result.success(agentChatService.chat(userId, request));
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI Agent 服务运行正常");
    }
}
