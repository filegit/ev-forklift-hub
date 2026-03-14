# 🐳 Docker 环境部署指南

## ✅ 已完成的工作

### 1. Docker Compose 配置
- ✅ MySQL 8.0（数据库）
- ✅ Redis 7（缓存）
- ✅ Nacos 2.2.3（服务注册与配置中心）
- ✅ Elasticsearch 7.17.9（搜索引擎）
- ✅ Kafka + Zookeeper（消息队列）
- ✅ 5 个微服务容器配置

### 2. 数据库初始化
- ✅ 自动创建所有数据库
- ✅ 自动创建所有表结构
- ✅ 插入测试数据

### 3. 服务配置
- ✅ 每个服务的 application-docker.yml
- ✅ 统一的 Dockerfile
- ✅ MySQL 配置文件

---

## 🚀 快速开始

### 方式一：仅启动基础设施（推荐用于开发）

```bash
# 启动 MySQL、Redis、Nacos、Elasticsearch
docker-compose up -d mysql redis nacos elasticsearch

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f nacos
```

**然后在 IDEA 中启动微服务**（使用本地开发环境）

---

### 方式二：启动所有服务（完整 Docker 环境）

```bash
# 1. 构建并启动所有服务
docker-compose up -d --build

# 2. 查看服务状态
docker-compose ps

# 3. 查看日志
docker-compose logs -f

# 4. 查看特定服务日志
docker-compose logs -f gateway
docker-compose logs -f user-service
```

---

## 📋 服务访问地址

### 基础设施
- **MySQL**: `localhost:3306`
  - 用户名: `root`
  - 密码: `123456`

- **Redis**: `localhost:6379`
  - 密码: `123456`

- **Nacos**: `http://localhost:8848/nacos`
  - 用户名: `nacos`
  - 密码: `nacos`

- **Elasticsearch**: `http://localhost:9200`
  - 用户名: `elastic`
  - 密码: `123456`

### 微服务
- **网关**: `http://localhost:8080`
- **用户服务**: `http://localhost:8081`
- **社区服务**: `http://localhost:8082`
- **配件服务**: `http://localhost:8083`
- **维修服务**: `http://localhost:8084`

---

## 🔍 验证服务

### 1. 验证基础设施

```bash
# 验证 MySQL
docker exec -it efh-mysql mysql -uroot -p123456 -e "SHOW DATABASES;"

# 验证 Redis
docker exec -it efh-redis redis-cli -a 123456 ping

# 验证 Nacos（浏览器访问）
http://localhost:8848/nacos

# 验证 Elasticsearch
curl http://localhost:9200
```

### 2. 验证微服务

```bash
# 验证网关
curl http://localhost:8080/actuator/health

# 验证用户服务
curl http://localhost:8081/actuator/health

# 验证社区服务
curl http://localhost:8082/actuator/health

# 验证配件服务
curl http://localhost:8083/actuator/health

# 验证维修服务
curl http://localhost:8084/actuator/health
```

### 3. 验证网关路由

```bash
# 通过网关访问用户服务
curl http://localhost:8080/user/actuator/health

# 通过网关访问社区服务
curl http://localhost:8080/community/actuator/health

# 通过网关访问配件服务
curl http://localhost:8080/parts/actuator/health

# 通过网关访问维修服务
curl http://localhost:8080/service/actuator/health
```

---

## 🛠️ 常用命令

### 启动服务
```bash
# 启动所有服务
docker-compose up -d

# 启动特定服务
docker-compose up -d mysql redis nacos

# 重新构建并启动
docker-compose up -d --build
```

### 停止服务
```bash
# 停止所有服务
docker-compose stop

# 停止特定服务
docker-compose stop gateway

# 停止并删除容器
docker-compose down

# 停止并删除容器和数据卷（慎用！）
docker-compose down -v
```

### 查看状态
```bash
# 查看所有服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f mysql
docker-compose logs -f gateway

# 查看最近 100 行日志
docker-compose logs --tail=100 gateway
```

### 进入容器
```bash
# 进入 MySQL 容器
docker exec -it efh-mysql bash

# 进入 Redis 容器
docker exec -it efh-redis sh

# 进入网关容器
docker exec -it efh-gateway bash
```

### 重启服务
```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart gateway
```

---

## 📊 数据库信息

### 已创建的数据库

| 数据库名 | 用途 | 说明 |
|---------|------|------|
| nacos_config | Nacos 配置 | Nacos 配置中心数据 |
| efh_user_0 | 用户服务 | 用户数据（分库 0） |
| efh_user_1 | 用户服务 | 用户数据（分库 1） |
| efh_community_0 | 社区服务 | 帖子和评论（分库 0） |
| efh_community_1 | 社区服务 | 帖子和评论（分库 1） |
| efh_parts | 配件服务 | 配件和订单 |
| efh_service | 维修服务 | 维修工单和师傅 |

### 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | 123456 | 管理员账号 |
| test | 123456 | 测试账号 |
| user1 | 123456 | 普通用户 1 |
| user2 | 123456 | 普通用户 2 |

---

## 🐛 故障排查

### 1. 容器启动失败

```bash
# 查看容器日志
docker-compose logs 服务名

# 查看容器状态
docker-compose ps

# 重新构建
docker-compose build --no-cache 服务名
```

### 2. 数据库连接失败

```bash
# 检查 MySQL 是否启动
docker-compose ps mysql

# 查看 MySQL 日志
docker-compose logs mysql

# 进入 MySQL 容器检查
docker exec -it efh-mysql mysql -uroot -p123456
```

### 3. Nacos 无法访问

```bash
# 检查 Nacos 是否启动
docker-compose ps nacos

# 查看 Nacos 日志
docker-compose logs nacos

# 等待 Nacos 完全启动（需要 1-2 分钟）
```

### 4. 服务无法注册到 Nacos

- 检查服务的 bootstrap.yml 配置
- 确认 Nacos 命名空间 ID 正确
- 查看服务启动日志

### 5. 端口冲突

```bash
# 查看端口占用
netstat -ano | findstr :8080

# 修改 docker-compose.yml 中的端口映射
ports:
  - "8081:8080"  # 将主机端口改为 8081
```

---

## 📝 开发建议

### 推荐的开发流程

1. **本地开发**（推荐）
   ```bash
   # 只启动基础设施
   docker-compose up -d mysql redis nacos elasticsearch
   
   # 在 IDEA 中启动微服务
   # 这样可以快速调试和热部署
   ```

2. **集成测试**
   ```bash
   # 启动所有服务
   docker-compose up -d --build
   
   # 测试完整的服务调用链路
   ```

3. **生产部署**
   ```bash
   # 使用生产配置
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
   ```

---

## 🔄 数据持久化

所有数据都保存在 Docker 数据卷中：

```bash
# 查看数据卷
docker volume ls | grep efh

# 备份数据卷
docker run --rm -v mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data

# 恢复数据卷
docker run --rm -v mysql-data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /
```

---

## 🚀 下一步

### 选项 1：测试 Docker 环境
```bash
# 启动基础设施
docker-compose up -d mysql redis nacos

# 等待 1-2 分钟让服务完全启动

# 验证服务
docker-compose ps
```

### 选项 2：开发业务功能
- 使用 Docker 基础设施
- 在 IDEA 中启动微服务
- 开始开发用户注册登录功能

### 选项 3：完整 Docker 部署
```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f
```

---

## 💡 提示

1. **首次启动需要时间**
   - MySQL 初始化需要 30 秒
   - Nacos 启动需要 1-2 分钟
   - 微服务构建需要 5-10 分钟

2. **推荐开发方式**
   - 基础设施用 Docker
   - 微服务在 IDEA 中运行
   - 这样可以快速调试

3. **数据持久化**
   - 所有数据保存在 Docker 卷中
   - 删除容器不会丢失数据
   - 使用 `docker-compose down -v` 会删除数据

4. **资源占用**
   - 完整环境需要约 4GB 内存
   - 建议至少 8GB 系统内存

---

**现在可以开始测试 Docker 环境了！** 🎉

建议先执行：
```bash
docker-compose up -d mysql redis nacos
```

然后在 IDEA 中启动微服务进行开发。
