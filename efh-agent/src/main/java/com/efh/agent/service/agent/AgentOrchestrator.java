package com.efh.agent.service.agent;

import com.efh.agent.model.ChatMessage;
import com.efh.agent.model.ConversationContext;
import com.efh.agent.model.RagChunk;
import com.efh.agent.model.TokenUsage;
import com.efh.agent.service.DocumentTextService;
import com.efh.agent.service.LlmClient;
import com.efh.agent.service.RagRetrievalService;
import com.efh.agent.service.concurrent.AgentRateLimitService;
import com.efh.agent.service.memory.ContextCompressionService;
import com.efh.agent.service.memory.ConversationContextStore;
import com.efh.agent.service.monitor.AgentExecutionLogService;
import com.efh.agent.service.monitor.AgentMetricsService;
import com.efh.agent.service.rag.HallucinationGuardService;
import com.efh.agent.service.security.ContentSafetyService;
import com.efh.agent.service.security.PromptSafetyService;
import com.efh.agent.service.tool.ToolContext;
import com.efh.agent.service.tool.ToolExecutor;
import com.efh.agent.vo.ChatRequestVO;
import com.efh.agent.vo.ChatResponseVO;
import com.efh.agent.vo.ChatSourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 多 Agent 协作编排器（问题4/7）
 * SafetyAgent → PlannerAgent → RagAgent(+Tool) → LlmAgent → HallucinationGuard → MemoryAgent
 */
@Slf4j
@Service
public class AgentOrchestrator {

    @Autowired
    private PromptSafetyService promptSafetyService;
    @Autowired
    private ContentSafetyService contentSafetyService;
    @Autowired
    private AgentRateLimitService rateLimitService;
    @Autowired
    private PlannerAgent plannerAgent;
    @Autowired
    private RagRetrievalService ragRetrievalService;
    @Autowired
    private LlmClient llmClient;
    @Autowired
    private HallucinationGuardService hallucinationGuardService;
    @Autowired
    private ConversationContextStore contextStore;
    @Autowired
    private ContextCompressionService compressionService;
    @Autowired
    private AgentExecutionLogService executionLogService;
    @Autowired
    private AgentMetricsService metricsService;
    @Autowired
    private ToolExecutor toolExecutor;
    @Autowired
    private DocumentTextService documentTextService;
    @Autowired
    @Qualifier("agentTaskExecutor")
    private Executor agentTaskExecutor;

    public ChatResponseVO orchestrate(Long userId, ChatRequestVO request) {
        long start = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = contextStore.resolveSessionId(request.getSessionId());
        ToolContext toolCtx = buildToolContext(userId, sessionId, traceId, request.getScope(), false);
        executionLogService.resetSteps();
        TokenUsage totalUsage = TokenUsage.empty();

        try {
            // Step1 SafetyAgent
            long t1 = System.currentTimeMillis();
            promptSafetyService.validateUserInput(request.getQuestion());
            rateLimitService.checkRateLimit(userId);
            executionLogService.logStep(toolCtx, "SAFETY", "SafetyAgent", null, request.getQuestion(), "OK", System.currentTimeMillis() - t1, true, null);

            // Step2 加载多轮上下文 + 压缩（问题1/5/15）
            ConversationContext ctx = contextStore.load(userId, sessionId);
            ctx = compressionService.compressIfNeeded(ctx);
            List<ChatMessage> history = compressionService.buildLlmHistory(ctx);

            // Step3 PlannerAgent（问题7）
            List<String> plan = plannerAgent.plan(request.getQuestion());
            executionLogService.logStep(toolCtx, "PLAN", "PlannerAgent", null, request.getQuestion(), plan.toString(), 0, true, null);

            // Step4 RagAgent 并行检索（问题2/6/13）
            String scope = request.getScope() == null ? "all" : request.getScope();
            long t4 = System.currentTimeMillis();
            List<RagChunk> chunks = parallelRetrieve(request.getQuestion(), userId, scope, plan);
            executionLogService.logStep(toolCtx, "RAG", "RagAgent", null, request.getQuestion(), "chunks=" + chunks.size(), System.currentTimeMillis() - t4, true, null);

            // Step5 ToolAgent 可选增强（问题3）
            if (plan.contains("TOOL_ENRICH") || plan.contains("TOOL_KNOWLEDGE_SEARCH")) {
                enrichWithTools(toolCtx, request.getQuestion(), chunks);
            }

            // Step6 LlmAgent
            long t6 = System.currentTimeMillis();
            LlmClient.ChatResult chatResult = llmClient.generateAnswer(request.getQuestion(), chunks, history);
            totalUsage.add(chatResult.getUsage());
            executionLogService.logTokenUsage(toolCtx, chatResult.getUsage());
            executionLogService.logStep(toolCtx, "LLM", "LlmAgent", null, request.getQuestion(), chatResult.getAnswer(), System.currentTimeMillis() - t6, true, null);

            // Step7 HallucinationGuard + ContentSafety（问题14/8）
            String answer = hallucinationGuardService.guard(request.getQuestion(), chatResult.getAnswer(), chunks, chatResult.isLlmUsed());
            answer = contentSafetyService.sanitizeOutput(answer);

            // Step8 MemoryAgent 持久化多轮（问题1/12）
            int qTokens = compressionService.estimateTokens(request.getQuestion());
            int aTokens = compressionService.estimateTokens(answer);
            contextStore.appendMessage(userId, sessionId, ChatMessage.of("user", request.getQuestion(), qTokens));
            contextStore.appendMessage(userId, sessionId, ChatMessage.of("assistant", answer, aTokens));

            ChatResponseVO response = buildResponse(answer, chatResult.isLlmUsed(), chunks, sessionId, traceId, totalUsage);
            metricsService.recordRequest(System.currentTimeMillis() - start, true, totalUsage);
            return response;
        } catch (Exception e) {
            metricsService.recordRequest(System.currentTimeMillis() - start, false, totalUsage);
            executionLogService.logStep(toolCtx, "ERROR", "Orchestrator", null, request.getQuestion(), e.getMessage(), System.currentTimeMillis() - start, false, e.getMessage());
            throw e;
        }
    }

    public SseEmitter orchestrateStream(Long userId, ChatRequestVO request) {
        SseEmitter emitter = new SseEmitter(agentPropertiesTimeout());
        String sessionId = contextStore.resolveSessionId(request.getSessionId());
        CompletableFuture.runAsync(() -> {
            try {
                promptSafetyService.validateUserInput(request.getQuestion());
                rateLimitService.checkRateLimit(userId);
                ConversationContext ctx = compressionService.compressIfNeeded(contextStore.load(userId, sessionId));
                List<ChatMessage> history = compressionService.buildLlmHistory(ctx);
                String scope = request.getScope() == null ? "all" : request.getScope();
                List<RagChunk> chunks = ragRetrievalService.retrieve(request.getQuestion(), userId, scope);
                llmClient.streamAnswer(request.getQuestion(), chunks, history, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, agentTaskExecutor);
        return emitter;
    }

    @Autowired
    private com.efh.agent.config.AgentProperties agentProperties;

    private long agentPropertiesTimeout() {
        return agentProperties.getLlm().getTimeoutMs() + 10000L;
    }

    private List<RagChunk> parallelRetrieve(String question, Long userId, String scope, List<String> plan) {
        if (plan.contains("PARALLEL_RAG_ALL")) {
            CompletableFuture<List<RagChunk>> f1 = CompletableFuture.supplyAsync(() -> ragRetrievalService.retrieve(question, userId, "knowledge"), agentTaskExecutor);
            CompletableFuture<List<RagChunk>> f2 = CompletableFuture.supplyAsync(() -> ragRetrievalService.retrieve(question, userId, "community"), agentTaskExecutor);
            List<RagChunk> all = new ArrayList<>();
            all.addAll(f1.join());
            all.addAll(f2.join());
            all.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
            return all.stream().limit(agentProperties.getMaxChunks()).collect(Collectors.toList());
        }
        return ragRetrievalService.retrieve(question, userId, scope);
    }

    private void enrichWithTools(ToolContext ctx, String question, List<RagChunk> chunks) {
        try {
            Map<String, Object> args = Collections.singletonMap("query", question);
            String k = toolExecutor.execute("knowledge_search", ctx, args);
            if (k != null && !k.isEmpty()) {
                RagChunk c = new RagChunk();
                c.setType("tool");
                c.setTitle("工具检索-知识库");
                c.setContent(k);
                c.setScore(0.5);
                c.setUnlocked(true);
                chunks.add(c);
            }
        } catch (Exception e) {
            log.debug("工具增强跳过: {}", e.getMessage());
        }
    }

    private ToolContext buildToolContext(Long userId, String sessionId, String traceId, String scope, boolean admin) {
        ToolContext ctx = new ToolContext();
        ctx.setUserId(userId);
        ctx.setSessionId(sessionId);
        ctx.setTraceId(traceId);
        ctx.setScope(scope);
        ctx.setAdmin(admin);
        return ctx;
    }

    private ChatResponseVO buildResponse(String answer, boolean llmUsed, List<RagChunk> chunks,
                                         String sessionId, String traceId, TokenUsage usage) {
        ChatResponseVO response = new ChatResponseVO();
        response.setAnswer(answer);
        response.setLlmUsed(llmUsed);
        response.setSessionId(sessionId);
        response.setTraceId(traceId);
        response.setPromptTokens(usage.getPromptTokens());
        response.setCompletionTokens(usage.getCompletionTokens());
        response.setCostYuan(usage.getCostYuan());
        for (RagChunk chunk : chunks) {
            ChatSourceVO source = new ChatSourceVO();
            source.setType(chunk.getType());
            source.setId(chunk.getId());
            source.setTitle(chunk.getTitle());
            source.setSnippet(documentTextService.truncate(chunk.getContent(), 160));
            source.setUnlocked(chunk.isUnlocked());
            source.setScore(chunk.getScore());
            source.setLink(buildLink(chunk));
            response.getSources().add(source);
        }
        return response;
    }

    private String buildLink(RagChunk chunk) {
        if ("knowledge".equals(chunk.getType())) return "/knowledge/" + chunk.getId();
        if ("post".equals(chunk.getType())) return "/post/" + chunk.getId();
        return "/";
    }
}
