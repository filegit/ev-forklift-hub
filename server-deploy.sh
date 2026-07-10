# 服务器一键部署脚本（在 111.170.36.78 上执行）
# 用法: bash server-deploy.sh

set -e
DEPLOY_DIR=/opt/ev-forklift-hub
cd "$DEPLOY_DIR"

echo "=== 1. 启动基础设施 ==="
docker compose up -d mysql redis nacos 2>/dev/null || docker-compose up -d mysql redis nacos
sleep 30

echo "=== 2. 初始化数据库 ==="
docker exec -i efh-mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD:-123456}" < docker/mysql/init/06-agent.sql 2>/dev/null || true
docker exec -i efh-mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD:-123456}" < fix-host-db.sql 2>/dev/null || true

echo "=== 3. 构建并启动全部服务 ==="
docker compose up -d --build 2>/dev/null || docker-compose up -d --build

echo "=== 4. 启动 Java 微服务（若未 Docker 化）==="
mkdir -p logs
for env_file in .env.local alipay.env sms.env; do
  if [ -f "$env_file" ]; then
    set -a
    . "./$env_file"
    set +a
    echo "loaded $env_file"
  fi
done
for svc in gateway:8080 user:8081 community:8082 parts:8083 service:8084 knowledge:8085 agent:8086; do
  name="${svc%%:*}"
  port="${svc##*:}"
  jar=$(ls efh-$name/target/*.jar 2>/dev/null | head -1)
  if [ -n "$jar" ]; then
    pkill -f "efh-$name" 2>/dev/null || true
    nohup java -Xms256m -Xmx512m -jar "$jar" > logs/$name.log 2>&1 &
    echo "Started efh-$name on $port"
  fi
done

echo "=== 部署完成 ==="
echo "前端: http://$(curl -s ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}'):80"
echo "网关: http://$(curl -s ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}'):8080"
