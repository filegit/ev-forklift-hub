# Redis 配置说明

## ⚠️ 重要：Redis 密码配置

根据您的 Redis 配置情况，选择以下方案之一：

---

## 方案一：Redis 没有设置密码（推荐用于开发环境）

### 1. 检查 Redis 是否需要密码

打开命令行测试：
```bash
redis-cli
ping
```

如果返回 `PONG`，说明 Redis **没有密码**，无需配置密码。

### 2. 配置文件设置

所有服务的 `application.yml` 中，**删除或留空** `password` 配置项：

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password:   # 留空或删除此行
    database: 0

redisson:
  address: redis://127.0.0.1:6379
  password:   # 留空或删除此行
  database: 0
```

---

## 方案二：Redis 设置了密码

### 1. 查看 Redis 密码

如果您设置了 Redis 密码，需要在配置文件中填写。

测试连接：
```bash
redis-cli
auth 你的密码
ping
```

如果返回 `PONG`，说明密码正确。

### 2. 配置文件设置

在所有服务的 `application.yml` 中，**填写密码**：

#### efh-user/src/main/resources/application.yml
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 你的Redis密码  # 填写实际密码
    database: 0

redisson:
  address: redis://127.0.0.1:6379
  password: 你的Redis密码  # 填写实际密码
  database: 0
```

#### efh-community/src/main/resources/application.yml
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 你的Redis密码  # 填写实际密码
    database: 1

redisson:
  address: redis://127.0.0.1:6379
  password: 你的Redis密码  # 填写实际密码
  database: 1
```

#### efh-parts/src/main/resources/application.yml
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 你的Redis密码  # 填写实际密码
    database: 2

redisson:
  address: redis://127.0.0.1:6379
  password: 你的Redis密码  # 填写实际密码
  database: 2
```

#### efh-service/src/main/resources/application.yml
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 你的Redis密码  # 填写实际密码
    database: 3

redisson:
  address: redis://127.0.0.1:6379
  password: 你的Redis密码  # 填写实际密码
  database: 3
```

#### efh-gateway/src/main/resources/application.yml
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 你的Redis密码  # 填写实际密码
    database: 0
```

---

## 方案三：移除 Redis 密码（推荐用于开发环境）

### 1. 修改 Redis 配置文件

找到 Redis 配置文件 `redis.conf` 或 `redis.windows.conf`：

```bash
# 注释掉或删除这一行
# requirepass 你的密码
```

### 2. 重启 Redis

```bash
# Windows
redis-server.exe redis.windows.conf

# 或者重启 Redis 服务
net stop redis
net start redis
```

### 3. 验证

```bash
redis-cli
ping
```

应该直接返回 `PONG`，无需输入密码。

---

## 🔧 已完成的修复

✅ 在 `RedissonConfig.java` 中添加了密码配置支持
✅ 在所有服务的 `application.yml` 中添加了 `password` 配置项
✅ 支持有密码和无密码两种模式

---

## 📝 配置文件位置

需要修改以下文件：
- `efh-user/src/main/resources/application.yml`
- `efh-community/src/main/resources/application.yml`
- `efh-parts/src/main/resources/application.yml`
- `efh-service/src/main/resources/application.yml`
- `efh-gateway/src/main/resources/application.yml`

---

## 🚀 修改后的操作

1. **根据您的 Redis 配置，选择上述方案之一**
2. **修改所有服务的 application.yml 文件**
3. **重新启动服务**

---

## ✅ 验证 Redis 连接

启动服务后，如果看到以下日志，说明 Redis 连接成功：

```
========== XXX服务启动成功 ==========
```

如果仍然报错 `NOAUTH Authentication required`，说明：
- Redis 设置了密码，但配置文件中没有填写
- 或者填写的密码不正确

---

## 🐛 常见问题

### 1. 如何查看 Redis 是否设置了密码？

```bash
redis-cli
CONFIG GET requirepass
```

返回：
- `1) "requirepass" 2) ""` - 没有密码
- `1) "requirepass" 2) "你的密码"` - 有密码

### 2. 如何设置 Redis 密码？

```bash
redis-cli
CONFIG SET requirepass "你的密码"
```

### 3. 如何取消 Redis 密码？

```bash
redis-cli
AUTH 你的密码
CONFIG SET requirepass ""
```

---

## 📞 需要帮助？

如果按照以上步骤操作后仍有问题：
1. 确认 Redis 服务已启动
2. 确认 Redis 端口为 6379
3. 使用 `redis-cli` 测试连接
4. 检查配置文件中的密码是否正确

---

**推荐：开发环境使用无密码模式，生产环境必须设置密码！** 🔒
