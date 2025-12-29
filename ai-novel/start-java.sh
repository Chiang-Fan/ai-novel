#!/bin/bash

# AI智能小说创作系统 - Java版启动脚本

echo "========================================="
echo "🚀 启动 AI智能小说创作系统 (Java版)"
echo "========================================="

# 检查MySQL是否运行
if ! pgrep -x "mysqld" > /dev/null; then
    echo "📦 启动MySQL服务..."
    brew services start mysql
    sleep 3
fi

# 检查并创建数据库
echo "📦 检查数据库..."
mysql -u root -e "CREATE DATABASE IF NOT EXISTS ai_novel_writer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null

# 停止旧进程
echo "🔄 停止旧进程..."
pkill -f ai-novel-writer.jar 2>/dev/null
sleep 2

# 读取.env文件
if [ -f "../.env" ]; then
    echo "📝 加载环境变量..."
    export $(cat ../.env | grep -v '^#' | xargs)
fi

# 设置环境变量
export SPRING_AI_OPENAI_API_KEY=${QWEN_API_KEY}
export SPRING_AI_OPENAI_BASE_URL=${QWEN_API_BASE}
export DASHSCOPE_API_KEY=${QWEN_API_KEY}
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ai_novel_writer
export DB_USER=root
export DB_PASSWORD=

# 使用 JDK 17 (TencentKonaJDK)
export JAVA_HOME=/Users/jiangfan/workspace/tencent/tsf/jdk/jdk-17.0.17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
JAVA_CMD=$JAVA_HOME/bin/java

echo "☕ 使用 JDK: $($JAVA_CMD -version 2>&1 | head -1)"

if [ ! -f "$JAVA_CMD" ]; then
    echo "❌ 错误: 未找到Java ($JAVA_CMD)"
    exit 1
fi

# 启动应用
echo "🚀 启动应用..."
cd "$(dirname "$0")"
$JAVA_CMD -jar target/ai-novel-writer.jar --spring.profiles.active=dev > app.log 2>&1 &

# 等待启动
echo "⏳ 等待应用启动..."
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo ""
        echo "========================================="
        echo "✅ 启动成功！"
        echo "========================================="
        echo "📚 前端页面: http://localhost:8080"
        echo "📖 API文档: http://localhost:8080/docs"
        echo "🔍 健康检查: http://localhost:8080/actuator/health"
        echo "📋 日志文件: $(pwd)/app.log"
        echo "========================================="
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo ""
echo "❌ 启动失败，请查看日志："
tail -50 app.log
exit 1
