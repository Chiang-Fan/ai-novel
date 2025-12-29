#!/bin/bash

# AI小说写作助手 - 重启脚本

echo "🔄 AI小说写作助手 - 重启中..."
echo "================================"

# 进入项目目录
cd "$(dirname "$0")"

# 检查配置文件
if [ ! -f "src/main/resources/application-local.yml" ]; then
    echo ""
    echo "⚠️  警告: application-local.yml不存在"
    echo ""
    echo "这是您第一次启动，请按照以下步骤配置："
    echo "1. 复制配置模板:"
    echo "   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml"
    echo ""
    echo "2. 编辑配置文件:"
    echo "   vim src/main/resources/application-local.yml"
    echo ""
    echo "3. 替换api-key为真实密钥（从 https://dashscope.console.aliyun.com/ 获取）"
    echo ""
    echo "4. 重新运行此脚本"
    echo ""
    read -p "是否现在自动创建配置文件? (y/n): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
        echo "✅ 已创建配置文件，请编辑后重新运行此脚本"
    fi
    exit 1
fi

# 设置Java环境
if [ -d "/Users/jiangfan/workspace/tencent/tsf/jdk/jdk-17.0.17.jdk/Contents/Home" ]; then
    export JAVA_HOME=/Users/jiangfan/workspace/tencent/tsf/jdk/jdk-17.0.17.jdk/Contents/Home
    export PATH=$JAVA_HOME/bin:$PATH
fi

# 检查Java
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java，请先安装JDK 17+"
    exit 1
fi

echo "☕ Java版本: $(java -version 2>&1 | head -1)"

# 停止旧进程
echo "🛑 停止旧进程..."
OLD_PID=$(ps aux | grep "ai-novel-writer.jar" | grep -v grep | awk '{print $2}')
if [ -n "$OLD_PID" ]; then
    kill $OLD_PID
    echo "   已停止进程: $OLD_PID"
    sleep 2
else
    echo "   未发现运行中的进程"
fi

# 检查JAR文件
if [ ! -f "target/ai-novel-writer.jar" ]; then
    echo "❌ 错误: JAR文件不存在，请先运行: mvn clean package"
    exit 1
fi

# 创建必要的目录
mkdir -p logs data

# 启动应用（使用local配置）
echo "🚀 启动应用（使用application-local.yml配置）..."
nohup java -jar target/ai-novel-writer.jar --spring.profiles.active=local > logs/app.log 2>&1 &
NEW_PID=$!
echo "   进程ID: $NEW_PID"

# 等待启动
echo "⏳ 等待应用启动..."
for i in {1..20}; do
    sleep 1
    if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
        echo ""
        echo "✅ 应用启动成功！"
        echo "================================"
        echo "📖 访问地址: http://localhost:8080"
        echo "📊 健康检查: http://localhost:8080/actuator/health"
        echo "📚 API文档:  http://localhost:8080/docs"
        echo "🗄️  H2控制台: http://localhost:8080/h2-console"
        echo "================================"
        echo "📝 查看日志: tail -f logs/app.log"
        echo ""
        exit 0
    fi
    echo -n "."
done

echo ""
echo "❌ 应用启动失败或超时！"
echo "请查看日志: tail -100 logs/app.log"
exit 1
