# JVM启动参数配置文档

## 用户服务 (efh-user) - 端口8081

```bash
java -jar efh-user.jar \
  -Xms512m \
  -Xmx1024m \
  -Xmn256m \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=logs/heapdump.hprof \
  -XX:+PrintGCDetails \
  -XX:+PrintGCDateStamps \
  -Xloggc:logs/gc.log \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=5 \
  -XX:GCLogFileSize=10M
```

## 社区服务 (efh-community) - 端口8082

```bash
java -jar efh-community.jar \
  -Xms512m \
  -Xmx1024m \
  -Xmn256m \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=logs/heapdump.hprof
```

## 配件服务 (efh-parts) - 端口8083

```bash
java -jar efh-parts.jar \
  -Xms512m \
  -Xmx1024m \
  -Xmn256m \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

## 维修服务 (efh-service) - 端口8084

```bash
java -jar efh-service.jar \
  -Xms512m \
  -Xmx1024m \
  -Xmn256m \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

## 网关服务 (efh-gateway) - 端口8080

```bash
java -jar efh-gateway.jar \
  -Xms256m \
  -Xmx512m \
  -Xmn128m \
  -XX:MetaspaceSize=128m \
  -XX:MaxMetaspaceSize=256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100
```

## JVM参数说明

### 堆内存配置
- `-Xms`: 初始堆大小
- `-Xmx`: 最大堆大小
- `-Xmn`: 年轻代大小

### 元空间配置
- `-XX:MetaspaceSize`: 元空间初始大小
- `-XX:MaxMetaspaceSize`: 元空间最大大小

### GC配置
- `-XX:+UseG1GC`: 使用G1垃圾收集器（适合大内存、低延迟场景）
- `-XX:MaxGCPauseMillis`: 最大GC停顿时间目标

### 故障诊断
- `-XX:+HeapDumpOnOutOfMemoryError`: OOM时自动生成堆转储文件
- `-XX:HeapDumpPath`: 堆转储文件路径
- `-XX:+PrintGCDetails`: 打印GC详细信息
- `-XX:+PrintGCDateStamps`: 打印GC时间戳
- `-Xloggc`: GC日志文件路径

## 性能调优建议

1. **根据实际负载调整堆内存大小**
2. **监控GC日志，优化GC参数**
3. **使用JVM监控工具（JConsole、VisualVM、Arthas）**
4. **定期分析堆转储文件，排查内存泄漏**
