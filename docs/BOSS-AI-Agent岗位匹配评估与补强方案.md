# BOSS 直聘 AI Agent 岗位匹配评估与补强方案

> 目标：判断当前 `ev-forklift-hub` 项目对 AI Agent 开发工程师岗位是否够用，并明确需要补强的方向。  
> 结论先说：当前项目已经够投“Java 后端 + AI 应用开发 / AI Agent 应用开发 / 大模型应用工程化”岗位；如果想冲更高薪资的“高级 AI Agent / Agent 架构 / 大模型平台工程”岗位，还需要补强 MCP、评测体系、向量库工程化、LangGraph/Spring AI 生态表达和生产级可观测性。

## 1. 岗位要求抽样结论

结合 BOSS 直聘和大厂岗位描述，AI Agent 相关岗位常见要求可以归纳为 8 类：

| 岗位要求 | 常见描述 | 当前项目覆盖度 |
|---|---|---|
| 后端工程能力 | Java/Python/Golang、接口设计、微服务、高可用、高扩展 | 高 |
| LLM 应用开发 | 接入大模型 API，构建企业级 AI 应用 | 高 |
| RAG 知识库问答 | 文档检索、向量召回、重排、引用溯源 | 中高 |
| Agent 工作流 | Planner、工具调用、业务流程自动化、多步骤任务 | 中高 |
| Tool / Function Calling | 工具 schema、工具执行、鉴权、审计 | 中高 |
| Memory 记忆机制 | 多轮上下文、短期记忆、长期摘要 | 中高 |
| 安全与幻觉治理 | Prompt 注入、权限、grounding、输出过滤 | 中 |
| 工程化与观测 | 日志、trace、指标、成本、限流、部署 | 中高 |
| 生态框架 | LangChain、LangGraph、LlamaIndex、MCP、Spring AI | 中 |
| 模型算法能力 | Transformer、微调、训练、推理优化 | 低到中 |

## 2. 当前项目能打的点

### 2.1 宽度够

当前系统不是一个纯 demo，而是有完整业务闭环：

- 用户、登录、积分、地址。
- 社区、帖子、评论、点赞、收藏。
- 知识库、文档上传、预览、权限、付费解锁。
- 配件商城、购物车、下单、支付宝支付、物流。
- 维修工单。
- AI Agent 助手。
- 前端页面、移动端接口文档、服务器部署。

这对 AI Agent 应用开发岗很重要，因为很多岗位不是让你训练模型，而是把 AI 能力稳定接入业务系统。

### 2.2 AI 深度已经有基础

`efh-agent` 已经包含以下能力：

- LLM 接入：OpenAI 兼容接口，支持 Qwen / DashScope 等。
- RAG：知识库、社区帖子、评论混合检索。
- 权限型 RAG：未解锁知识库文档只参与摘要检索，避免付费内容泄露。
- 向量能力：Embedding API + 本地哈希向量 fallback。
- 重排：关键词分数 + 向量相似度。
- 多 Agent 编排：Safety -> Planner -> RAG -> Tool -> LLM -> Guard -> Memory。
- 工具调用：AgentTool、ToolRegistry、ToolExecutor。
- 多轮记忆：Redis 短期记忆 + MySQL 长期摘要。
- 上下文压缩：超过 token 阈值后摘要旧对话。
- SSE 流式输出。
- 限流：Redis 单用户限流。
- 观测：traceId、执行日志、token 成本、metrics。
- 安全：Prompt 注入拦截、工具黑名单、输出过滤、幻觉提示。

### 2.3 后端可信度强

你当前项目还有后端工程优势：

- Spring Cloud Gateway 统一鉴权和路由。
- MySQL 多业务表设计。
- Redis/Redisson 用于限流、验证码、会话、锁。
- 支付宝支付闭环，包含异步回调和幂等意识。
- Docker/Nginx/Linux 服务器部署和排障经验。

这类内容在面试里非常加分，因为很多 AI Agent 候选人只会 Python demo，不一定能把系统真正上线。

## 3. 当前项目的短板

### 3.1 对普通 AI 应用开发岗：够用

适合投：

- AI 应用开发工程师。
- 大模型应用开发工程师。
- Java AI 应用开发工程师。
- AI Agent 后端开发工程师。
- 企业知识库/RAG 开发工程师。
- AI Copilot / 智能助手开发工程师。

### 3.2 对高级 Agent 架构岗：还差一点

不足点：

1. 没有真正接入生产级向量数据库  
   当前是 MySQL JSON + fallback，能讲原理，但不如 Milvus、pgvector、Elasticsearch dense vector、OpenSearch 更有说服力。

2. MCP 还停留在规划层  
   当前有 ToolRegistry，但没有把工具以 MCP Server 标准暴露出去。

3. Agent 评测体系不足  
   现在有 metrics 和 trace，但缺少离线评测集、命中率、引用准确率、幻觉率、答案质量评分。

4. LangChain / LangGraph / Spring AI 生态表达不足  
   当前是 Java 自研编排，优点是可控，但岗位 JD 经常写 LangChain、LlamaIndex、LangGraph，需要能说明等价设计和迁移方案。

5. 多模态和模型训练能力较弱  
   对需要 OCR、图片理解、微调、推理引擎的岗位匹配度一般。

6. 生产级高可用还要补  
   Nacos、服务保活、Prometheus、Grafana、CI/CD、灰度发布还可以完善。

## 4. 建议补强到项目里的能力

### P0：马上写进简历和面试表达

这些项目已经有，只需要包装好：

- 企业级 RAG：知识库 + 社区 + 权限过滤 + 引用溯源。
- Agent 编排：Safety、Planner、RAG、Tool、LLM、Guard、Memory。
- Tool Calling：工具注册、schema、鉴权、黑名单、审计。
- Memory：Redis 短期 + MySQL 长期摘要 + Redisson 锁。
- 工程化：SSE、限流、trace、成本统计、fallback。
- 业务闭环：支付、订单、物流、维修、知识库解锁。

### P1：建议补文档，面试时可以讲规划

已建议补到文档和简历中的“可扩展方向”：

- MCP Server：把知识库搜索、社区搜索、订单查询、维修工单查询暴露为 MCP tools。
- RAG 评测集：建立 30 到 100 条车企售后问答样本，记录 expected_sources、expected_keywords、risk_level。
- 生产向量库选型：Milvus / pgvector / Elasticsearch dense vector。
- Agent 观测升级：Prometheus + Grafana，指标包括 QPS、P95、token 成本、tool error rate。
- Prompt 版本管理：prompt_template 表，支持版本、灰度、回滚。
- 高风险回答策略：维修安全类问题要求引用资料，不足时拒答或提示人工确认。

### P2：有时间可以真正实现

1. 接入 pgvector 或 Milvus  
   加 `embedding` 表和近似向量检索，替换 MySQL JSON 手算相似度。

2. 实现 MCP Server  
   Java 可以先用 HTTP/SSE 暴露 MCP 风格工具，或者单独用 Node/Python 起 MCP server。

3. 加 Agent Eval 模块  
   表结构：`agent_eval_case`、`agent_eval_run`、`agent_eval_result`。  
   指标：answer_contains、source_hit、grounding_score、latency_ms、cost_yuan。

4. 接 Spring AI  
   用 Spring AI 的 ChatClient / VectorStore 做一版并行实现，简历上可写“了解并可迁移 Spring AI 生态”。

5. 加 RAG 文档分块器  
   按车型、部件、故障码、章节标题分块，而不是简单截断文本。

## 5. 对标岗位匹配度评分

| 岗位类型 | 匹配度 | 说明 |
|---|---:|---|
| Java 后端 + AI 应用开发 | 90% | 非常适合，项目业务完整，后端深度足 |
| 大模型应用开发工程师 | 85% | RAG/Agent/LLM 接入都有，需要补更多框架名词 |
| AI Agent 开发工程师 | 80% | Agent 编排、工具、记忆都有，MCP 和评测可补 |
| 企业知识库/RAG 工程师 | 85% | 知识库、权限型 RAG 是亮点，向量库需加强 |
| 高级 AI Agent 架构师 | 65% | 还缺生产级 Agent 平台、MCP、评测和高可用 |
| 算法/模型训练工程师 | 35% | 不建议主投，这不是你的优势方向 |
| 推理引擎/模型平台工程师 | 30% | vLLM、TensorRT、CUDA、推理优化不在当前项目重点 |

## 6. 简历定位建议

不要写成：

> AI 算法工程师、模型训练工程师、深度学习算法专家。

应该写成：

> Java 后端 / AI Agent 应用开发工程师，擅长 Spring Cloud 微服务、企业级 RAG、Agent 工具调用、多轮记忆、支付业务闭环和大模型应用工程化落地。

## 7. 面试时的主叙事

建议这样讲：

> 我原本是后端开发，熟悉 Java 微服务、数据库、Redis、支付和业务系统落地。最近这个项目我把 AI Agent 能力接入了车企售后业务，做了知识库和社区内容的 RAG 问答，设计了 Safety、Planner、RAG、Tool、LLM、Guard、Memory 的编排链路。相比只会调模型 API，我更关注企业级落地问题，比如权限过滤、付费内容防泄露、工具鉴权、链路日志、token 成本、限流降级和支付回调幂等。

## 8. 需要补到你复习里的关键词

必会：

- LLM API、Prompt Engineering、Chat Messages、Temperature、Token。
- RAG、Embedding、Chunk、Vector Search、Rerank、Hybrid Search。
- Agent、Planner、ReAct、Tool Calling、Function Calling。
- Memory、Context Compression、Session、Trace。
- Prompt Injection、Hallucination、Grounding、Guardrails。
- MCP、LangChain、LangGraph、LlamaIndex、Spring AI。
- SSE、限流、降级、成本统计、可观测性。

了解即可：

- Fine-tuning、LoRA、QLoRA。
- vLLM、TensorRT-LLM、KV Cache。
- 多模态 OCR、图像理解。
- 模型评测、A/B 测试。

## 9. 可在简历里写的“补强后说法”

可以写：

- 设计 MCP 化改造方案，将知识库搜索、社区搜索、订单查询等内部工具标准化为 Agent Tool，便于多端复用。
- 规划 Agent Eval 评测体系，从 source hit、grounding score、latency、token cost 等维度评估 RAG 问答质量。
- 设计向量库演进方案，从 MySQL JSON 向量索引迁移到 pgvector/Milvus，提升大规模文档检索性能。

不要写：

- 精通大模型训练。
- 精通 CUDA 推理优化。
- 已完成千万级向量库生产部署。

## 10. 参考岗位和技术资料

- BOSS 直聘 AI 应用开发相关岗位：强调 AI 应用落地、RAG、Agent 应用架构、工具调用、业务流程自动化、LangChain/LlamaIndex 等。
  - https://www.zhipin.com/zhaopin/39608183209b29790HR-0tm5/
  - https://www.zhipin.com/zhaopin/b893569d6e5de1bd1n193Nm6/
- 字节 AI Agent 后端开发岗位：强调后端经验、模块独立开发、AI 垂直场景落地。
  - https://jobs.bytedance.com/experienced/position/7636694952900004101/detail
- OpenAI Function Calling：工具调用是应用和模型之间的多步骤流程。
  - https://developers.openai.com/api/docs/guides/function-calling
- OpenAI Agents SDK：强调 agent loop、handoff、session、tracing、guardrails。
  - https://developers.openai.com/api/docs/guides/agents
- OpenAI Tools：工具可扩展模型能力，包括 function calling、remote MCP servers 等。
  - https://developers.openai.com/api/docs/guides/tools
- LangGraph：强调 durable execution、streaming、human-in-the-loop、persistence。
  - https://docs.langchain.com/oss/python/langgraph/overview
- LangGraph Memory：短期记忆 checkpointer 和长期记忆 store。
  - https://docs.langchain.com/oss/python/langgraph/persistence
