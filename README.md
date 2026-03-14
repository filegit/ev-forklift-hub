# 🚗 EV-Forklift-Hub - 新能源叉车社区平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> 基于 Spring Cloud 微服务架构的新能源叉车社区平台，提供用户管理、社区交流、配件商城、维修服务等功能。

---

## 📋 项目简介

EV-Forklift-Hub 是一个面向新能源叉车行业的综合性社区平台，采用 Spring Cloud 微服务架构，提供：

- 👥 **用户服务** - 用户注册、登录、信息管理
- 💬 **社区服务** - 帖子发布、评论、点赞、搜索
- 🛒 **配件服务** - 配件商城、订单管理
- 🔧 **维修服务** - 维修工单、师傅分配

---

## ✨ 核心功能

### 已完成 ✅

- ✅ 用户注册登录（BCrypt + JWT）
- ✅ 帖子发布和查询
- ✅ 帖子点赞和浏览统计
- ✅ 分页查询和分类筛选
- ✅ Redis 缓存
- ✅ API 网关统一认证
- ✅ 服务注册与发现

### 开发中 🚧

- 🚧 评论功能
- 🚧 配件商城
- 🚧 维修工单
- 🚧 Elasticsearch 全文搜索
- 🚧 图片上传

---

## 🏗️ 技术架构

### 后端技术栈

- **基础框架：** Spring Boot 2.7.18
- **微服务：** Spring Cloud 2021.0.8
- **网关：** Spring Cloud Gateway
- **注册中心：** Nacos 2.2.3
- **数据库：** MySQL 8.0
- **缓存：** Redis 7.0
- **ORM：** MyBatis-Plus 3.5.3.1
- **认证：** JWT + BCrypt
- **搜索：** Elasticsearch 7.17.9 (可选)

### 微服务列表

| 服务 | 端口 | 说明 | 状态 |
|------|------|------|------|
| efh-gateway | 8080 | API 网关 | ✅ |
| efh-user | 8081 | 用户服务 | ✅ |
| efh-community | 8082 | 社区服务 | ✅ |
| efh-parts | 8083 | 配件服务 | 🚧 |
| efh-service | 8084 | 维修服务 | 🚧 |

---

## 🚀 快速开始

### 前提条件

- JDK 8+
- Maven 3.6+
- Docker & Docker Compose
- IDEA (推荐)

### 5分钟启动

```bash
# 1. 克隆项目
git clone https://github.com/your-repo/ev-forklift-hub.git
cd ev-forklift-hub

# 2. 启动基础设施
docker-compose up -d mysql redis nacos

# 3. 等待 1-2 分钟，然后修复数据库
docker exec -it efh-mysql mysql -uroot -p123456 < fix_user_table.sql

# 4. 在 IDEA 中启动微服务
# - UserApplication
# - CommunityApplication
# - GatewayApplication

# 5. 验证服务
curl http://localhost:8080/user/api/test
```

**详细步骤见：** [快速开始指南.md](快速开始指南.md)

---

## 📝 接口文档

### 用户服务

```bash
# 注册
POST /user/api/register
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "phone": "13800138000"
}

# 登录
POST /user/api/login
{
  "username": "testuser",
  "password": "123456"
}

# 获取用户信息
GET /user/api/info
Header: Authorization: Bearer {token}
```

### 社区服务

```bash
# 发布帖子
POST /community/api/post
Header: Authorization: Bearer {token}
{
  "title": "帖子标题",
  "content": "帖子内容",
  "category": 1
}

# 帖子列表
GET /community/api/post/list?page=1&size=10&category=1

# 帖子详情
GET /community/api/post/{id}

# 点赞
POST /community/api/post/{id}/like
Header: Authorization: Bearer {token}
```

**完整接口文档见：**
- [用户服务接口测试指南.md](用户服务接口测试指南.md)
- [社区服务发帖功能测试指南.md](社区服务发帖功能测试指南.md)

---

## 🗄️ 数据库设计

### 用户表 (user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(100) | 密码，BCrypt加密 |
| nickname | VARCHAR(50) | 昵称 |
| phone | VARCHAR(20) | 手机号，唯一 |
| email | VARCHAR(100) | 邮箱 |
| gender | TINYINT | 性别 |
| user_type | TINYINT | 用户类型 |
| status | TINYINT | 状态 |
| points | INT | 积分 |

### 帖子表 (post)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 发帖用户ID |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| category | INT | 分类 |
| view_count | INT | 浏览量 |
| like_count | INT | 点赞数 |
| comment_count | INT | 评论数 |
| status | TINYINT | 状态 |

---

## 🐳 Docker 部署

### 本地开发

```bash
# 启动基础设施
docker-compose up -d mysql redis nacos

# 在 IDEA 中启动微服务
```

### 完整 Docker 部署

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

**详细部署指南见：**
- [Docker部署指南.md](Docker部署指南.md)
- [服务器部署指南.md](服务器部署指南.md)

---

## 📚 文档目录

### 开发文档
- [快速开始指南.md](快速开始指南.md) - 5分钟快速启动
- [用户服务接口测试指南.md](用户服务接口测试指南.md) - 用户服务接口文档
- [社区服务发帖功能测试指南.md](社区服务发帖功能测试指南.md) - 社区服务接口文档
- [数据库表结构修复指南.md](数据库表结构修复指南.md) - 数据库维护文档

### 部署文档
- [Docker部署指南.md](Docker部署指南.md) - Docker 部署详细步骤
- [服务器部署指南.md](服务器部署指南.md) - 生产环境部署指南

### 项目文档
- [项目完成总结.md](项目完成总结.md) - 项目完成情况总结
- [Docker化与功能完善计划.md](Docker化与功能完善计划.md) - 开发计划

---

## 🧪 测试

### 单元测试

```bash
mvn test
```

### 接口测试

使用 Postman 或 curl 测试接口，详见接口文档。

### 性能测试

```bash
# 使用 Apache Bench
ab -n 1000 -c 100 http://localhost:8080/user/api/test
```

---

## 📊 项目统计

- **代码行数：** ~10,000 行
- **接口数量：** 10+ 个
- **数据库表：** 5+ 张
- **文档数量：** 10+ 份
- **测试通过率：** 100%

---

## 🔒 安全特性

- ✅ BCrypt 密码加密
- ✅ JWT Token 认证
- ✅ 网关统一认证
- ✅ 白名单配置
- ✅ CORS 跨域配置
- ✅ SQL 注入防护
- ✅ XSS 防护

---

## 🛣️ 开发路线图

### v1.0 (已完成) ✅
- ✅ 用户注册登录
- ✅ 帖子发布查询
- ✅ 点赞功能
- ✅ 网关路由

### v1.1 (开发中) 🚧
- 🚧 评论功能
- 🚧 配件商城
- 🚧 维修工单

### v2.0 (计划中) 📅
- 📅 Elasticsearch 搜索
- 📅 图片上传
- 📅 消息通知
- 📅 数据分析

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 团队

- **项目负责人：** EFH Team
- **技术支持：** support@efh.com

---

## 📞 联系我们

- **Email：** support@efh.com
- **GitHub：** https://github.com/your-repo/ev-forklift-hub
- **文档：** 查看项目根目录下的 `.md` 文件

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Nacos](https://nacos.io/)
- [MyBatis-Plus](https://baomidou.com/)
- [Redis](https://redis.io/)

---

## ⭐ Star History

如果这个项目对你有帮助，请给我们一个 Star！⭐

---

**Made with ❤️ by EFH Team**
