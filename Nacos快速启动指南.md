# Nacos 快速启动指南

## 🚀 快速启动 Nacos

### 方式一：下载并启动 Nacos（推荐）

#### 1. 下载 Nacos

访问官网下载：
```
https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip
```

或使用国内镜像：
```
https://gitee.com/mirrors/Nacos/releases/download/2.2.3/nacos-server-2.2.3.zip
```

#### 2. 解压到任意目录

例如：`D:\nacos`

#### 3. 启动 Nacos（单机模式）

**Windows：**
```bash
cd D:\nacos\bin
startup.cmd -m standalone
```

**Linux/Mac：**
```bash
cd /path/to/nacos/bin
sh startup.sh -m standalone
```

#### 4. 验证启动成功

访问 Nacos 控制台：
```
http://localhost:8848/nacos
```

默认账号密码：`nacos` / `nacos`

---

### 方式二：使用 Docker 启动 Nacos

```bash
docker run -d \
  --name nacos-standalone \
  -e MODE=standalone \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.2.3
```

---

## ✅ 启动成功标志

### 1. 控制台输出

看到以下日志表示启动成功：
```
Nacos started successfully in stand alone mode. use embedded storage
```

### 2. 访问控制台

能够正常访问：http://localhost:8848/nacos

### 3. 端口监听

检查端口是否监听：
```bash
# Windows
netstat -ano | findstr :8848

# Linux/Mac
lsof -i :8848
```

---

## 🔧 配置 Nacos 命名空间

### 1. 登录 Nacos 控制台

访问：http://localhost:8848/nacos
账号：nacos
密码：nacos

### 2. 创建命名空间

1. 点击左侧菜单 "命名空间"
2. 点击右上角 "新建命名空间"
3. 填写信息：
   - **命名空间ID**: `dev`
   - **命名空间名**: `开发环境`
   - **描述**: `开发环境配置`
4. 点击 "确定"

### 3. 验证命名空间

在命名空间列表中应该看到新创建的 `dev` 命名空间。

---

## 📝 现在启动微服务

### 1. 确认 Nacos 已启动

```bash
# 访问 Nacos 控制台
http://localhost:8848/nacos
```

### 2. 确认其他服务已启动

- ✅ MySQL (3306)
- ✅ Redis (6379)

### 3. 按顺序启动微服务

在 IDEA 中启动：

1. **GatewayApplication** (8080)
2. **UserApplication** (8081)
3. **CommunityApplication** (8082)
4. **PartsApplication** (8083)
5. **ServiceApplication** (8084)

### 4. 验证服务注册

在 Nacos 控制台：
1. 切换到 `dev` 命名空间
2. 点击 "服务管理" > "服务列表"
3. 应该看到所有 5 个服务

---

## 🐛 常见问题

### 1. Nacos 启动失败

**问题：** 双击 `startup.cmd` 后窗口闪退

**解决方案：**
```bash
# 使用命令行启动，查看错误信息
cd D:\nacos\bin
startup.cmd -m standalone

# 或查看日志
type ..\logs\start.out
```

**常见原因：**
- Java 环境未配置
- 端口 8848 被占用
- 内存不足

### 2. 端口被占用

**错误信息：** `Address already in use: bind`

**解决方案：**
```bash
# Windows 查看端口占用
netstat -ano | findstr :8848

# 结束占用进程
taskkill /PID 进程ID /F

# 或修改 Nacos 端口
# 编辑 conf/application.properties
server.port=8849
```

### 3. 微服务连接 Nacos 失败

**错误信息：** `Client not connected, current status:STARTING`

**解决方案：**

1. **确认 Nacos 已启动**
   ```bash
   curl http://localhost:8848/nacos/
   ```

2. **检查命名空间是否存在**
   - 登录 Nacos 控制台
   - 确认 `dev` 命名空间已创建

3. **检查配置文件**
   - 确认 `bootstrap.yml` 中的 Nacos 地址正确
   - 确认命名空间 ID 为 `dev`

4. **增加超时时间**
   ```yaml
   spring:
     cloud:
       nacos:
         config:
           timeout: 10000  # 增加到 10 秒
   ```

### 4. 命名空间不存在

**错误信息：** `namespace not found`

**解决方案：**
- 在 Nacos 控制台创建 `dev` 命名空间
- 或修改 `bootstrap.yml` 使用 `public` 命名空间（留空）

```yaml
spring:
  cloud:
    nacos:
      config:
        namespace:   # 留空使用 public 命名空间
```

### 5. Nacos 内存不足

**解决方案：**

编辑 `bin/startup.cmd`（Windows）或 `bin/startup.sh`（Linux），调整 JVM 参数：

```bash
# 原配置
set "JAVA_OPT=%JAVA_OPT% -server -Xms2g -Xmx2g"

# 修改为（适合开发环境）
set "JAVA_OPT=%JAVA_OPT% -server -Xms512m -Xmx512m"
```

---

## 📊 Nacos 控制台功能

### 1. 服务管理
- 查看所有注册的服务
- 查看服务实例详情
- 服务上下线

### 2. 配置管理
- 集中管理配置文件
- 动态刷新配置
- 配置版本管理

### 3. 命名空间
- 环境隔离（dev、test、prod）
- 多租户管理

### 4. 集群管理
- 查看 Nacos 集群状态
- 节点管理

---

## 🎯 推荐配置

### 开发环境

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        namespace: dev
        enabled: true
        timeout: 10000
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: dev
        enabled: true
        register-enabled: true
```

### 生产环境

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: nacos-server:8848  # 使用域名
        namespace: prod
        enabled: true
        timeout: 5000
      discovery:
        server-addr: nacos-server:8848
        namespace: prod
        enabled: true
        register-enabled: true
        heart-beat-interval: 5000
        heart-beat-timeout: 15000
```

---

## 📞 需要帮助？

### 查看 Nacos 日志

```bash
# Windows
type D:\nacos\logs\nacos.log

# Linux/Mac
tail -f /path/to/nacos/logs/nacos.log
```

### 查看微服务日志

在 IDEA 控制台查看完整的错误信息。

---

## ✨ 启动步骤总结

1. ✅ 下载并解压 Nacos
2. ✅ 启动 Nacos：`startup.cmd -m standalone`
3. ✅ 访问控制台：http://localhost:8848/nacos
4. ✅ 创建 `dev` 命名空间
5. ✅ 启动 MySQL 和 Redis
6. ✅ 启动微服务
7. ✅ 在 Nacos 控制台验证服务注册

---

**现在启动 Nacos，然后重新启动微服务即可！** 🚀
