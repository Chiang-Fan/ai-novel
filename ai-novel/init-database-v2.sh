#!/bin/bash

# AI智能小说创作系统 - 数据库初始化脚本（简化版）
# 用于在云服务器上初始化MySQL数据库

echo "========================================="
echo "🗄️  AI智能小说创作系统 - 数据库初始化"
echo "========================================="

# 数据库配置
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/application-prod.yml"
SCHEMA_FILE="${SCRIPT_DIR}/schema.sql"

# 检查配置文件
if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ 未找到配置文件: $CONFIG_FILE"
    exit 1
fi

# 检查SQL脚本文件
if [ ! -f "$SCHEMA_FILE" ]; then
    echo "❌ 未找到SQL脚本文件: $SCHEMA_FILE"
    exit 1
fi

# 从配置文件读取数据库配置
DB_HOST=$(grep -A10 "datasource:" "$CONFIG_FILE" | grep "url:" | sed 's/.*:\/\/\([^:]*\).*/\1/')
DB_PORT=$(grep -A10 "datasource:" "$CONFIG_FILE" | grep "url:" | sed 's/.*:\([0-9]*\)\/.*/\1/')
DB_NAME=$(grep -A10 "datasource:" "$CONFIG_FILE" | grep "url:" | sed 's/.*\/\([^?]*\).*/\1/')
DB_USER=$(grep -A10 "datasource:" "$CONFIG_FILE" | grep "username:" | grep -v "#" | awk '{print $2}')
DB_PASSWORD=$(grep -A10 "datasource:" "$CONFIG_FILE" | grep "password:" | grep -v "#" | awk '{print $2}')

echo "📋 数据库配置："
echo "   主机: $DB_HOST:$DB_PORT"
echo "   数据库: $DB_NAME"
echo "   用户: $DB_USER"
echo ""

# 检查MySQL连接
echo "🔍 检查MySQL连接..."
if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" == "your_mysql_password_here" ]; then
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "SELECT 1;" > /dev/null 2>&1
    MYSQL_CMD="mysql -h $DB_HOST -P $DB_PORT -u $DB_USER"
else
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
    MYSQL_CMD="mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD"
fi

if [ $? -ne 0 ]; then
    echo "❌ 无法连接到MySQL服务器"
    echo "请检查："
    echo "  1. MySQL服务是否运行: systemctl status mysql"
    echo "  2. application-prod.yml中的数据库配置是否正确"
    echo "  3. 用户权限是否足够"
    exit 1
fi

echo "✅ MySQL连接成功"
echo ""

# 确认操作
read -p "⚠️  这将初始化数据库 '$DB_NAME'，是否继续? (y/n): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "操作已取消"
    exit 0
fi

# 执行SQL脚本
echo ""
echo "🚀 开始初始化数据库..."
echo ""

$MYSQL_CMD < "$SCHEMA_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ 数据库初始化成功！"
    echo "========================================="
    echo ""
    
    # 显示表列表
    echo "📊 数据库表列表："
    $MYSQL_CMD "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null
    
    echo ""
    echo "🎉 可以启动应用了："
    echo "   ./start-server.sh"
    echo "========================================="
else
    echo ""
    echo "❌ 数据库初始化失败"
    echo ""
    echo "请检查错误信息并手动执行："
    echo "   $MYSQL_CMD < $SCHEMA_FILE"
    exit 1
fi
