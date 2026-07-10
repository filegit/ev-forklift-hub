package com.efh.agent.service.agent;

import com.efh.agent.config.AgentProperties;
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
import com.efh.agent.service.monitor.LangfuseTraceService;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Enterprise Agent engine:
 * intent -> safety -> memory -> plan -> RAG -> tools -> LLM -> guardrail -> trace.
 */
@Slf4j
@Service
public class AgentOrchestrator {

    @Autowired
    private AgentProperties agentProperties;
    @Autowired
    private PromptSafetyService promptSafetyService;
    @Autowired
    private ContentSafetyService contentSafetyService;
    @Autowired
    private AgentRateLimitService rateLimitService;
    @Autowired
    private PlannerAgent plannerAgent;
    @Autowired
    private IntentClassifier intentClassifier;
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
    private LangfuseTraceService langfuseTraceService;
    @Autowired
    private ToolExecutor toolExecutor;
    @Autowired
    private DocumentTextService documentTextService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    @Qualifier("agentTaskExecutor")
    private Executor agentTaskExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponseVO orchestrate(Long userId, ChatRequestVO request) {
        long start = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = contextStore.resolveSessionId(request.getSessionId());
        ToolContext toolCtx = buildToolContext(userId, sessionId, traceId, request.getScope(), false);
        executionLogService.resetSteps();
        TokenUsage totalUsage = TokenUsage.empty();
        List<String> stageEvents = new ArrayList<>();
        List<String> toolCalls = new ArrayList<>();

        try {
            IntentClassifier.IntentResult intent = intentClassifier.classify(request.getQuestion(), hasImages(request));
            addStage(stageEvents, "分析中", "识别意图: " + intent.getIntent());

            ChatResponseVO cached = loadCachedResponse(userId, request, sessionId, traceId);
            if (cached != null) {
                cached.setIntent(intent.getIntent());
                cached.setIntentConfidence(intent.getConfidence());
                cached.getStageEvents().addAll(0, stageEvents);
                metricsService.recordRequest(System.currentTimeMillis() - start, true, totalUsage);
                return cached;
            }

            long t1 = System.currentTimeMillis();
            promptSafetyService.validateUserInput(request.getQuestion());
            rateLimitService.checkRateLimit(userId);
            executionLogService.logStep(toolCtx, "SAFETY", "SafetyAgent", null, request.getQuestion(), "OK", System.currentTimeMillis() - t1, true, null);
            addStage(stageEvents, "安全检查", "输入通过安全护栏");

            ConversationContext ctx = contextStore.load(userId, sessionId);
            ctx = compressionService.compressIfNeeded(ctx);
            List<ChatMessage> history = compressionService.buildLlmHistory(ctx);
            addStage(stageEvents, "读取记忆", "已加载上下文 " + history.size() + " 条");

            List<String> plan = plannerAgent.plan(request.getQuestion(), intent.getIntent());
            executionLogService.logStep(toolCtx, "PLAN", "PlannerAgent", null, request.getQuestion(), plan.toString(), 0, true, null);
            addStage(stageEvents, "规划步骤", String.join(" -> ", plan));

            String scope = request.getScope() == null ? "all" : request.getScope();
            long t4 = System.currentTimeMillis();
            List<RagChunk> chunks = parallelRetrieve(request.getQuestion(), userId, scope, plan);
            executionLogService.logStep(toolCtx, "RAG", "RagAgent", null, request.getQuestion(), "chunks=" + chunks.size(), System.currentTimeMillis() - t4, true, null);
            addStage(stageEvents, "检索中", "找到 " + chunks.size() + " 条候选资料");

            if (hasImages(request)) {
                chunks.add(buildImageChunk(request));
                addStage(stageEvents, "图片理解", agentProperties.getMultimodal().isEnabled()
                        ? "已接收图片，进入多模态诊断链路"
                        : "已接收图片，当前以图片URL和用户描述作为诊断上下文");
            }

            executePlannedTools(toolCtx, request, plan, chunks, toolCalls, stageEvents);

            long t6 = System.currentTimeMillis();
            addStage(stageEvents, "生成中", "LLM 正在基于资料和工具结果生成回答");
            LlmClient.ChatResult chatResult = llmClient.generateAnswer(request.getQuestion(), chunks, history);
            totalUsage.add(chatResult.getUsage());
            executionLogService.logTokenUsage(toolCtx, chatResult.getUsage());
            executionLogService.logStep(toolCtx, "LLM", "LlmAgent", null, request.getQuestion(), chatResult.getAnswer(), System.currentTimeMillis() - t6, true, null);

            String answer = hallucinationGuardService.guard(request.getQuestion(), chatResult.getAnswer(), chunks, chatResult.isLlmUsed());
            answer = appendHandoffHintIfNeeded(answer, intent.getIntent());
            answer = contentSafetyService.sanitizeOutput(answer);
            addStage(stageEvents, "安全复核", "完成事实一致性和输出脱敏检查");

            int qTokens = compressionService.estimateTokens(request.getQuestion());
            int aTokens = compressionService.estimateTokens(answer);
            contextStore.appendMessage(userId, sessionId, ChatMessage.of("user", request.getQuestion(), qTokens));
            contextStore.appendMessage(userId, sessionId, ChatMessage.of("assistant", answer, aTokens));

            ChatResponseVO response = buildResponse(answer, chatResult.isLlmUsed(), chunks, sessionId, traceId, totalUsage);
            response.setIntent(intent.getIntent());
            response.setIntentConfidence(intent.getConfidence());
            response.getPlan().addAll(plan);
            response.getStageEvents().addAll(stageEvents);
            response.getToolCalls().addAll(toolCalls);
            cacheResponse(userId, request, response);
            metricsService.recordRequest(System.currentTimeMillis() - start, true, totalUsage);
            langfuseTraceService.trace(userId, request.getQuestion(), response, System.currentTimeMillis() - start);
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
            long start = System.currentTimeMillis();
            String traceId = UUID.randomUUID().toString().replace("-", "");
            ToolContext toolCtx = buildToolContext(userId, sessionId, traceId, request.getScope(), false);
            TokenUsage usage = TokenUsage.empty();
            List<String> stageEvents = new ArrayList<>();
            List<String> toolCalls = new ArrayList<>();
            try {
                IntentClassifier.IntentResult intent = intentClassifier.classify(request.getQuestion(), hasImages(request));
                addStage(stageEvents, "分析中", "识别意图: " + intent.getIntent());
                sendStage(emitter, "分析中", "正在识别意图");
                sendStage(emitter, "意图识别", intent.getIntent());
                promptSafetyService.validateUserInput(request.getQuestion());
                rateLimitService.checkRateLimit(userId);
                addStage(stageEvents, "安全检查", "输入通过安全护栏");
                sendStage(emitter, "安全检查", "输入通过安全护栏");

                ConversationContext ctx = compressionService.compressIfNeeded(contextStore.load(userId, sessionId));
                List<ChatMessage> history = compressionService.buildLlmHistory(ctx);
                List<String> plan = plannerAgent.plan(request.getQuestion(), intent.getIntent());
                addStage(stageEvents, "规划步骤", String.join(" -> ", plan));
                sendStage(emitter, "规划步骤", String.join(" -> ", plan));

                String scope = request.getScope() == null ? "all" : request.getScope();
                List<RagChunk> chunks = parallelRetrieve(request.getQuestion(), userId, scope, plan);
                addStage(stageEvents, "检索中", "找到 " + chunks.size() + " 条资料");
                if (hasImages(request)) {
                    chunks.add(buildImageChunk(request));
                    addStage(stageEvents, "图片理解", "已接收图片上下文");
                    sendStage(emitter, "图片理解", "已接收图片上下文");
                }
                executePlannedTools(toolCtx, request, plan, chunks, toolCalls, stageEvents);
                sendStage(emitter, "检索中", "找到 " + chunks.size() + " 条资料");
                sendStage(emitter, "生成中", "开始流式生成回答");
                String answer = llmClient.streamAnswer(request.getQuestion(), chunks, history, emitter);
                answer = hallucinationGuardService.guard(request.getQuestion(), answer, chunks, agentProperties.isLlmReady());
                answer = appendHandoffHintIfNeeded(answer, intent.getIntent());
                answer = contentSafetyService.sanitizeOutput(answer);

                contextStore.appendMessage(userId, sessionId, ChatMessage.of("user", request.getQuestion(), compressionService.estimateTokens(request.getQuestion())));
                contextStore.appendMessage(userId, sessionId, ChatMessage.of("assistant", answer, compressionService.estimateTokens(answer)));

                ChatResponseVO meta = buildResponse(answer, agentProperties.isLlmReady(), chunks, sessionId, traceId, usage);
                meta.setIntent(intent.getIntent());
                meta.setIntentConfidence(intent.getConfidence());
                meta.getPlan().addAll(plan);
                meta.getStageEvents().addAll(stageEvents);
                meta.getToolCalls().addAll(toolCalls);
                metricsService.recordRequest(System.currentTimeMillis() - start, true, usage);
                langfuseTraceService.trace(userId, request.getQuestion(), meta, System.currentTimeMillis() - start);

                emitter.send(SseEmitter.event().name("meta").data(objectMapper.writeValueAsString(meta)));
                emitter.send(SseEmitter.event().name("done").data(answer));
                emitter.complete();
            } catch (Exception e) {
                metricsService.recordRequest(System.currentTimeMillis() - start, false, usage);
                emitter.completeWithError(e);
            }
        }, agentTaskExecutor);
        return emitter;
    }

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

    private void executePlannedTools(ToolContext ctx, ChatRequestVO request, List<String> plan,
                                     List<RagChunk> chunks, List<String> toolCalls, List<String> stageEvents) {
        if (plan.contains("TOOL_ORDER_QUERY")) {
            addToolResult(ctx, "order_query", extractOrderArgs(request.getQuestion()), chunks, toolCalls, stageEvents);
        }
        if (plan.contains("TOOL_SERVICE_TICKET_CREATE")) {
            Map<String, Object> args = new HashMap<>();
            args.put("query", request.getQuestion());
            if (Boolean.TRUE.equals(request.getAllowCreateTicket())) {
                addToolResult(ctx, "service_ticket_create", args, chunks, toolCalls, stageEvents);
            } else {
                addStage(stageEvents, "工单确认", "识别到工单意图，需要用户确认并补充电话、地址后再创建");
            }
        }
        if (plan.contains("TOOL_USER_PROFILE")) {
            addToolResult(ctx, "user_profile_query", Collections.emptyMap(), chunks, toolCalls, stageEvents);
        }
        if (plan.contains("TOOL_ENRICH") || plan.contains("TOOL_KNOWLEDGE_SEARCH")) {
            addToolResult(ctx, "knowledge_search", Collections.singletonMap("query", request.getQuestion()), chunks, toolCalls, stageEvents);
        }
        if (plan.contains("TOOL_COMMUNITY_SEARCH")) {
            addToolResult(ctx, "community_search", Collections.singletonMap("query", request.getQuestion()), chunks, toolCalls, stageEvents);
        }
    }

    private void addToolResult(ToolContext ctx, String toolName, Map<String, Object> args,
                               List<RagChunk> chunks, List<String> toolCalls, List<String> stageEvents) {
        long start = System.currentTimeMillis();
        try {
            String result = toolExecutor.execute(toolName, ctx, args);
            toolCalls.add(toolName + "(" + (System.currentTimeMillis() - start) + "ms)");
            addStage(stageEvents, "工具调用", toolName + " 执行完成");
            if (result != null && !result.trim().isEmpty()) {
                RagChunk c = new RagChunk();
                c.setType("tool");
                c.setTitle("工具结果: " + toolName);
                c.setContent(result);
                c.setScore(0.65);
                c.setUnlocked(true);
                chunks.add(c);
            }
        } catch (Exception e) {
            toolCalls.add(toolName + "(failed)");
            addStage(stageEvents, "工具异常", toolName + ": " + e.getMessage());
            RagChunk c = new RagChunk();
            c.setType("tool_error");
            c.setTitle("工具异常: " + toolName);
            c.setContent(e.getMessage());
            c.setScore(0.2);
            c.setUnlocked(true);
            chunks.add(c);
        }
    }

    private boolean hasImages(ChatRequestVO request) {
        return request.getImageUrls() != null && !request.getImageUrls().isEmpty();
    }

    private RagChunk buildImageChunk(ChatRequestVO request) {
        RagChunk c = new RagChunk();
        c.setType("image");
        c.setTitle("用户上传图片");
        c.setContent("用户上传了图片URL: " + request.getImageUrls()
                + "。请结合图片说明、叉车故障现象和知识库资料进行诊断。multimodalEnabled="
                + agentProperties.getMultimodal().isEnabled());
        c.setScore(0.70);
        c.setUnlocked(true);
        return c;
    }

    private Map<String, Object> extractOrderArgs(String question) {
        Map<String, Object> args = new HashMap<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(PO\\d{6,}|PAY\\d{6,}|ORD\\d{6,})", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(question == null ? "" : question);
        if (matcher.find()) {
            args.put("orderNo", matcher.group(1));
        } else {
            args.put("query", question);
        }
        return args;
    }

    private String appendHandoffHintIfNeeded(String answer, String intent) {
        if (!"HANDOFF".equals(intent)) {
            return answer;
        }
        return answer + "\n\n我已经识别到你需要人工协助。当前系统会保留本轮上下文，后续可以接入客服坐席或自动创建售后工单。";
    }

    private void addStage(List<String> stageEvents, String stage, String detail) {
        stageEvents.add(stage + " - " + detail);
    }

    private void sendStage(SseEmitter emitter, String stage, String detail) {
        if (!agentProperties.isStageEventsEnabled()) return;
        try {
            emitter.send(SseEmitter.event().name("stage").data(stage + " - " + detail));
        } catch (Exception ignored) {
        }
    }

    private ChatResponseVO loadCachedResponse(Long userId, ChatRequestVO request, String sessionId, String traceId) {
        if (!agentProperties.getCache().isEnabled() || hasImages(request)) return null;
        String value = stringRedisTemplate.opsForValue().get(cacheKey(userId, request));
        if (value == null || value.isEmpty()) return null;
        ChatResponseVO response = new ChatResponseVO();
        response.setAnswer(value + "\n\n(命中相似问题缓存)");
        response.setLlmUsed(false);
        response.setSessionId(sessionId);
        response.setTraceId(traceId);
        response.getStageEvents().add("缓存命中 - 短时间内相同问题复用结果");
        return response;
    }

    private void cacheResponse(Long userId, ChatRequestVO request, ChatResponseVO response) {
        if (!agentProperties.getCache().isEnabled() || response == null || response.getAnswer() == null || hasImages(request)) return;
        stringRedisTemplate.opsForValue().set(cacheKey(userId, request), response.getAnswer(),
                agentProperties.getCache().getTtlSeconds(), TimeUnit.SECONDS);
    }

    private String cacheKey(Long userId, ChatRequestVO request) {
        String q = request.getQuestion() == null ? "" : request.getQuestion().trim().toLowerCase(Locale.ROOT);
        return "agent:cache:" + (userId == null ? 0 : userId) + ":" + Integer.toHexString(q.hashCode());
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
