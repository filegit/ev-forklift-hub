#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🔄 快速更新服务...${NC}"

# 拉取最新代码
echo -e "${YELLOW}📥 拉取最新代码...${NC}"
git pull origin main

# 只更新指定的服务
if [ -z "$1" ]; then
    echo -e "${YELLOW}🔨 更新所有服务...${NC}"
    docker-compose up -d --build
else
    echo -e "${YELLOW}🔨 更新服务: $1${NC}"
    docker-compose up -d --build $1
fi

# 查看日志
echo -e "${YELLOW}📋 查看日志...${NC}"
if [ -z "$1" ]; then
    docker-compose logs --tail=20
else
    docker-compose logs --tail=20 $1
fi

echo -e "${GREEN}✅ 更新完成！${NC}"
