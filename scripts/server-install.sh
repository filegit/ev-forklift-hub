#!/bin/bash
# 在服务器 /opt/ev-forklift-hub 执行
set -e
export DEBIAN_FRONTEND=noninteractive
cd /opt/ev-forklift-hub

echo "=== 安装依赖 ==="
apt-get update -qq
apt-get install -y -qq openjdk-17-jre-headless curl ca-certificates gnupg lsb-release nginx 2>/dev/null || true

if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sh
  systemctl enable docker
  systemctl start docker
fi

if ! docker compose version >/dev/null 2>&1; then
  apt-get install -y -qq docker-compose-plugin 2>/dev/null || {
    curl -L "https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
  }
fi

echo "=== 启动 MySQL Redis Nacos ==="
docker compose up -d mysql redis nacos 2>/dev/null || docker-compose up -d mysql redis nacos
echo "等待数据库..."
sleep 35

echo "=== 初始化数据库 ==="
docker exec -i efh-mysql mysql -uroot -p123456 < fix-host-db.sql 2>/dev/null || true
for f in docker/mysql/init/*.sql; do
  [ -f "$f" ] && docker exec -i efh-mysql mysql -uroot -p123456 < "$f" 2>/dev/null || true
done

echo "=== 启动 Java 微服务 ==="
mkdir -p logs
export MYSQL_HOST=127.0.0.1 MYSQL_PASSWORD=123456 REDIS_HOST=127.0.0.1 REDIS_PASSWORD=123456 NACOS_SERVER_ADDR=127.0.0.1:8848
for env_file in .env.local alipay.env sms.env; do
  if [ -f "$env_file" ]; then
    set -a
    . "./$env_file"
    set +a
    echo "loaded $env_file"
  fi
done

start_jar() {
  local jar=$1
  [ -f "$jar" ] || return 0
  local name=$(basename "$jar" .jar)
  pkill -f "$name" 2>/dev/null || true
  sleep 1
  nohup java -Xms256m -Xmx512m -jar "$jar" >> logs/${name}.log 2>&1 &
  echo "Started $name PID=$!"
}

start_jar efh-gateway/target/efh-gateway-1.0.0.jar
sleep 8
start_jar efh-user/target/efh-user-1.0.0.jar
start_jar efh-community/target/efh-community-1.0.0.jar
start_jar efh-parts/target/efh-parts-1.0.0.jar
start_jar efh-service/target/efh-service-1.0.0.jar
start_jar efh-knowledge/target/efh-knowledge-1.0.0.jar
start_jar efh-agent/target/efh-agent-1.0.0.jar

echo "=== 配置 Nginx 前端 ==="
if [ -d efh-web/dist ]; then
  rm -rf /var/www/efh-web
  mkdir -p /var/www/efh-web
  cp -r efh-web/dist/* /var/www/efh-web/
fi

cat > /etc/nginx/sites-available/efh <<'NGINX'
server {
    listen 80 default_server;
    server_name _;
    root /var/www/efh-web;
    index index.html;

    location ~ ^/(user|community|parts|service|knowledge|agent)/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_read_timeout 120s;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX

ln -sf /etc/nginx/sites-available/efh /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx 2>/dev/null || nginx -s reload 2>/dev/null || true

echo "=== 开放防火墙 ==="
iptables -I INPUT -p tcp --dport 80 -j ACCEPT 2>/dev/null || true
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT 2>/dev/null || true

sleep 10
echo "=== 健康检查 ==="
curl -s -o /dev/null -w "gateway:%{http_code}\n" http://127.0.0.1:8080/user/api/test || true
curl -s -o /dev/null -w "service:%{http_code}\n" http://127.0.0.1:8084/actuator/health || true
echo "DEPLOY_DONE"
