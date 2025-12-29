#!/bin/bash

# AI智能小说创作系统 - 一键部署脚本（Java版）

set -e

echo "======================================"
echo "🚀 AI智能小说创作系统 - 部署开始"
echo "======================================"

# 检查环境
echo "📋 检查环境..."

if ! command -v java &> /dev/null; then
    echo "❌ 未安装JDK，请先安装JDK 17+"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ 未安装Maven，请先安装Maven 3.8+"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "⚠️  未安装Docker，将使用本地运行模式"
    DOCKER_AVAILABLE=false
else
    DOCKER_AVAILABLE=true
fi

# 检查配置文件
if [ ! -f ".env" ]; then
    echo "⚠️  未找到.env文件，从模板复制..."
    cp .env.example .env
    echo "📝 请编辑.env文件，配置数据库和AI服务参数"
    echo "   vi .env"
    read -p "按回车键继续..."
fi

# 选择部署方式
echo ""
echo "请选择部署方式："
echo "1) 本地运行（需要本地MySQL）"
echo "2) Docker Compose部署（推荐）"
echo "3) 仅构建JAR包"
read -p "请选择 [1-3]: " choice

case $choice in
    1)
        echo "🔨 开始本地构建..."
        mvn clean package -DskipTests
        
        echo "✅ 构建完成！"
        echo ""
        echo "启动应用:"
        echo "  java -jar target/ai-novel-writer.jar"
        echo ""
        echo "或使用Maven运行:"
        echo "  mvn spring-boot:run"
        ;;
    2)
        if [ "$DOCKER_AVAILABLE" = false ]; then
            echo "❌ Docker未安装，无法使用此选项"
            exit 1
        fi
        
        echo "🐳 使用Docker Compose部署..."
        docker-compose down
        docker-compose build
        docker-compose up -d
        
        echo ""
        echo "⏳ 等待服务启动..."
        sleep 30
        
        echo "✅ 部署完成！"
        echo ""
        echo "访问地址："
        echo "  前端页面: http://localhost:8080"
        echo "  API文档: http://localhost:8080/docs"
        echo ""
        echo "查看日志："
        echo "  docker-compose logs -f app"
        ;;
    3)
        echo "🔨 仅构建JAR包..."
        mvn clean package -DskipTests
        
        echo "✅ 构建完成！"
        echo ""
        echo "JAR文件位置: target/ai-novel-writer.jar"
        echo "文件大小: $(du -h target/ai-novel-writer.jar | cut -f1)"
        ;;
    *)
        echo "❌ 无效选择"
        exit 1
        ;;
esac

echo ""
echo "======================================"
echo "🎉 部署流程完成！"
echo "======================================"
