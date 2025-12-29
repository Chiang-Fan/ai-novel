#!/bin/bash

# AI智能小说创作系统 - 数据库初始化脚本
# 用于在云服务器上初始化MySQL数据库

echo "========================================="
echo "🗄️  AI智能小说创作系统 - 数据库初始化"
echo "========================================="

# 数据库配置（从.env文件读取或使用默认值）
if [ -f ".env" ]; then
    source .env
fi

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-ai_novel_writer}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD:-}

# SQL脚本路径
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SCHEMA_FILE="${SCRIPT_DIR}/src/main/resources/db/schema.sql"

# 如果是打包后的环境，SQL文件可能在其他位置
if [ ! -f "$SCHEMA_FILE" ]; then
    SCHEMA_FILE="${SCRIPT_DIR}/schema.sql"
fi

echo "📋 数据库配置："
echo "   主机: $DB_HOST:$DB_PORT"
echo "   数据库: $DB_NAME"
echo "   用户: $DB_USER"
echo ""

# 检查MySQL连接
echo "🔍 检查MySQL连接..."
if [ -z "$DB_PASSWORD" ]; then
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "SELECT 1;" > /dev/null 2>&1
else
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
fi

if [ $? -ne 0 ]; then
    echo "❌ 无法连接到MySQL服务器"
    echo "请检查："
    echo "  1. MySQL服务是否运行: systemctl status mysql"
    echo "  2. 连接参数是否正确"
    echo "  3. 用户权限是否足够"
    exit 1
fi

echo "✅ MySQL连接成功"
echo ""

# 检查SQL脚本文件
if [ ! -f "$SCHEMA_FILE" ]; then
    echo "❌ 未找到SQL脚本文件: $SCHEMA_FILE"
    echo ""
    echo "解决方案："
    echo "1. 如果是开发环境，请在项目根目录运行此脚本"
    echo "2. 如果是生产环境，请手动执行SQL脚本："
    echo "   mysql -h $DB_HOST -u $DB_USER -p < schema.sql"
    exit 1
fi

echo "📄 SQL脚本: $SCHEMA_FILE"
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

if [ -z "$DB_PASSWORD" ]; then
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" < "$SCHEMA_FILE"
else
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" < "$SCHEMA_FILE"
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ 数据库初始化成功！"
    echo "========================================="
    echo ""
    
    # 显示表列表
    echo "📊 数据库表列表："
    if [ -z "$DB_PASSWORD" ]; then
        mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null
    else
        mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null
    fi
    
    echo ""
    echo "🎉 可以启动应用了："
    echo "   ./start-server.sh"
    echo "========================================="
else
    echo ""
    echo "❌ 数据库初始化失败"
    echo ""
    echo "请检查错误信息并手动执行："
    if [ -z "$DB_PASSWORD" ]; then
        echo "   mysql -h $DB_HOST -P $DB_PORT -u $DB_USER < $SCHEMA_FILE"
    else
        echo "   mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p < $SCHEMA_FILE"
    fi
    exit 1
fi
