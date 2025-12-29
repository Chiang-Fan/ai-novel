#!/bin/bash

# AI智能小说创作系统 - 云服务器启动脚本
# 适用于生产环境部署

echo "========================================="
echo "🚀 启动 AI智能小说创作系统"
echo "========================================="

# 切换到脚本所在目录
cd "$(dirname "$0")"
WORK_DIR=$(pwd)

# 配置文件路径
ENV_FILE="${WORK_DIR}/.env"
JAR_FILE="${WORK_DIR}/ai-novel-writer.jar"
PID_FILE="${WORK_DIR}/app.pid"
LOG_FILE="${WORK_DIR}/logs/app.log"

# 创建日志目录
mkdir -p logs

# 检查JAR文件
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 错误: 未找到JAR文件 ($JAR_FILE)"
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

# 加载环境变量
if [ -f "$ENV_FILE" ]; then
    echo "📝 加载环境变量..."
    set -a
    source "$ENV_FILE"
    set +a
else
    echo "⚠️  未找到 .env 文件，使用默认配置"
    echo "建议创建 .env 文件配置数据库和AI服务参数"
fi

# 设置必要的环境变量（如果未在.env中设置）
export SPRING_AI_OPENAI_API_KEY=${SPRING_AI_OPENAI_API_KEY:-${QWEN_API_KEY}}
export SPRING_AI_OPENAI_BASE_URL=${SPRING_AI_OPENAI_BASE_URL:-${QWEN_API_BASE}}
export DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY:-${QWEN_API_KEY}}

export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-3306}
export DB_NAME=${DB_NAME:-ai_novel_writer}
export DB_USER=${DB_USER:-root}
export DB_PASSWORD=${DB_PASSWORD:-}

export SERVER_PORT=${SERVER_PORT:-8080}
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

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

# 检查数据库连接（可选）
if command -v mysql &> /dev/null; then
    if [ -n "$DB_PASSWORD" ]; then
        mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME; SELECT 1;" > /dev/null 2>&1
    else
        mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "USE $DB_NAME; SELECT 1;" > /dev/null 2>&1
    fi
    
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

# JVM参数配置
JVM_OPTS="${JVM_OPTS:--Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200}"

# 启动应用（使用sudo -E保留环境变量，或显式传递环境变量）
echo "🚀 启动应用..."
echo "   JAR: $JAR_FILE"
echo "   日志: $LOG_FILE"
echo "   端口: $SERVER_PORT"
echo "   环境: $SPRING_PROFILES_ACTIVE"
echo "   数据库: $DB_HOST:$DB_PORT/$DB_NAME"
echo ""

# 方式一：使用 sudo -E（保留所有环境变量）
# sudo -E nohup java $JVM_OPTS \
#     -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
#     -Dserver.port=$SERVER_PORT \
#     -jar "$JAR_FILE" \
#     > "$LOG_FILE" 2>&1 &

# 方式二：显式传递环境变量（推荐，更安全）
sudo nohup java $JVM_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -Dserver.port=$SERVER_PORT \
    -DDB_HOST=$DB_HOST \
    -DDB_PORT=$DB_PORT \
    -DDB_NAME=$DB_NAME \
    -DDB_USER=$DB_USER \
    -DDB_PASSWORD="$DB_PASSWORD" \
    -DSPRING_AI_OPENAI_API_KEY="$SPRING_AI_OPENAI_API_KEY" \
    -DSPRING_AI_OPENAI_BASE_URL="$SPRING_AI_OPENAI_BASE_URL" \
    -DDASHSCOPE_API_KEY="$DASHSCOPE_API_KEY" \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &

APP_PID=$!
echo $APP_PID > "$PID_FILE"

echo "✅ 应用已启动 (PID: $APP_PID)"

# 等待启动
echo "⏳ 等待应用启动..."
for i in {1..60}; do
    if curl -s http://localhost:$SERVER_PORT/actuator/health > /dev/null 2>&1; then
        HEALTH=$(curl -s http://localhost:$SERVER_PORT/actuator/health)
        echo ""
        echo "========================================="
        echo "✅ 启动成功！"
        echo "========================================="
        echo "📚 前端页面: http://localhost:$SERVER_PORT"
        echo "📖 API文档: http://localhost:$SERVER_PORT/docs"
        echo "🔍 健康检查: http://localhost:$SERVER_PORT/actuator/health"
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
echo "2. 端口 $SERVER_PORT 是否被占用"
echo "3. 环境变量是否正确配置"
echo "4. 查看完整日志: tail -f $LOG_FILE"
exit 1
