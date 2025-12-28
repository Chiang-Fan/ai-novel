#!/bin/bash

###############################################################################
# AI Novel Writer - Docker 部署验证脚本
# 自动化验证所有部署功能
###############################################################################

set -e

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASS_COUNT=0
FAIL_COUNT=0

log_pass() {
    echo -e "${GREEN}✓ PASS${NC} $1"
    ((PASS_COUNT++))
}

log_fail() {
    echo -e "${RED}✗ FAIL${NC} $1"
    ((FAIL_COUNT++))
}

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo "========================================="
echo "AI Novel Writer - 部署验证"
echo "========================================="
echo ""

# 1. 容器状态检查
log_info "检查容器状态..."
if docker ps | grep -q "ai-novel-mysql.*Up"; then
    log_pass "MySQL容器运行正常"
else
    log_fail "MySQL容器未运行"
fi

if docker ps | grep -q "ai-novel-app.*Up"; then
    log_pass "应用容器运行正常"
else
    log_fail "应用容器未运行"
fi

# 2. 健康检查
log_info "执行健康检查..."
sleep 2
HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/health 2>/dev/null || echo "failed")
if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
    log_pass "应用健康检查通过"
else
    log_fail "应用健康检查失败"
fi

# 3. 数据库连接检查
log_info "检查数据库连接..."
if docker exec ai-novel-mysql mysql -unovel_user -pnovel_pass_123456 -e "SELECT 1" ai_novel_writer > /dev/null 2>&1; then
    log_pass "数据库连接正常"
else
    log_fail "数据库连接失败"
fi

# 4. API功能测试
log_info "测试API功能..."

# 创建小说
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/novels \
  -H "Content-Type: application/json" \
  -d '{"title":"验证测试小说","author":"系统","type":"测试"}' 2>/dev/null || echo "failed")

if echo "$CREATE_RESPONSE" | grep -q "验证测试小说"; then
    log_pass "创建小说API正常"
    NOVEL_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
else
    log_fail "创建小说API失败"
    NOVEL_ID=""
fi

# 查询小说
if curl -s http://localhost:8080/api/novels | grep -q "验证测试小说"; then
    log_pass "查询小说API正常"
else
    log_fail "查询小说API失败"
fi

# 5. 数据持久化验证
log_info "验证数据持久化..."
DATA_FILES=$(ls -la docker/data/mysql/ 2>/dev/null | wc -l)
if [ "$DATA_FILES" -gt 3 ]; then
    log_pass "MySQL数据文件存在"
else
    log_fail "MySQL数据文件不存在"
fi

# 6. 日志文件检查
log_info "检查日志文件..."
if [ -d "docker/logs" ] && [ "$(ls -A docker/logs 2>/dev/null)" ]; then
    log_pass "应用日志文件存在"
else
    log_warning "应用日志目录为空（可能刚启动）"
fi

# 7. 网络连接检查
log_info "检查容器网络..."
if docker network ls | grep -q "ai-novel-network"; then
    log_pass "Docker网络正常"
else
    log_fail "Docker网络不存在"
fi

# 8. 前端访问测试（如果有）
log_info "测试前端访问..."
FRONTEND_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
if [ "$FRONTEND_RESPONSE" = "200" ] || [ "$FRONTEND_RESPONSE" = "304" ]; then
    log_pass "前端页面可访问"
elif [ "$FRONTEND_RESPONSE" = "404" ]; then
    log_warning "前端页面404（可能未集成前端）"
else
    log_fail "前端页面访问失败"
fi

# 总结
echo ""
echo "========================================="
echo "验证结果汇总"
echo "========================================="
echo -e "通过: ${GREEN}${PASS_COUNT}${NC}"
echo -e "失败: ${RED}${FAIL_COUNT}${NC}"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}✓ 所有验证通过！部署成功！${NC}"
    echo ""
    echo "访问地址:"
    echo "  - 应用: http://localhost:8080"
    echo "  - API健康检查: http://localhost:8080/api/health"
    echo "  - MySQL: localhost:3306"
    exit 0
else
    echo -e "${RED}✗ 部署验证失败，请检查错误信息${NC}"
    echo ""
    echo "排查建议:"
    echo "  1. 查看容器日志: docker-compose logs -f"
    echo "  2. 检查容器状态: docker-compose ps"
    echo "  3. 验证配置文件: cat .env"
    exit 1
fi
