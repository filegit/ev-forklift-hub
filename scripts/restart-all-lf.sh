#!/bin/bash
set -e
cd /opt/ev-forklift-hub
export MYSQL_HOST=127.0.0.1 MYSQL_PASSWORD=123456 REDIS_HOST=127.0.0.1 REDIS_PASSWORD=123456 NACOS_SERVER_ADDR=127.0.0.1:8848
if [ -f .env.local ]; then
  set -a
  . ./.env.local
  set +a
fi
mkdir -p logs

echo "[1/4] 启动 Docker 基础设施..."
docker start efh-mysql efh-redis efh-nacos 2>/dev/null || docker compose up -d mysql redis nacos
for i in $(seq 1 30); do
  docker exec efh-mysql mysqladmin ping -h127.0.0.1 -uroot -p123456 --silent 2>/dev/null && break
  sleep 2
done
sleep 10

echo "[2/4] 停止旧 Java 进程..."
pkill -f 'efh-.*-1.0.0.jar' 2>/dev/null || true
sleep 2

start_jar() {
  local jar=$1
  local wait=${2:-0}
  [ -f "$jar" ] || return 0
  local name=$(basename "$jar" .jar)
  nohup java -Xms256m -Xmx512m -jar "$jar" >> "logs/${name}.log" 2>&1 &
  echo "  started $name pid=$!"
  [ "$wait" -gt 0 ] && sleep "$wait"
}

echo "[3/4] 启动微服务..."
start_jar efh-gateway/target/efh-gateway-1.0.0.jar 20
start_jar efh-user/target/efh-user-1.0.0.jar 3
start_jar efh-community/target/efh-community-1.0.0.jar 3
start_jar efh-parts/target/efh-parts-1.0.0.jar 3
start_jar efh-service/target/efh-service-1.0.0.jar 3
start_jar efh-knowledge/target/efh-knowledge-1.0.0.jar 3
start_jar efh-agent/target/efh-agent-1.0.0.jar 3

echo "[4/4] 重启前端 Nginx..."
docker rm -f efh-web 2>/dev/null || true
docker run -d --name efh-web --restart unless-stopped \
  --add-host=host.docker.internal:host-gateway \
  -p 80:80 -p 8888:8888 \
  -v /opt/ev-forklift-hub/efh-web/dist:/usr/share/nginx/html:ro \
  -v /opt/ev-forklift-hub/efh-web/nginx-all.conf:/etc/nginx/conf.d/default.conf:ro \
  nginx:alpine

sleep 25
echo "=== 健康检查 ==="
curl -s -o /dev/null -w "gateway:%{http_code}\n" http://127.0.0.1:8080/user/api/test || true
curl -s -o /dev/null -w "community:%{http_code}\n" "http://127.0.0.1:8080/community/api/post/list?page=1&size=10" || true
curl -s -o /dev/null -w "nginx80:%{http_code}\n" http://127.0.0.1/ || true
curl -s -o /dev/null -w "nginx8888-api:%{http_code}\n" "http://127.0.0.1:8888/api/community/api/post/list?page=1&size=10" || true
docker exec efh-web wget -qO- --timeout=5 http://host.docker.internal:8080/user/api/test 2>&1 | head -c 80 || echo "nginx->gateway FAIL"
echo
echo "DONE"
