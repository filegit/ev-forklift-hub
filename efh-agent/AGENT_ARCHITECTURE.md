# Agent 增强架构说明（15 项能力实现索引）

| # | 能力 | 实现类 | 说明 |
|---|------|--------|------|
| 1 | 多轮上下文存储 | `ConversationContextStore` | Redis 短期 + MySQL 长期，Redisson 分布式锁 |
| 2 | 并发优化 | `AgentExecutorConfig`, `AgentRateLimitService`, `parallelRetrieve` | 线程池并行 RAG + Redis 限流 |
| 3 | Function Call | `AgentTool`, `ToolRegistry`, `ToolExecutor` | OpenAI tools 格式 + 工具鉴权 |
| 4 | 多 Agent 协作 | `AgentOrchestrator` | Safety→Planner→RAG→Tool→LLM→Guard→Memory |
| 5 | 长短期记忆 | `ConversationContextStore`, `ContextCompressionService` | summary 长期 + messages 短期 |
| 6 | 向量数据库 | `VectorStoreService` + `agent_vector_index` 表 | Embedding API + 本地 fallback |
| 7 | 任务规划 | `PlannerAgent` | 复合问题拆步骤 |
| 8 | 安全 | `PromptSafetyService`, `ContentSafetyService`, `ToolExecutor` | 注入拦截、工具黑名单、输出过滤 |
| 9 | SSE 流式 | `LlmClient.streamAnswer`, `POST /api/agent/chat/stream` | text/event-stream |
| 10 | 链路日志 | `AgentExecutionLogService` → `agent_execution_log` | traceId 逐步骤埋点 |
| 11 | 监控成本 | `AgentMetricsService`, `GET /api/agent/metrics` | QPS/耗时/Token/元 |
| 12 | 分布式会话 | Redis key `agent:ctx:{userId}:{sessionId}` + 锁 | 多实例一致 |
| 13 | 重排阈值 | `RerankService` | 综合分过滤 + TopK |
| 14 | 防幻觉 | `HallucinationGuardService` | 关键词 grounding 重叠率 |
| 15 | 上下文压缩 | `ContextCompressionService` | 超 token 阈值 LLM 摘要 |

## 数据库初始化

```bash
docker exec -i efh-mysql mysql -uroot -p123456 < docker/mysql/init/06-agent.sql
```

## 新接口

- `POST /api/agent/chat` — 同步，返回 sessionId/traceId/token 成本
- `POST /api/agent/chat/stream` — SSE 流式
- `GET /api/agent/metrics` — 监控快照

## 客户端多轮

请求体增加 `sessionId`，首次为空服务端自动生成，后续原样带回。
