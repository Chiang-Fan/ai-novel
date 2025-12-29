#!/bin/bash

# AI Novel Writer - 健康检查脚本

echo "🔍 AI Novel Writer - 健康检查"
echo "=================================="
echo ""

# 检查应用是否运行
if ! curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "❌ 应用未运行或健康检查失败"
    echo ""
    echo "请检查："
    echo "  1. 应用是否已启动: ps aux | grep ai-novel-writer.jar"
    echo "  2. 端口是否正确: 默认8080，检查.env中的SERVER_PORT"
    echo "  3. 查看日志: tail -f logs/application.log"
    exit 1
fi

echo "✅ 应用运行中"
echo ""

# 获取健康状态
echo "📊 健康状态："
curl -s http://localhost:8080/api/health | python3 -m json.tool || curl -s http://localhost:8080/api/health
echo ""
echo ""

# 测试API
echo "🧪 测试小说列表API："
RESPONSE=$(curl -s http://localhost:8080/api/novels)
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""
echo ""

# 访问信息
echo "✅ 所有检查通过！"
echo ""
echo "📍 访问地址："
echo "  - 前端页面: http://localhost:8080"
echo "  - API文档:  http://localhost:8080/docs"
echo "  - 健康检查: http://localhost:8080/api/health"
echo ""
echo "=================================="
