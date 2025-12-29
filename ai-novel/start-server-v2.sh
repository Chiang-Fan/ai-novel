#!/bin/bash

# AI智能小说创作系统 - 云服务器启动脚本（使用外部配置文件）

echo "========================================="
echo "🚀 启动 AI智能小说创作系统"
echo "========================================="

# 切换到脚本所在目录
cd "$(dirname "$0")"
WORK_DIR=$(pwd)

# 文件路径
JAR_FILE="${WORK_DIR}/ai-novel-writer.jar"
CONFIG_FILE="${WORK_DIR}/application-prod.yml"
PID_FILE="${WORK_DIR}/app.pid"
LOG_FILE="${WORK_DIR}/logs/app.log"

# 创建日志目录
mkdir -p logs

# 检查JAR文件
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 错误: 未找到JAR文件 ($JAR_FILE)"
    exit 1
fi

# 检查配置文件
if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ 错误: 未找到配置文件 ($CONFIG_FILE)"
    echo "请先创建并配置 application-prod.yml"
    exit 1
fi

# 检查是否已经在运行
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "⚠️  应用已在运行 (PID: $OLD_PID)"
        read -p "是否重启? (y/n): " restart
        if [ "$restart" != "y" ]; then
            echo "取消启动"
            exit 0
        fi
        echo "🔄 停止旧进程..."
        kill "$OLD_PID"
        sleep 3
    fi
    rm -f "$PID_FILE"
fi

# 检查Java
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未安装Java"
    echo "请安装 JDK 17 或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ 错误: Java版本过低 (当前: $JAVA_VERSION, 需要: 17+)"
    exit 1
fi

echo "☕ Java版本: $(java -version 2>&1 | head -1)"

# 从配置文件读取数据库配置（用于检查）
DB_HOST=$(grep -A5 "datasource:" "$CONFIG_FILE" | grep "url:" | sed 's/.*:\/\/\([^:]*\).*/\1/')
DB_PORT=$(grep -A5 "datasource:" "$CONFIG_FILE" | grep "url:" | sed 's/.*:\([0-9]*\)\/.*/\1/')
DB_NAME=$(grep -A5 "datasource:" "$CONFIG_FILE" | grep "url:" | sed 's/.*\/\([^?]*\).*/\1/')
SERVER_PORT=$(grep "port:" "$CONFIG_FILE" | grep -v "#" | awk '{print $2}')

# 检查数据库连接（可选）
if command -v mysql &> /dev/null; then
    DB_USER=$(grep "username:" "$CONFIG_FILE" | grep -v "#" | awk '{print $2}')
    DB_PASSWORD=$(grep "password:" "$CONFIG_FILE" | grep -v "#" | awk '{print $2}')
    
    if [ -n "$DB_PASSWORD" ] && [ "$DB_PASSWORD" != "your_mysql_password_here" ]; then
        mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME; SELECT 1;" > /dev/null 2>&1
        
        if [ $? -ne 0 ]; then
            echo "⚠️  警告: 无法连接到数据库 $DB_NAME"
            echo "   如果数据库未初始化，请运行: ./init-database.sh"
            read -p "   是否继续启动? (y/n): " continue_start
            if [ "$continue_start" != "y" ]; then
                echo "启动已取消"
                exit 1
            fi
        fi
    fi
fi

# JVM参数配置
JVM_OPTS="${JVM_OPTS:--Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200}"

# 启动应用
echo "🚀 启动应用..."
echo "   JAR: $JAR_FILE"
echo "   配置: $CONFIG_FILE"
echo "   日志: $LOG_FILE"
echo "   端口: ${SERVER_PORT:-8080}"
echo "   数据库: ${DB_HOST:-localhost}:${DB_PORT:-3306}/${DB_NAME:-ai_novel_writer}"
echo ""

nohup java $JVM_OPTS \
    -Dspring.config.location="file:$CONFIG_FILE" \
    -Dspring.profiles.active=prod \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &

APP_PID=$!
echo $APP_PID > "$PID_FILE"

echo "✅ 应用已启动 (PID: $APP_PID)"

# 等待启动
echo "⏳ 等待应用启动..."
for i in {1..60}; do
    if curl -s http://localhost:${SERVER_PORT:-8080}/actuator/health > /dev/null 2>&1; then
        HEALTH=$(curl -s http://localhost:${SERVER_PORT:-8080}/actuator/health)
        echo ""
        echo "========================================="
        echo "✅ 启动成功！"
        echo "========================================="
        echo "📚 前端页面: http://localhost:${SERVER_PORT:-8080}"
        echo "📖 API文档: http://localhost:${SERVER_PORT:-8080}/docs"
        echo "🔍 健康检查: http://localhost:${SERVER_PORT:-8080}/actuator/health"
        echo "📋 日志文件: $LOG_FILE"
        echo "🔢 进程ID: $APP_PID"
        echo "========================================="
        echo ""
        echo "查看实时日志："
        echo "  tail -f $LOG_FILE"
        echo ""
        echo "停止应用："
        echo "  ./stop-server.sh"
        echo "========================================="
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo ""
echo "⚠️  应用启动超时（60秒）"
echo "📋 最近日志："
tail -30 "$LOG_FILE"
echo ""
echo "请检查："
echo "1. 数据库连接是否正常"
echo "2. 端口 ${SERVER_PORT:-8080} 是否被占用"
echo "3. 配置文件是否正确: $CONFIG_FILE"
echo "4. 查看完整日志: tail -f $LOG_FILE"
exit 1
