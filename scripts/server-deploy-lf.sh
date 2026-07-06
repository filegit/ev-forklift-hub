#!/bin/bash
set -e
cd /opt/ev-forklift-hub

log() { echo "[$(date +%H:%M:%S)] $*"; }

log "=== 安装 Docker 静态二进制 (跳过 apt) ==="
if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL -o /tmp/docker.tgz \
    "https://download.docker.com/linux/static/stable/x86_64/docker-27.3.1.tgz"
  tar -xzf /tmp/docker.tgz -C /tmp
  cp /tmp/docker/* /usr/local/bin/
  chmod +x /usr/local/bin/docker*
  groupadd docker 2>/dev/null || true

  cat > /etc/systemd/system/docker.service <<'UNIT'
[Unit]
Description=Docker Application Container Engine
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
ExecStart=/usr/local/bin/dockerd
Restart=always
LimitNOFILE=1048576

[Install]
WantedBy=multi-user.target
UNIT

  systemctl daemon-reload
  systemctl enable docker
  systemctl start docker
  sleep 5
fi
docker --version

if ! docker compose version >/dev/null 2>&1; then
  curl -fsSL -o /usr/local/bin/docker-compose \
    "https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-linux-x86_64"
  chmod +x /usr/local/bin/docker-compose
  mkdir -p /usr/local/lib/docker/cli-plugins
  ln -sf /usr/local/bin/docker-compose /usr/local/lib/docker/cli-plugins/docker-compose
fi

log "=== 安装便携 JRE 17 ==="
if ! command -v java >/dev/null 2>&1; then
  JRE_DIR="/opt/jre17"
  if [ ! -x "$JRE_DIR/bin/java" ]; then
    curl -fsSL -o /tmp/jre17.tar.gz \
      "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.15%2B6/OpenJDK17U-jre_x64_linux_hotspot_17.0.15_6.tar.gz"
    rm -rf "$JRE_DIR"
    mkdir -p "$JRE_DIR"
    tar -xzf /tmp/jre17.tar.gz -C "$JRE_DIR" --strip-components=1
  fi
  ln -sf "$JRE_DIR/bin/java" /usr/local/bin/java
fi
java -version

log "=== 启动 MySQL Redis Nacos ==="
docker compose up -d mysql redis nacos 2>/dev/null || docker-compose up -d mysql redis nacos
log "等待基础设施就绪..."
for i in $(seq 1 50); do
  if docker exec efh-mysql mysqladmin ping -h127.0.0.1 -uroot -p123456 --silent 2>/dev/null; then
    log "MySQL ready"
    break
  fi
  sleep 4
done
sleep 20

log "=== 初始化数据库 ==="
docker exec -i efh-mysql mysql -uroot -p123456 < fix-host-db.sql 2>/dev/null || true
for f in docker/mysql/init/*.sql; do
  [ -f "$f" ] && docker exec -i efh-mysql mysql -uroot -p123456 < "$f" 2>/dev/null || true
done

log "=== 启动 Java 微服务 ==="
mkdir -p logs
export MYSQL_HOST=127.0.0.1 MYSQL_PASSWORD=123456 REDIS_HOST=127.0.0.1 REDIS_PASSWORD=123456 NACOS_SERVER_ADDR=127.0.0.1:8848

start_jar() {
  local jar=$1
  [ -f "$jar" ] || { log "SKIP missing $jar"; return 0; }
  local name
  name=$(basename "$jar" .jar)
  pkill -f "$name" 2>/dev/null || true
  sleep 1
  nohup java -Xms256m -Xmx512m -jar "$jar" >> "logs/${name}.log" 2>&1 &
  log "Started $name PID=$!"
}

start_jar efh-gateway/target/efh-gateway-1.0.0.jar
sleep 15
start_jar efh-user/target/efh-user-1.0.0.jar
start_jar efh-community/target/efh-community-1.0.0.jar
start_jar efh-parts/target/efh-parts-1.0.0.jar
start_jar efh-service/target/efh-service-1.0.0.jar
start_jar efh-knowledge/target/efh-knowledge-1.0.0.jar
start_jar efh-agent/target/efh-agent-1.0.0.jar

log "=== 构建并启动前端 ==="
docker rm -f efh-web 2>/dev/null || true
cd /opt/ev-forklift-hub/efh-web
docker build -t efh-web:prod .
docker run -d --name efh-web --restart unless-stopped \
  --add-host=host.docker.internal:host-gateway \
  -p 80:80 efh-web:prod

log "=== 开放防火墙 ==="
iptables -I INPUT -p tcp --dport 80 -j ACCEPT 2>/dev/null || true
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT 2>/dev/null || true

sleep 20
log "=== 健康检查 ==="
curl -s -o /dev/null -w "gateway:%{http_code}\n" http://127.0.0.1:8080/user/api/test || true
curl -s -o /dev/null -w "service:%{http_code}\n" http://127.0.0.1:8084/actuator/health || true
curl -s -o /dev/null -w "web:%{http_code}\n" http://127.0.0.1:80/ || true
log "DEPLOY_DONE"
