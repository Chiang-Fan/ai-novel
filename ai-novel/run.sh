#!/bin/bash

# AI小说创作系统 - 启动脚本
# 使用指定的JDK 17运行

echo "=========================================="
echo "  AI小说创作系统 - 启动"
echo "=========================================="
echo ""

# 设置JDK 17路径
export JAVA_HOME=/Users/jiangfan/workspace/tencent/tsf/jdk/jdk-17.0.17.jdk/Contents/Home

# 检查JDK是否存在
if [ ! -d "$JAVA_HOME" ]; then
    echo "❌ 错误: JDK 17 未找到！"
    echo "   路径: $JAVA_HOME"
    exit 1
fi

# 进入项目目录
cd "$(dirname "$0")"

# 检查JAR文件是否存在
if [ ! -f "target/ai-novel-writer.jar" ]; then
    echo "❌ 错误: JAR文件不存在！"
    echo "   请先运行 ./build.sh 编译项目"
    exit 1
fi

echo "✅ 使用JDK: $JAVA_HOME"
echo ""
echo "🚀 启动应用..."
echo ""

# 启动应用
"$JAVA_HOME/bin/java" -jar target/ai-novel-writer.jar

echo ""
echo "=========================================="
echo "  应用已停止"
echo "=========================================="
