#!/bin/bash

# AI智能小说创作系统 - 本地数据库快速初始化
# 适用于本地开发环境

echo "========================================="
echo "🗄️  本地数据库初始化"
echo "========================================="

cd "$(dirname "$0")"

# 检查MySQL
if ! command -v mysql &> /dev/null; then
    echo "❌ 未安装MySQL"
    exit 1
fi

# 数据库配置
DB_NAME="ai_novel_writer"
DB_USER="root"
DB_PASSWORD=""

echo "📋 数据库: $DB_NAME"
echo ""

# 创建数据库并执行SQL
echo "🚀 初始化数据库..."
mysql -u "$DB_USER" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 数据库创建成功"
else
    echo "⚠️  数据库可能已存在"
fi

# 执行SQL脚本
if [ -f "src/main/resources/db/schema.sql" ]; then
    echo "📄 执行SQL脚本..."
    mysql -u "$DB_USER" "$DB_NAME" < src/main/resources/db/schema.sql 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "========================================="
        echo "✅ 数据库初始化完成！"
        echo "========================================="
        echo ""
        echo "📊 数据库表："
        mysql -u "$DB_USER" "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null
        echo ""
        echo "🎉 现在可以启动应用："
        echo "   ./start-java.sh"
        echo "========================================="
    else
        echo "❌ SQL脚本执行失败"
        exit 1
    fi
else
    echo "❌ 未找到SQL脚本"
    exit 1
fi
