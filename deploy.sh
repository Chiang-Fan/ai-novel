#!/bin/bash
# AI小说创作系统 - 一键部署脚本
# 适用于Linux/MacOS环境

set -e

echo "======================================"
echo "AI智能小说创作系统 - Spring Boot版本"
echo "一键部署脚本"
echo "======================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查JDK
echo -e "${YELLOW}[1/7] 检查Java环境...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: 未找到Java,请安装JDK 17或更高版本${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}错误: Java版本过低,需要JDK 17+,当前版本: $JAVA_VERSION${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java环境检查通过${NC}"

# 检查Maven
echo -e "${YELLOW}[2/7] 检查Maven环境...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: 未找到Maven,请安装Maven 3.8+${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven环境检查通过${NC}"

# 检查MySQL
echo -e "${YELLOW}[3/7] 检查MySQL连接...${NC}"
read -p "MySQL主机地址 [localhost]: " DB_HOST
DB_HOST=${DB_HOST:-localhost}

read -p "MySQL端口 [3306]: " DB_PORT
DB_PORT=${DB_PORT:-3306}

read -p "MySQL用户名 [root]: " DB_USER
DB_USER=${DB_USER:-root}

read -sp "MySQL密码: " DB_PASSWORD
echo ""

read -p "数据库名称 [ai_novel_writer]: " DB_NAME
DB_NAME=${DB_NAME:-ai_novel_writer}

# 测试MySQL连接
if command -v mysql &> /dev/null; then
    if mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" &> /dev/null; then
        echo -e "${GREEN}✓ MySQL连接成功${NC}"
        
        # 创建数据库(如果不存在)
        echo "创建数据库..."
        mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || true
    else
        echo -e "${RED}警告: MySQL连接失败,请确保数据库可访问${NC}"
    fi
else
    echo -e "${YELLOW}警告: 未安装mysql客户端,跳过连接测试${NC}"
fi

# 配置AI API
echo -e "${YELLOW}[4/7] 配置AI API...${NC}"
read -p "AI API密钥: " AI_API_KEY

read -p "AI API基础URL [https://dashscope.aliyuncs.com/compatible-mode/v1]: " AI_API_BASE
AI_API_BASE=${AI_API_BASE:-https://dashscope.aliyuncs.com/compatible-mode/v1}

read -p "AI模型 [qwen-plus]: " AI_MODEL
AI_MODEL=${AI_MODEL:-qwen-plus}

# 创建配置文件
echo -e "${YELLOW}[5/7] 生成配置文件...${NC}"
cat > src/main/resources/application-prod.yml <<EOF
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
  
  jpa:
    show-sql: false

spring.ai:
  openai:
    base-url: ${AI_API_BASE}
    api-key: ${AI_API_KEY}
    chat:
      options:
        model: ${AI_MODEL}

logging:
  level:
    root: INFO
    com.ai.novel: INFO
EOF

echo -e "${GREEN}✓ 配置文件生成完成${NC}"

# 编译打包
echo -e "${YELLOW}[6/7] 编译打包应用...${NC}"
echo "执行: mvn clean package -DskipTests"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}错误: 编译失败${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 编译打包完成${NC}"

# 部署
echo -e "${YELLOW}[7/7] 部署应用...${NC}"
read -p "应用端口 [8080]: " APP_PORT
APP_PORT=${APP_PORT:-8080}

# 创建启动脚本
cat > start.sh <<EOF
#!/bin/bash
# AI小说创作系统启动脚本

APP_NAME="ai-novel-writer"
JAR_FILE="target/ai-novel-writer.jar"
PID_FILE="\${APP_NAME}.pid"
LOG_FILE="logs/\${APP_NAME}.log"

# 创建日志目录
mkdir -p logs

# 检查是否已运行
if [ -f "\$PID_FILE" ]; then
    PID=\$(cat "\$PID_FILE")
    if ps -p \$PID > /dev/null 2>&1; then
        echo "应用已在运行 (PID: \$PID)"
        exit 1
    fi
fi

# 启动应用
echo "启动AI小说创作系统..."
nohup java -jar \\
    -Xms512m -Xmx2g \\
    -XX:+UseG1GC \\
    -Dspring.profiles.active=prod \\
    -Dserver.port=${APP_PORT} \\
    "\$JAR_FILE" \\
    > "\$LOG_FILE" 2>&1 &

# 保存PID
echo \$! > "\$PID_FILE"
echo "应用已启动 (PID: \$!)"
echo "日志文件: \$LOG_FILE"
echo "访问地址: http://localhost:${APP_PORT}"
echo ""
echo "使用 ./stop.sh 停止应用"
EOF

# 创建停止脚本
cat > stop.sh <<EOF
#!/bin/bash
# AI小说创作系统停止脚本

APP_NAME="ai-novel-writer"
PID_FILE="\${APP_NAME}.pid"

if [ ! -f "\$PID_FILE" ]; then
    echo "应用未运行"
    exit 1
fi

PID=\$(cat "\$PID_FILE")

if ps -p \$PID > /dev/null 2>&1; then
    echo "停止应用 (PID: \$PID)..."
    kill \$PID
    
    # 等待进程结束
    for i in {1..30}; do
        if ! ps -p \$PID > /dev/null 2>&1; then
            break
        fi
        sleep 1
    done
    
    # 强制kill
    if ps -p \$PID > /dev/null 2>&1; then
        echo "强制停止应用..."
        kill -9 \$PID
    fi
    
    rm -f "\$PID_FILE"
    echo "应用已停止"
else
    echo "应用未运行"
    rm -f "\$PID_FILE"
fi
EOF

# 赋予执行权限
chmod +x start.sh stop.sh

echo -e "${GREEN}✓ 部署完成${NC}"
echo ""
echo "======================================"
echo "部署成功!"
echo "======================================"
echo ""
echo "启动应用: ./start.sh"
echo "停止应用: ./stop.sh"
echo "访问地址: http://localhost:${APP_PORT}"
echo "健康检查: curl http://localhost:${APP_PORT}/actuator/health"
echo ""
echo "日志文件: logs/ai-novel-writer.log"
echo "查看日志: tail -f logs/ai-novel-writer.log"
echo ""

# 询问是否立即启动
read -p "是否立即启动应用? [y/N]: " START_NOW
if [[ "$START_NOW" == "y" || "$START_NOW" == "Y" ]]; then
    ./start.sh
fi
