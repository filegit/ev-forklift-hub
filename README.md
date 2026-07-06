# EV-Forklift-Hub · 新能源叉车社区平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-blue.svg)](https://spring.io/projects/spring-cloud)
[![Vue](https://img.shields.io/badge/Vue-3.3-42b883.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> 面向新能源叉车行业的综合性社区平台，采用 Spring Cloud 微服务 + Vue 3 前端，涵盖社区交流、配件商城、维修工单、知识库与 AI 智能助手。

**仓库地址：** [github.com/filegit/ev-forklift-hub](https://github.com/filegit/ev-forklift-hub)

---

## 项目简介

EV-Forklift-Hub 是一套可本地开发、可 Docker 部署的微服务系统，主要模块包括：

| 模块 | 说明 |
|------|------|
| 👥 用户服务 | 注册登录、个人资料、收货地址、积分体系 |
| 💬 社区服务 | 发帖、评论、点赞、收藏、积分奖励 |
| 🛒 配件商城 | 商品浏览、购物车、下单、支付、物流跟踪 |
| 🔧 维修服务 | 维修工单创建与查询 |
| 📚 知识库 | 文档上传、免费/付费阅读、支付宝解锁 |
| 🤖 AI 助手 | 基于知识库 + 社区内容的 RAG 问答（OpenAI 兼容 API） |

---

## 功能概览

### 用户与社区

- 用户注册 / 登录（BCrypt + JWT，网关统一鉴权）
- 帖子发布、分页列表、分类筛选、详情、点赞、浏览统计
- 评论与评论点赞
- 帖子收藏与个人中心（我的帖子 / 评论 / 收藏）
- 积分获取（发帖、评论、点赞）与积分兑换

### 配件商城

- 配件列表与详情
- 购物车增删改查
- 订单预览、提交、取消
- 支付宝沙箱支付（可配置）
- 订单状态与物流轨迹

### 维修服务

- 维修工单提交与查询
- 工单状态流转

### 知识库

- 文档列表与详情（支持 TXT / PDF）
- 管理员上传与编辑
- 免费文档直接阅读，付费文档支付宝解锁

### AI 智能助手

- 检索知识库文档 + 社区帖子片段（RAG）
- 调用 OpenAI 兼容接口（niceAPI、阿里云百炼等）
- 前端对话页，支持引用来源展示
- API Key 通过环境变量注入，**不写入代码仓库**

### 前端页面

Vue 3 + Vite + Element Plus + Pinia，主要路由：

| 路径 | 页面 |
|------|------|
| `/` | 社区首页 |
| `/parts` | 配件商城 |
| `/cart` · `/checkout` · `/orders` | 购物车与订单 |
| `/service` | 维修服务 |
| `/knowledge` | 知识库 |
| `/assistant` | AI 助手 |
| `/profile` · `/collections` | 个人中心与收藏 |

---

## 技术架构

```
                    ┌─────────────┐
                    │  efh-web    │  Vue 3 · Nginx :80
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │ efh-gateway │  Spring Cloud Gateway :8080
                    └──┬──┬──┬──┬─┘
         ┌─────────────┼──┼──┼──┼─────────────┐
         ▼             ▼  ▼  ▼  ▼             ▼
    efh-user    efh-community  efh-parts  efh-service
     :8081         :8082        :8083      :8084
         │             │            │          │
         └─────────────┴────────────┴──────────┘
                           │
              efh-knowledge :8085    efh-agent :8086
                           │
              MySQL · Redis · Nacos · ES · Kafka
```

### 后端

- Spring Boot 2.7 · Spring Cloud 2021 · Spring Cloud Alibaba
- Nacos 服务注册与配置
- MyBatis-Plus · MySQL 8.0 · Redis / Redisson
- JWT 认证 · OpenFeign 服务调用
- Elasticsearch 7.17（社区搜索，可选）
- Kafka（消息，可选）

### 前端

- Vue 3 · Vue Router · Pinia · Axios
- Element Plus · Vite 4

### 微服务端口

| 服务 | 端口 | 网关路径 | 状态 |
|------|------|----------|------|
| efh-gateway | 8080 | — | ✅ |
| efh-user | 8081 | `/user/**` | ✅ |
| efh-community | 8082 | `/community/**` | ✅ |
| efh-parts | 8083 | `/parts/**` | ✅ |
| efh-service | 8084 | `/service/**` | ✅ |
| efh-knowledge | 8085 | `/knowledge/**` | ✅ |
| efh-agent | 8086 | `/agent/**` | ✅ |
| efh-web | 80 | — | ✅ |

---

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- Node.js 16+（前端）
- Docker & Docker Compose（基础设施）
- Redis（Agent 服务依赖）

### 1. 克隆项目

```bash
git clone https://github.com/filegit/ev-forklift-hub.git
cd ev-forklift-hub
```

### 2. 启动基础设施

```bash
docker-compose up -d mysql redis nacos
```

等待约 1–2 分钟，MySQL 会自动执行 `docker/mysql/init/` 下的初始化脚本。

### 3. 编译后端

```bash
mvn clean package -DskipTests
```

### 4. 启动微服务

在 IDE 中依次启动，或使用 jar 包：

```bash
java -jar efh-gateway/target/efh-gateway-1.0.0.jar
java -jar efh-user/target/efh-user-1.0.0.jar
java -jar efh-community/target/efh-community-1.0.0.jar
java -jar efh-parts/target/efh-parts-1.0.0.jar
java -jar efh-service/target/efh-service-1.0.0.jar
java -jar efh-knowledge/target/efh-knowledge-1.0.0.jar
```

### 5. 配置并启动 AI Agent（可选）

```bash
copy .env.example .env.local    # Windows
# cp .env.example .env.local    # Linux / macOS

# 编辑 .env.local，填入 LLM_API_KEY
start-agent.bat                 # Windows
```

支持 niceAPI、阿里云百炼等 OpenAI 兼容接口，详见 `.env.example`。

### 6. 启动前端

```bash
cd efh-web
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`，API 默认代理到网关 `http://localhost:8080`。

### 7. 验证

```bash
curl http://localhost:8080/user/api/test
```

---

## 环境变量与密钥

| 文件 | 用途 | 是否提交 Git |
|------|------|--------------|
| `.env.example` | 本地 LLM 配置模板 | ✅ |
| `.env.local` | 本地真实 Key | ❌ |
| `deploy.env.example` | 服务器部署模板 | ✅ |
| `.env`（服务器） | 生产环境变量 | ❌ |

生产部署时将 `deploy.env.example` 复制为服务器上的 `.env`，通过 Docker Compose 或 systemd 注入，**不要把 API Key 写进代码或镜像**。

---

## 接口示例

所有请求经网关 `http://localhost:8080` 转发，需登录的接口携带 Header：

```
Authorization: Bearer {token}
```

### 用户

```http
POST /user/api/register
POST /user/api/login
GET  /user/api/info
GET  /user/api/points/balance
```

### 社区

```http
GET  /community/api/post/list?page=1&size=10
POST /community/api/post
POST /community/api/post/{id}/like
GET  /community/api/comment/post/{postId}
POST /community/api/collection/{postId}
```

### AI 助手

```http
POST /agent/api/chat
Content-Type: application/json
Authorization: Bearer {token}

{ "question": "叉车电池如何保养？" }
```

更多接口见项目内文档：

- [用户服务接口测试指南.md](用户服务接口测试指南.md)
- [社区服务发帖功能测试指南.md](社区服务发帖功能测试指南.md)
- [评论功能测试指南.md](评论功能测试指南.md)
- [Postman测试指南.md](Postman测试指南.md)

---

## Docker 部署

```bash
# 仅基础设施
docker-compose up -d mysql redis nacos

# 完整构建（网关 + 微服务 + 前端）
docker-compose up -d --build
```

生产环境配置：

```bash
cp deploy.env.example .env
vim .env   # 填入数据库密码、LLM_API_KEY 等
docker compose --env-file .env up -d
```

详细步骤：

- [Docker部署完整指南.md](Docker部署完整指南.md)
- [服务器部署指南.md](服务器部署指南.md)
- [功能扩展快速部署指南.md](功能扩展快速部署指南.md)

---

## 项目结构

```
ev-forklift-hub/
├── efh-common/          # 公共模块（JWT、Redis、工具类）
├── efh-gateway/         # API 网关
├── efh-user/            # 用户服务
├── efh-community/       # 社区服务
├── efh-parts/           # 配件商城
├── efh-service/         # 维修服务
├── efh-knowledge/       # 知识库
├── efh-agent/           # AI RAG 助手
├── efh-web/             # Vue 3 前端
├── docker/              # Dockerfile 与 MySQL 初始化脚本
├── docker-compose.yml
├── .env.example         # LLM 本地配置模板
├── deploy.env.example   # 服务器部署模板
├── start-agent.bat      # Agent 启动脚本（Windows）
└── docs (*.md)          # 开发与部署文档
```

---

## 安全说明

- 密码 BCrypt 加密，接口 JWT 鉴权，网关统一校验
- LLM / 支付宝等密钥通过环境变量注入
- `.env.local`、`.env`、`uploads/` 已加入 `.gitignore`
- 生产环境请更换默认数据库密码，并使用强密码与 HTTPS

---

## 路线图

| 版本 | 内容 | 状态 |
|------|------|------|
| v1.0 | 用户、社区、网关 | ✅ |
| v1.5 | 评论、收藏、积分、个人中心 | ✅ |
| v2.0 | 配件商城、维修工单、知识库 | ✅ |
| v2.5 | AI RAG 助手、前端完整页面 | ✅ |
| v3.0 | 向量检索、SSE 流式输出、消息通知 | 📅 计划中 |

---

## 文档索引

| 类型 | 文档 |
|------|------|
| 快速上手 | [快速开始指南.md](快速开始指南.md) · [启动前必读.md](启动前必读.md) |
| 微服务 | [微服务启动完整指南.md](微服务启动完整指南.md) |
| 部署 | [Docker部署完整指南.md](Docker部署完整指南.md) · [服务器部署指南.md](服务器部署指南.md) |
| 功能总结 | [功能扩展最终总结.md](功能扩展最终总结.md) · [项目完成总结.md](项目完成总结.md) |

---

## 贡献

1. Fork 本仓库
2. 创建分支 `git checkout -b feature/your-feature`
3. 提交 `git commit -m 'feat: your feature'`
4. 推送 `git push origin feature/your-feature`
5. 发起 Pull Request

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源。

---

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) · [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Nacos](https://nacos.io/) · [MyBatis-Plus](https://baomidou.com/)
- [Vue.js](https://vuejs.org/) · [Element Plus](https://element-plus.org/)

---

如果这个项目对你有帮助，欢迎 Star ⭐
