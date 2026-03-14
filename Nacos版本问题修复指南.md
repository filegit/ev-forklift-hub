# Nacos 版本问题修复指南

## 🔍 问题分析

错误信息：`Client not connected, current status:STARTING`
- Nacos Client 版本：2.2.0（项目中）
- Nacos Server 版本：2.2.3（Docker）

虽然版本接近，但可能存在兼容性问题。

---

## ✅ 解决方案（推荐方案一）

### 方案一：暂时禁用 Nacos 配置中心，只使用服务发现

这是最快的解决方案，不需要修改依赖版本。

#### 修改所有 bootstrap.yml

将 `config.enabled` 改为 `false`：

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: false  # 禁用配置中心
      discovery:
        enabled: true   # 保留服务发现
```

这样服务可以正常启动，并且能注册到 Nacos，只是不从 Nacos 读取配置。

---

## 🔧 解决方案（方案二）

### 方案二：升级 Nacos Client 到 2.2.3

修改父 pom.xml，明确指定 Nacos Client 版本。

#### 1. 修改 pom.xml

在 `<properties>` 中添加：

```xml
<properties>
    <!-- 其他配置... -->
    <nacos.client.version>2.2.3</nacos.client.version>
</properties>
```

在 `<dependencyManagement>` 中添加：

```xml
<dependencyManagement>
    <dependencies>
        <!-- 其他依赖... -->
        
        <!-- 明确指定 Nacos Client 版本 -->
        <dependency>
            <groupId>com.alibaba.nacos</groupId>
            <artifactId>nacos-client</artifactId>
            <version>${nacos.client.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 2. 刷新 Maven 依赖

```bash
mvn clean install -U
```

#### 3. 重新启动服务

---

## 🐳 解决方案（方案三）

### 方案三：使用 Nacos 2.2.0 服务端

修改 docker-compose.yml，使用与客户端匹配的 Nacos 版本：

```yaml
nacos:
  image: nacos/nacos-server:v2.2.0  # 改为 2.2.0
  # 其他配置保持不变
```

然后重启 Docker：

```bash
docker-compose down
docker-compose up -d nacos
```

---

## 🎯 推荐做法

### 开发环境（推荐方案一）

**暂时禁用 Nacos 配置中心**，使用本地配置文件：

优点：
- ✅ 快速启动
- ✅ 不需要修改依赖
- ✅ 配置文件在本地，方便调试

缺点：
- ❌ 无法使用配置中心的动态刷新功能

### 生产环境（推荐方案二）

**升级 Nacos Client 到 2.2.3**：

优点：
- ✅ 版本统一
- ✅ 可以使用所有 Nacos 功能
- ✅ 更好的兼容性

缺点：
- ❌ 需要重新下载依赖

---

## 📝 详细步骤（方案一 - 最快）

### 1. 修改所有 bootstrap.yml

我来帮您修改所有文件，将 `config.enabled` 改为 `false`。

### 2. 重新启动服务

直接在 IDEA 中启动服务即可。

### 3. 验证

服务应该能正常启动，并且在 Nacos 控制台能看到服务注册。

---

## 🔍 如何验证 Nacos 连接

### 1. 检查 Docker Nacos 是否运行

```bash
docker ps | grep nacos
```

### 2. 访问 Nacos 控制台

```
http://localhost:8848/nacos
```

### 3. 检查端口

```bash
netstat -ano | findstr :8848
```

### 4. 查看 Nacos 日志

```bash
docker logs nacos-standalone
```

---

## ⚠️ 注意事项

### 1. 配置中心 vs 服务发现

- **配置中心**：从 Nacos 读取配置文件
- **服务发现**：服务注册和发现

两者是独立的功能，可以单独使用。

### 2. 当前配置模式

禁用配置中心后：
- ✅ 配置文件：使用本地 `application.yml`
- ✅ 服务注册：注册到 Nacos
- ✅ 服务发现：从 Nacos 获取服务列表
- ✅ 负载均衡：通过 Ribbon/LoadBalancer

### 3. 版本兼容性

Spring Cloud Alibaba 2021.0.5.0 内置的 Nacos Client 版本：
- 默认版本：2.2.0
- 推荐 Nacos Server：2.2.0 或 2.2.3

---

## 🚀 立即执行

我现在帮您修改所有 bootstrap.yml，将配置中心禁用。

修改后您就可以直接启动服务了！

---

**选择方案一最快，我现在就帮您修改！** 🎉
