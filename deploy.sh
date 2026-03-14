#!/bin/bash
# 服务器部署脚本

# 服务器信息
SERVER_IP="8.219.135.26"
SERVER_USER="root"
SERVER_PASSWORD="aliyun2026_"
DEPLOY_DIR="/root/ev-forklift-hub"

echo "开始部署到服务器 $SERVER_IP..."

# 1. 创建部署目录
echo "1. 创建部署目录..."
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no $SERVER_USER@$SERVER_IP "mkdir -p $DEPLOY_DIR"

# 2. 上传 docker-compose.yml
echo "2. 上传 docker-compose.yml..."
sshpass -p "$SERVER_PASSWORD" scp -o StrictHostKeyChecking=no docker-compose.yml $SERVER_USER@$SERVER_IP:$DEPLOY_DIR/

# 3. 上传 docker 目录
echo "3. 上传 docker 配置..."
sshpass -p "$SERVER_PASSWORD" scp -r -o StrictHostKeyChecking=no docker $SERVER_USER@$SERVER_IP:$DEPLOY_DIR/

# 4. 上传各个模块的 target 目录
echo "4. 上传编译后的 JAR 文件..."
for module in efh-gateway efh-user efh-community efh-parts efh-service; do
    echo "   上传 $module..."
    sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no $SERVER_USER@$SERVER_IP "mkdir -p $DEPLOY_DIR/$module/target"
    sshpass -p "$SERVER_PASSWORD" scp -o StrictHostKeyChecking=no $module/target/*.jar $SERVER_USER@$SERVER_IP:$DEPLOY_DIR/$module/target/ 2>/dev/null || echo "   $module 没有 JAR 文件"
done

# 5. 上传前端构建文件
echo "5. 上传前端文件..."
sshpass -p "$SERVER_PASSWORD" scp -r -o StrictHostKeyChecking=no efh-web $SERVER_USER@$SERVER_IP:$DEPLOY_DIR/

# 6. 在服务器上启动服务
echo "6. 启动 Docker 服务..."
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no $SERVER_USER@$SERVER_IP << 'EOF'
cd /root/ev-forklift-hub
docker-compose down
docker-compose up -d --build
docker-compose ps
EOF

echo "部署完成！"
echo "访问地址："
echo "  前端: http://$SERVER_IP"
echo "  网关: http://$SERVER_IP:8080"
echo "  Nacos: http://$SERVER_IP:8848/nacos"
