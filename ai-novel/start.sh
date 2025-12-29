#!/bin/bash

# AI Novel Writer - 快速启动脚本
set -e

echo "🚀 AI Novel Writer - 启动应用"
echo "=================================="

# 加载环境变量
if [ -f ".env" ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✓ 环境变量已加载"
else
    echo "⚠ 警告: 未找到.env文件，将使用默认配置"
fi

# 检查JAR文件是否存在
if [ ! -f "target/ai-novel-writer.jar" ]; then
    echo ""
    echo "❌ 未找到可执行文件，请先运行构建："
    echo "   mvn clean package -DskipTests"
    exit 1
fi

echo ""
echo "📍 访问地址："
echo "   - 前端页面: http://localhost:${SERVER_PORT:-8080}"
echo "   - API文档:  http://localhost:${SERVER_PORT:-8080}/docs"
echo "   - 健康检查: http://localhost:${SERVER_PORT:-8080}/api/health"
echo ""
echo "按 Ctrl+C 停止应用"
echo "=================================="
echo ""

# 启动应用
java -jar target/ai-novel-writer.jar
