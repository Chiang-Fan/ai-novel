#!/bin/bash

###############################################################################
# AI Novel Writer - 构建和部署脚本
# 用途: 一键构建Docker镜像并部署应用
###############################################################################

set -e

echo "========================================="
echo " AI Novel Writer - 构建和部署"
echo "========================================="
echo

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 停止并清理旧容器
echo -e "${YELLOW}[1/5] 停止旧容器...${NC}"
docker-compose down -v 2>/dev/null || true
docker rm -f ai-novel-app ai-novel-mysql 2>/dev/null || true
echo -e "${GREEN}✓ 旧容器已清理${NC}"
echo

# 2. 创建必要的目录
echo -e "${YELLOW}[2/5] 创建数据目录...${NC}"
mkdir -p docker/data/mysql
mkdir -p docker/data/app
mkdir -p docker/logs
echo -e "${GREEN}✓ 目录创建完成${NC}"
echo

# 3. 构建Docker镜像
echo -e "${YELLOW}[3/5] 构建Docker镜像(这可能需要几分钟)...${NC}"
if docker build --platform linux/amd64 -t ai-novel-writer:latest . ; then
    echo -e "${GREEN}✓ Docker镜像构建成功${NC}"
else
    echo -e "${RED}✗ Docker镜像构建失败${NC}"
    exit 1
fi
echo

# 4. 启动服务
echo -e "${YELLOW}[4/5] 启动服务...${NC}"
export MYSQL_PORT=3307
if docker-compose up -d ; then
    echo -e "${GREEN}✓ 服务启动成功${NC}"
else
    echo -e "${RED}✗ 服务启动失败${NC}"
    exit 1
fi
echo

# 5. 等待服务就绪
echo -e "${YELLOW}[5/5] 等待服务就绪...${NC}"
echo "等待MySQL启动..."
for i in {1..30}; do
    if docker-compose ps | grep -q "ai-novel-mysql.*healthy"; then
        echo -e "${GREEN}✓ MySQL已就绪${NC}"
        break
    fi
    echo -n "."
    sleep 2
done
echo

echo "等待应用启动(最多60秒)..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 应用已就绪${NC}"
        break
    fi
    echo -n "."
    sleep 2
done
echo

# 6. 显示状态
echo
echo "========================================="
echo " 部署完成!"
echo "========================================="
echo
docker-compose ps
echo
echo -e "${GREEN}服务地址:${NC}"
echo "  - 应用: http://localhost:8080"
echo "  - 健康检查: http://localhost:8080/api/health"
echo "  - MySQL: localhost:3307"
echo
echo -e "${YELLOW}查看日志:${NC}"
echo "  docker-compose logs -f app"
echo
echo -e "${YELLOW}停止服务:${NC}"
echo "  docker-compose down"
echo
