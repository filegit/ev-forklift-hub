# 🚀 新能源叉车社区平台 - 完整部署指南

## 📋 项目概述

新能源叉车社区平台是一个基于微服务架构的完整解决方案，包含前端 Web 应用和后端微服务。

### 技术栈

**前端：**
- Vue 3 + Vite
- Element Plus
- Pinia + Vue Router

**后端：**
- Spring Boot + Spring Cloud
- Nacos (服务注册与配置)
- MySQL + Redis
- MyBatis-Plus

---

## 🐳 Docker 完整部署

### 前提条件

- Docker 20.10+
- Docker Compose 2.0+
- 服务器内存至少 4GB

---

### 第一步：准备项目文件

```bash
# 克隆项目（或上传到服务器）
git clone https://github.com/your-repo/ev-forklift-hub.git
cd ev-forklift-hub
```

---

### 第二步：启动基础设施

```bash
# 启动 MySQL、Redis、Nacos
docker-compose up -d mysql redis nacos

# 等待服务启动（约 30 秒）
docker-compose ps

# 查看 Nacos 日志
docker-compose logs -f nacos
```

**验证 Nacos：**
- 访问：http://localhost:8848/nacos
- 用户名：nacos
- 密码：nacos

---

### 第三步：启动后端微服务

```bash
# 启动网关
docker-compose up -d gateway

# 启动用户服务
docker-compose up -d user-service

# 启动社区服务
docker-compose up -d community-service

# 查看服务状态
docker-compose ps
```

**验证微服务：**
- 网关：http://localhost:8080
- 用户服务：http://localhost:8081
- 社区服务：http://localhost:8082

---

### 第四步：启动前端应用

```bash
# 构建并启动前端
docker-compose up -d web

# 查看前端日志
docker-compose logs -f web
```

**访问前端：**
- 前端地址：http://localhost

---

### 第五步：验证完整功能

#### 1. 注册用户

访问：http://localhost/register

```
用户名：testuser
密码：123456
昵称：测试用户
手机号：13800138000
```

#### 2. 登录

访问：http://localhost/login

#### 3. 发布帖子

点击顶部"发帖"按钮

#### 4. 查看帖子列表

首页自动显示

#### 5. 发表评论

点击帖子进入详情页，发表评论

---

## 📊 服务端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 Web | 80 | 前端应用 |
| API 网关 | 8080 | 统一入口 |
| 用户服务 | 8081 | 用户管理 |
| 社区服务 | 8082 | 帖子评论 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 服务注册 |

---

## 🔧 常用命令

### 查看所有服务状态

```bash
docker-compose ps
```

### 查看服务日志

```bash
# 查看所有日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f web
docker-compose logs -f gateway
docker-compose logs -f user-service
docker-compose logs -f community-service
```

### 重启服务

```bash
# 重启前端
docker-compose restart web

# 重启网关
docker-compose restart gateway

# 重启所有服务
docker-compose restart
```

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 更新服务

```bash
# 重新构建并启动
docker-compose up -d --build web
docker-compose up -d --build gateway
```

---

## 🌐 生产环境部署

### 1. 服务器配置建议

**最低配置：**
- CPU: 2核
- 内存: 4GB
- 硬盘: 50GB
- 系统: Ubuntu 20.04+ / CentOS 7+

**推荐配置：**
- CPU: 4核
- 内存: 8GB
- 硬盘: 100GB

---

### 2. 安装 Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com | sh
sudo systemctl start docker
sudo systemctl enable docker

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

---

### 3. 配置防火墙

```bash
# 开放端口
sudo ufw allow 80/tcp    # 前端
sudo ufw allow 8080/tcp  # 网关
sudo ufw allow 8848/tcp  # Nacos
sudo ufw enable
```

---

### 4. 配置域名（可选）

**Nginx 反向代理：**

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

### 5. 配置 HTTPS（可选）

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

---

## 📝 环境变量配置

### 修改 docker-compose.yml

```yaml
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: your_strong_password  # 修改密码
  
  redis:
    command: redis-server --requirepass your_redis_password  # 修改密码
  
  nacos:
    environment:
      MYSQL_SERVICE_PASSWORD: your_strong_password  # 与 MySQL 密码一致
```

---

## 🔍 监控和日志

### 查看容器资源使用

```bash
docker stats
```

### 查看磁盘使用

```bash
docker system df
```

### 清理无用资源

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的数据卷
docker volume prune
```

---

## 🐛 故障排查

### 问题1：服务无法启动

```bash
# 查看详细日志
docker-compose logs service-name

# 检查端口占用
netstat -tulpn | grep :8080

# 重启服务
docker-compose restart service-name
```

### 问题2：数据库连接失败

```bash
# 检查 MySQL 是否健康
docker-compose ps mysql

# 进入 MySQL 容器
docker exec -it efh-mysql mysql -uroot -p123456

# 检查数据库
SHOW DATABASES;
```

### 问题3：前端无法访问后端

```bash
# 检查网关是否启动
docker-compose ps gateway

# 检查网络连接
docker network inspect ev-forklift-hub_efh-network

# 测试网关接口
curl http://localhost:8080/user/api/test
```

---

## 📦 备份和恢复

### 备份数据库

```bash
# 备份所有数据库
docker exec efh-mysql mysqldump -uroot -p123456 --all-databases > backup.sql

# 备份特定数据库
docker exec efh-mysql mysqldump -uroot -p123456 efh_user_0 > user_backup.sql
```

### 恢复数据库

```bash
# 恢复数据库
docker exec -i efh-mysql mysql -uroot -p123456 < backup.sql
```

### 备份 Redis

```bash
# 备份 Redis 数据
docker exec efh-redis redis-cli -a 123456 SAVE
docker cp efh-redis:/data/dump.rdb ./redis_backup.rdb
```

---

## 🎉 完成！

现在你的新能源叉车社区平台已经完全部署成功！

**访问地址：**
- 前端：http://your-server-ip
- 后端网关：http://your-server-ip:8080
- Nacos 控制台：http://your-server-ip:8848/nacos

**默认账号：**
- Nacos：nacos / nacos

---

## 📞 技术支持

如有问题，请联系：support@efh.com

---

**祝你使用愉快！** 🚀
