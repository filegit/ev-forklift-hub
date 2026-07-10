# 个人简历 - AI Agent 开发工程师

## 基本信息

姓名：XXX  
手机号：XXX  
邮箱：XXX  
求职方向：Java 后端开发 / AI Agent 开发工程师 / 大模型应用开发工程师  
当前背景：车企后端开发，熟悉售后、维修、配件、知识库等业务场景  
期望城市：XXX  

## 个人优势

- Java 后端开发背景，熟悉 Spring Boot、Spring Cloud Gateway、MyBatis-Plus、MySQL、Redis、Docker、Nginx 等企业级开发和部署技术。
- 具备 AI Agent 应用落地经验，能围绕企业知识库、社区内容、业务系统 API 构建 RAG 问答、工具调用、多轮记忆和流式输出能力。
- 熟悉大模型应用工程化问题，包括 Prompt 设计、RAG 检索、Embedding、重排、权限过滤、Prompt Injection 防护、幻觉治理、限流降级、token 成本统计和链路追踪。
- 有车企业务背景，理解售后服务、维修工单、备件采购、支付物流、知识沉淀等真实业务流程，能将 AI Agent 和业务系统结合。
- 具备从开发到部署的完整经验，能够完成接口设计、数据库设计、支付接入、前端联调、服务器部署和线上问题排查。

## 技术栈

### 后端

Java 8、Spring Boot、Spring Cloud Gateway、Spring Cloud Alibaba、Nacos、MyBatis-Plus、MySQL、Redis、Redisson、Kafka、JWT、支付宝 SDK、Docker、Nginx、Linux。

### AI / 大模型应用

LLM API、OpenAI Compatible API、Qwen / DashScope、Prompt Engineering、RAG、Embedding、Vector Search、Rerank、Hybrid Search、Function Calling / Tool Calling、Agent Workflow、Multi-Agent Orchestration、Agent Memory、SSE 流式输出、Prompt Injection 防护、Hallucination Guard、Token 成本统计、Agent Trace。

### 前端和工程化

Vue 3、Vite、Element Plus、Pinia、Axios、SSE/fetch、RESTful API、Postman、Git、Docker Compose、Nginx 反向代理。

### 了解和持续学习

LangChain、LangGraph、LlamaIndex、Spring AI、MCP、pgvector、Milvus、Prometheus、Grafana、LoRA、vLLM。

## 项目经历

### 新能源叉车售后智能协同中台

项目类型：企业级 Java 微服务 + AI Agent 应用  
项目角色：后端开发 / AI Agent 模块开发 / 部署联调  
项目技术：Spring Boot、Spring Cloud Gateway、Nacos、MyBatis-Plus、MySQL、Redis、Redisson、Kafka、Docker、Nginx、Vue 3、支付宝 SDK、OpenAI Compatible API、RAG、SSE、Tool Calling  

#### 项目描述

该项目面向车企售后业务，建设集社区交流、知识库、配件商城、维修工单、支付物流和 AI 助手于一体的智能协同中台。系统通过微服务拆分用户、社区、知识库、配件、维修和 AI Agent 模块，并基于企业知识库和社区内容构建 RAG 智能问答能力，辅助用户查询维修资料、故障经验、配件信息和售后知识。

#### 核心模块

- 用户服务：注册登录、JWT 鉴权、短信验证码、积分、地址管理。
- 社区服务：帖子、评论、回复、点赞、收藏、积分奖励。
- 知识库服务：文档上传、分类、发布、在线预览、权限控制、积分/支付宝解锁。
- 配件服务：配件上架、购物车、下单、支付宝支付、订单状态、物流轨迹。
- 维修服务：维修预约、工单流转。
- AI Agent 服务：知识库/社区 RAG 问答、多轮会话、工具调用、流式输出、监控和成本统计。
- 前端页面：社区、知识库、配件商城、订单、支付、AI 助手、个人中心。

#### 个人职责和亮点

1. 设计并实现微服务后端架构  
   - 使用 Spring Cloud Gateway 作为统一入口，完成路由转发、JWT 鉴权、白名单配置和用户身份透传。
   - 拆分用户、社区、知识库、配件、维修、AI Agent 等服务，基于 MyBatis-Plus 完成核心业务 CRUD 和分页查询。
   - 设计 MySQL 表结构，覆盖用户、积分、帖子、评论、知识库、订单、支付单、物流、Agent 日志和会话等数据。

2. 实现企业级 RAG 智能问答  
   - 基于知识库文档、社区帖子和评论构建多数据源检索能力，支持按用户问题召回相关资料。
   - 对知识库文档做权限过滤，免费文档可全文参与 RAG，付费/积分文档未解锁时只允许摘要参与检索，避免 AI 泄露付费内容。
   - 结合关键词分数和向量相似度进行重排，过滤低相关内容，保留 topK 上下文注入 Prompt。
   - 响应中返回引用来源，包括来源类型、标题、摘要片段、跳转链接和相关性分数，提升答案可解释性。

3. 设计 AI Agent 编排链路  
   - 通过 `AgentOrchestrator` 串联 Safety、Planner、RAG、Tool、LLM、Guard、Memory 等步骤。
   - `PlannerAgent` 根据问题判断是否需要并行检索、工具增强等策略。
   - 支持知识库和社区并行 RAG，提高多源检索效率。
   - 使用 `traceId/sessionId` 串联一次 Agent 执行链路，便于排查和观测。

4. 实现 Tool Calling 工具体系  
   - 抽象 `AgentTool` 接口，设计 `ToolRegistry` 统一管理工具，支持转换为 OpenAI tools 格式。
   - 实现 `ToolExecutor`，统一处理工具执行、登录校验、管理员校验、工具黑名单和调用日志。
   - 当前支持知识库搜索、社区搜索等工具，后续可扩展订单查询、维修工单查询、配件查询等业务工具。

5. 实现多轮会话记忆和上下文压缩  
   - 使用 Redis 保存短期会话消息，支持用户同一 session 多轮对话。
   - 使用 MySQL 保存长期摘要和会话元数据。
   - 使用 Redisson 对同一 userId/sessionId 的会话写入加锁，避免多实例并发写入导致上下文错乱。
   - 当上下文超过 token 阈值时，通过摘要压缩旧对话，保留最近消息和长期摘要。

6. 封装 LLM 调用和流式输出  
   - 封装 OpenAI 兼容 `/chat/completions` 接口，支持 Qwen / DashScope 等模型供应商。
   - 支持同步问答和 SSE 流式输出，前端可逐字展示 AI 回复。
   - 统计 promptTokens、completionTokens、totalTokens 和 costYuan，支撑成本监控。
   - LLM 或 Embedding API 不可用时，降级返回检索摘要或本地哈希向量结果，提升服务可用性。

7. 处理 AI 安全和幻觉治理  
   - 增加 Prompt Injection 检测，拦截要求忽略系统提示、泄露 prompt 等异常输入。
   - 对危险工具配置黑名单，避免模型触发高风险操作。
   - 使用 `HallucinationGuardService` 做 grounding 检查，资料不足或相关性较低时追加风险提示。
   - 对维修安全类回答强调引用资料和人工核实，避免用户仅凭 AI 回答操作设备。

8. 完成支付购买闭环  
   - 接入支付宝网页支付，支持配件订单、知识库付费解锁、积分购买。
   - 删除演示支付入口，所有购买行为统一跳转支付宝。
   - 支付成功后以后端异步回调为准，完成验签、状态校验、幂等更新和业务状态变更。
   - 积分购买从“直接到账”改造为“支付回调成功后到账”，避免前端伪造购买成功。

9. 完成部署和联调  
   - 使用 Docker 部署 MySQL、Redis、Nacos、Nginx，Java 服务运行在 Linux 服务器。
   - 配置 Nginx 前端静态资源和 API 反向代理。
   - 完成服务器服务启动、日志排查、端口检查、接口验证和支付回调配置。

#### 项目成果

- 完成一个覆盖社区、知识库、配件商城、维修工单、支付物流和 AI 助手的可运行系统。
- AI Agent 支持 RAG 问答、多轮上下文、工具调用、流式输出、限流、日志、成本统计和安全防护。
- 支付相关购买入口全部接入支付宝跳转支付，避免前端直接确认成功。
- 生成移动端接口文档，支持前端或 App 团队基于接口独立开发。
- 项目已部署到服务器，可通过 Web 页面访问和测试核心流程。

#### 可扩展优化

- 将知识库搜索、社区搜索、订单查询、维修工单查询封装为 MCP tools，提升多端 Agent 复用能力。
- 引入 pgvector / Milvus / Elasticsearch dense vector 替换 MySQL JSON 向量索引，提升大规模文档检索性能。
- 建立 Agent Eval 评测集，从 source hit、grounding score、latency、token cost 等维度评估 RAG 质量。
- 接入 Prometheus + Grafana，实现模型调用、工具调用、token 成本、错误率和 P95 延迟监控。
- 基于 Spring AI 或 LangGraph 思想重构 Agent 工作流，提升可恢复性、可观测性和可扩展性。

## 项目二：企业后端业务系统开发

项目描述：当前在车企从事后端相关开发工作，参与企业内部业务系统建设和维护，涉及接口开发、数据处理、业务流程实现和系统问题排查。

个人职责：

- 负责后端接口开发和业务逻辑实现。
- 参与数据库表设计、SQL 编写和接口联调。
- 处理线上问题，进行日志分析、接口排查和数据修复。
- 根据业务需求与前端、测试、产品协同完成迭代交付。

技术栈：

Java、Spring Boot、MySQL、Redis、Linux、Git、接口联调、业务系统维护。

> 注：这一段可以根据你的真实工作项目继续细化，建议补充具体业务名称、负责模块、数据量、性能优化或上线成果。

## 面试项目介绍模板

我之前主要做 Java 后端，熟悉微服务、数据库、Redis、支付和部署。为了转 AI Agent 开发，我做了一个车企售后智能协同中台，把社区、知识库、配件商城、维修工单和 AI 助手结合起来。AI 部分不是简单调大模型接口，而是做了 RAG 检索、权限过滤、工具调用、多轮记忆、SSE 流式输出、限流、日志和 token 成本统计。  

这个项目里我最想重点介绍三个点：

第一是企业级 RAG。知识库里有免费和付费文档，我在检索时会根据用户解锁状态决定是否能读取全文，避免 AI 泄露付费内容。  

第二是 Agent 编排。我通过后端固定编排实现 Safety、Planner、RAG、Tool、LLM、Guard、Memory 的链路，既能体现 Agent 能力，又能保证企业业务可控。  

第三是工程化落地。系统有网关鉴权、Redis 限流、链路日志、成本统计、支付宝支付回调、Docker/Nginx 部署，能支撑真实业务流程，不只是一个 AI demo。

## 高频面试问答简版

### 1. 你这个项目为什么算 AI Agent？

因为它不是单轮问答，而是包含目标拆解、检索、工具调用、记忆、安全检查和业务系统联动。`AgentOrchestrator` 会串联 Safety、Planner、RAG、Tool、LLM、Guard、Memory 多个步骤，并记录 trace 和成本。

### 2. RAG 怎么做的？

数据来自知识库、帖子、评论。用户提问后先做关键词和向量检索，召回候选内容，再根据关键词分数和向量相似度重排，过滤低相关内容，把 topK 注入 prompt。回答会返回 sources，便于用户查看原始文档或帖子。

### 3. 如何防止付费知识被 AI 泄露？

检索时根据用户身份和解锁记录判断权限。免费文档可以全文参与检索，已解锁文档可以全文参与检索，未解锁的付费文档只使用标题和摘要，不读取全文。

### 4. 如何降低幻觉？

通过三层方式：检索相关性阈值过滤低质量资料；prompt 要求基于资料回答并标注引用；生成后做 grounding 检查，资料不足时追加风险提示或建议人工核实。

### 5. Tool Calling 怎么做？

我把工具抽象成 `AgentTool`，通过 `ToolRegistry` 注册，再转成 OpenAI tools schema。执行时统一走 `ToolExecutor`，做登录校验、管理员校验、工具黑名单和执行日志。

### 6. 为什么不用 LangChain？

项目基于 Java 微服务，业务里有鉴权、权限、支付、事务、日志这些强业务控制点，自研编排更可控。LangChain/LangGraph 的思想可以参考，比如 Planner、Tool、Memory、Graph 工作流，后续也可以用 Spring AI 或 MCP 做标准化。

### 7. Agent 记忆怎么做？

短期记忆存在 Redis，保存最近多轮对话；长期摘要存在 MySQL；同一个用户同一个 session 写入时用 Redisson 加锁；超过 token 阈值时压缩旧对话。

### 8. 如何控制模型成本？

做单用户限流，控制 topK 和 chunk 大小，超过上下文做摘要压缩，统计 promptTokens 和 completionTokens，接口失败时降级为检索摘要。

### 9. 支付为什么以后端回调为准？

前端不可信，不能让前端直接确认购买成功。正确流程是后端生成支付单，用户跳支付宝，支付宝异步回调后端，后端验签、金额校验、状态幂等后再更新订单、积分或知识库解锁状态。

### 10. 这个项目还有哪些不足？

目前向量库还不是生产级，可以迁移到 pgvector/Milvus；MCP 还只是规划，后续可以把业务工具标准化；RAG 评测体系还可以补充，增加 source hit、grounding score、latency、token cost 等指标。

## 自我评价

我是后端转 AI Agent 应用开发方向，优势是能把大模型能力落到真实业务系统里。我熟悉 Java 微服务、数据库、Redis、支付、部署和业务流程，也理解 RAG、Agent、Tool Calling、Memory、SSE、Prompt 安全和成本控制。相比只做 demo，我更关注企业场景里的权限、可靠性、可观测性、降级和业务闭环。
