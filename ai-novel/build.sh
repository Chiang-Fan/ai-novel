#!/bin/bash

# AI小说创作系统 - 编译脚本
# 使用指定的JDK 17进行编译

echo "=========================================="
echo "  AI小说创作系统 - Maven编译脚本"
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

echo "✅ 使用JDK: $JAVA_HOME"
echo ""

# 进入项目目录
cd "$(dirname "$0")"

# 执行Maven编译
echo "🔨 开始编译..."
echo ""

mvn clean package -DskipTests

# 检查编译结果
if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "  ✅ 编译成功！"
    echo "=========================================="
    echo ""
    echo "📦 JAR文件: target/ai-novel-writer.jar"
    echo ""
    echo "🚀 启动命令:"
    echo "   ./run.sh"
    echo ""
else
    echo ""
    echo "=========================================="
    echo "  ❌ 编译失败！"
    echo "=========================================="
    exit 1
fi
