# 多阶段构建 Dockerfile
# 适用于所有微服务模块

# 第一阶段：构建
FROM maven:3.8.6-openjdk-8 AS builder

# 设置工作目录
WORKDIR /build

# 配置 Maven 使用阿里云镜像
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings>' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyun</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# 复制 pom.xml 文件
COPY pom.xml .
COPY efh-common/pom.xml efh-common/
COPY efh-gateway/pom.xml efh-gateway/
COPY efh-user/pom.xml efh-user/
COPY efh-community/pom.xml efh-community/
COPY efh-parts/pom.xml efh-parts/
COPY efh-service/pom.xml efh-service/

# 复制源代码
COPY efh-common efh-common/
COPY efh-gateway efh-gateway/
COPY efh-user efh-user/
COPY efh-community efh-community/
COPY efh-parts efh-parts/
COPY efh-service efh-service/

# 构建参数：指定要构建的模块
ARG MODULE_NAME

# 编译打包
RUN mvn clean package -pl ${MODULE_NAME} -am -DskipTests

# 第二阶段：运行
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 构建参数
ARG MODULE_NAME

# 从构建阶段复制 jar 包
COPY --from=builder /build/${MODULE_NAME}/target/*.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai

# 暴露端口
EXPOSE 8080 8081 8082 8083 8084

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
