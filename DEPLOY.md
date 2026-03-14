# 🚀 快速部署指南

## 📋 部署前检查清单

- [ ] 服务器已安装 Docker 和 Docker Compose
- [ ] 已配置好域名（可选）
- [ ] 已修改 docker-compose.yml 中的密码
- [ ] 已上传项目到服务器

---

## 🎯 一键部署

### Linux/Mac

```bash
# 赋予执行权限
chmod +x deploy.sh

# 执行部署
./deploy.sh
```

### Windows

```bash
# 双击运行
deploy.bat
```

---

## 🔄 快速更新

### 更新所有服务

```bash
./update.sh
```

### 更新特定服务

```bash
# 只更新前端
./update.sh web

# 只更新网关
./update.sh gateway

# 只更新用户服务
./update.sh user-service

# 只更新社区服务
./update.sh community-service
```

---

## 📊 常用命令

### 查看服务状态

```bash
docker-compose ps
```

### 查看日志

```bash
# 查看所有日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f web
docker-compose logs -f gateway
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart web
```

### 停止服务

```bash
docker-compose down
```

---

## 🐛 故障排查

### 服务无法启动

```bash
# 查看详细日志
docker-compose logs service-name

# 检查端口占用
netstat -tulpn | grep :8080

# 重新构建
docker-compose up -d --build --force-recreate
```

### 数据库连接失败

```bash
# 检查 MySQL 是否健康
docker-compose ps mysql

# 进入 MySQL 容器
docker exec -it efh-mysql mysql -uroot -p
```

### 前端无法访问

```bash
# 检查 Nginx 配置
docker exec efh-web nginx -t

# 查看 Nginx 日志
docker logs efh-web
```

---

## 📦 备份和恢复

### 备份数据

```bash
# 备份脚本
./backup.sh
```

### 恢复数据

```bash
# 恢复脚本
./restore.sh backup_20240309.tar.gz
```

---

## 🌐 访问地址

- **前端：** http://your-server-ip
- **网关：** http://your-server-ip:8080
- **Nacos：** http://your-server-ip:8848/nacos

---

**详细文档见：`服务器部署完整指南.md`**
