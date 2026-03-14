# 🐳 EV-Forklift-Hub Docker 化与功能完善计划

## 📋 项目目标

将整个微服务系统 Docker 化，完善核心功能，确保可以平滑迁移到生产服务器。

---

## 第一阶段：Docker 环境完善（1-2天）

### ✅ 已完成
- [x] Nacos 容器运行（8848）
- [x] Elasticsearch 容器运行（9200）
- [x] 所有微服务本地启动成功
- [x] 服务注册到 Nacos
- [x] 网关路由验证通过

### 🔲 待完成

#### 1. 补充基础设施容器
```yaml
# docker-compose.yml 需要添加：
- MySQL 容器（替代本地 MySQL）
- Redis 容器（替代本地 Redis）
- Kafka 容器（可选，用于消息队列）
```

#### 2. 创建微服务 Dockerfile
为每个微服务创建 Dockerfile：
- efh-gateway
- efh-user
- efh-community
- efh-parts
- efh-service

#### 3. 完善 docker-compose.yml
- 定义所有服务
- 配置服务依赖关系
- 配置网络和卷
- 配置环境变量

#### 4. 创建数据库初始化脚本
- 自动创建数据库
- 自动创建表结构
- 导入初始数据

---

## 第二阶段：核心功能开发（2-3周）

### 1. 用户服务 (efh-user)

#### 1.1 数据库设计
```sql
-- 用户表（分库分表）
CREATE TABLE user_0/1 (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(200),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
```

#### 1.2 API 接口
- [x] POST /user/api/register - 用户注册
- [x] POST /user/api/login - 用户登录
- [ ] GET /user/api/info - 获取用户信息
- [ ] PUT /user/api/info - 更新用户信息
- [ ] PUT /user/api/password - 修改密码
- [ ] POST /user/api/logout - 退出登录

#### 1.3 功能特性
- JWT Token 认证
- 密码加密（BCrypt）
- Redis 缓存用户信息
- 登录日志记录

---

### 2. 社区服务 (efh-community)

#### 2.1 数据库设计
```sql
-- 帖子表（分库分表）
CREATE TABLE post_0/1_0/1/2/3 (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(200),
    content TEXT,
    category VARCHAR(50),
    images TEXT,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 评论表（分库分表）
CREATE TABLE comment_0/1_0/1/2/3 (
    id BIGINT PRIMARY KEY,
    post_id BIGINT,
    user_id BIGINT,
    content TEXT,
    parent_id BIGINT,
    like_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME
);
```

#### 2.2 API 接口
- [ ] POST /community/api/post - 发布帖子
- [ ] GET /community/api/post/list - 帖子列表（分页）
- [ ] GET /community/api/post/{id} - 帖子详情
- [ ] PUT /community/api/post/{id} - 更新帖子
- [ ] DELETE /community/api/post/{id} - 删除帖子
- [ ] POST /community/api/post/{id}/like - 点赞
- [ ] POST /community/api/post/{id}/comment - 发表评论
- [ ] GET /community/api/post/{id}/comments - 评论列表
- [ ] GET /community/api/search - 搜索帖子（Elasticsearch）

#### 2.3 功能特性
- ShardingSphere 分库分表
- Redis 缓存热门帖子
- Elasticsearch 全文搜索
- 图片上传（OSS 或本地）
- 浏览量统计

---

### 3. 配件服务 (efh-parts)

#### 3.1 数据库设计
```sql
-- 配件表
CREATE TABLE parts (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    category VARCHAR(50),
    brand VARCHAR(50),
    model VARCHAR(100),
    price DECIMAL(10,2),
    stock INT DEFAULT 0,
    images TEXT,
    description TEXT,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 配件订单表
CREATE TABLE parts_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE,
    user_id BIGINT,
    parts_id BIGINT,
    quantity INT,
    total_price DECIMAL(10,2),
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
```

#### 3.2 API 接口
- [ ] GET /parts/api/list - 配件列表（分页、筛选）
- [ ] GET /parts/api/{id} - 配件详情
- [ ] POST /parts/api/order - 创建订单
- [ ] GET /parts/api/order/list - 我的订单
- [ ] GET /parts/api/order/{id} - 订单详情
- [ ] PUT /parts/api/order/{id}/cancel - 取消订单

#### 3.3 功能特性
- 库存管理
- 订单状态流转
- Redis 缓存配件信息
- 分类筛选

---

### 4. 维修服务 (efh-service)

#### 4.1 数据库设计
```sql
-- 维修工单表
CREATE TABLE service_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE,
    user_id BIGINT,
    forklift_model VARCHAR(100),
    problem_desc TEXT,
    images TEXT,
    address VARCHAR(200),
    contact_phone VARCHAR(20),
    technician_id BIGINT,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME,
    finish_time DATETIME
);

-- 维修师傅表
CREATE TABLE technician (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50),
    phone VARCHAR(20),
    skill_level TINYINT,
    status TINYINT DEFAULT 1,
    create_time DATETIME
);
```

#### 4.2 API 接口
- [ ] POST /service/api/order - 创建维修工单
- [ ] GET /service/api/order/list - 工单列表
- [ ] GET /service/api/order/{id} - 工单详情
- [ ] PUT /service/api/order/{id}/assign - 分配师傅
- [ ] PUT /service/api/order/{id}/status - 更新工单状态
- [ ] PUT /service/api/order/{id}/finish - 完成工单

#### 4.3 功能特性
- 工单状态流转
- 师傅分配算法
- 消息通知（Kafka）
- 工单评价

---

### 5. 网关服务 (efh-gateway)

#### 5.1 功能完善
- [x] 路由转发
- [x] JWT 认证
- [x] 白名单配置
- [ ] 限流配置（Redis + Lua）
- [ ] 熔断降级（Sentinel）
- [ ] 日志记录
- [ ] 统一异常处理
- [ ] 接口文档聚合（Swagger）

---

## 第三阶段：Docker 化实施（3-5天）

### 1. 创建 Dockerfile

#### 示例：efh-user/Dockerfile
```dockerfile
FROM openjdk:8-jre-slim
WORKDIR /app
COPY target/efh-user-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
```

### 2. 完善 docker-compose.yml

```yaml
version: '3.8'

services:
  # MySQL
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
    volumes:
      - ./docker/mysql/data:/var/lib/mysql
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
    ports:
      - "3306:3306"

  # Redis
  redis:
    image: redis:7-alpine
    command: redis-server --requirepass 123456
    ports:
      - "6379:6379"

  # Nacos
  nacos:
    image: nacos/nacos-server:v2.2.3
    environment:
      MODE: standalone
    ports:
      - "8848:8848"

  # Elasticsearch
  elasticsearch:
    image: elasticsearch:7.17.15
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  # 网关服务
  efh-gateway:
    build: ./efh-gateway
    depends_on:
      - nacos
      - redis
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  # 用户服务
  efh-user:
    build: ./efh-user
    depends_on:
      - mysql
      - redis
      - nacos
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  # 社区服务
  efh-community:
    build: ./efh-community
    depends_on:
      - mysql
      - redis
      - nacos
      - elasticsearch
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  # 配件服务
  efh-parts:
    build: ./efh-parts
    depends_on:
      - mysql
      - redis
      - nacos
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  # 维修服务
  efh-service:
    build: ./efh-service
    depends_on:
      - mysql
      - redis
      - nacos
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
```

### 3. 创建 application-docker.yml

为每个服务创建 Docker 环境配置，使用容器服务名：
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/efh_user_0
  redis:
    host: redis
  cloud:
    nacos:
      discovery:
        server-addr: nacos:8848
```

### 4. 数据库初始化脚本

创建 `docker/mysql/init/init.sql`：
```sql
-- 创建所有数据库
CREATE DATABASE IF NOT EXISTS efh_user_0;
CREATE DATABASE IF NOT EXISTS efh_user_1;
CREATE DATABASE IF NOT EXISTS efh_community_0;
CREATE DATABASE IF NOT EXISTS efh_community_1;
CREATE DATABASE IF NOT EXISTS efh_parts;
CREATE DATABASE IF NOT EXISTS efh_service;

-- 创建表结构
USE efh_user_0;
CREATE TABLE user (...);
-- ... 更多表
```

---

## 第四阶段：测试与优化（1周）

### 1. 功能测试
- [ ] 用户注册登录流程
- [ ] 帖子发布查询流程
- [ ] 配件订单流程
- [ ] 维修工单流程
- [ ] 网关路由测试
- [ ] 服务间调用测试

### 2. 性能测试
- [ ] 压力测试（JMeter）
- [ ] 并发测试
- [ ] 数据库性能优化
- [ ] Redis 缓存优化

### 3. Docker 测试
- [ ] 一键启动所有服务
- [ ] 服务健康检查
- [ ] 容器日志查看
- [ ] 数据持久化验证

---

## 第五阶段：部署准备（2-3天）

### 1. 生产环境配置
- [ ] 创建 application-prod.yml
- [ ] 配置生产数据库
- [ ] 配置域名和 SSL
- [ ] 配置监控告警

### 2. 部署脚本
```bash
#!/bin/bash
# deploy.sh - 一键部署脚本

# 1. 拉取最新代码
git pull

# 2. Maven 打包
mvn clean package -DskipTests

# 3. 构建 Docker 镜像
docker-compose build

# 4. 启动所有服务
docker-compose up -d

# 5. 查看服务状态
docker-compose ps
```

### 3. 服务器环境准备
- [ ] 安装 Docker
- [ ] 安装 Docker Compose
- [ ] 配置防火墙
- [ ] 配置域名解析

---

## 📝 开发优先级

### 高优先级（必须完成）
1. ✅ 验证网关路由
2. 🔲 完善 Docker Compose 配置
3. 🔲 创建数据库表结构
4. 🔲 开发用户注册登录
5. 🔲 开发帖子发布查询
6. 🔲 为所有服务创建 Dockerfile

### 中优先级（重要功能）
7. 🔲 配件订单功能
8. 🔲 维修工单功能
9. 🔲 Elasticsearch 搜索
10. 🔲 Redis 缓存优化

### 低优先级（优化功能）
11. 🔲 限流降级
12. 🔲 监控告警
13. 🔲 日志收集
14. 🔲 性能优化

---

## 🎯 下一步行动

### 立即开始（今天）

**选项 A：完善 Docker 环境**
1. 补充 MySQL 和 Redis 容器到 docker-compose.yml
2. 创建数据库初始化脚本
3. 测试容器化环境

**选项 B：开发核心功能**
1. 设计用户表结构
2. 开发用户注册接口
3. 开发用户登录接口
4. 测试 JWT 认证

**选项 C：创建 Dockerfile**
1. 为每个微服务创建 Dockerfile
2. 创建 application-docker.yml
3. 测试服务容器化

---

## 💡 建议的开发顺序

1. **先完善 Docker 环境**（1天）
   - 这样后续开发可以直接在容器中测试
   - 确保环境一致性

2. **再开发核心功能**（2周）
   - 用户服务 → 社区服务 → 配件服务 → 维修服务
   - 每完成一个服务就进行测试

3. **最后优化和部署**（1周）
   - 性能优化
   - 部署到服务器
   - 监控配置

---

## 📚 需要创建的文档

- [ ] API 接口文档
- [ ] 数据库设计文档
- [ ] Docker 部署文档
- [ ] 开发规范文档
- [ ] 测试用例文档

---

**您想从哪里开始？**

1. 完善 Docker Compose 配置（添加 MySQL、Redis 容器）
2. 设计数据库表结构
3. 开发用户注册登录功能
4. 创建所有服务的 Dockerfile

请告诉我您的选择，我会立即开始帮您实现！🚀
