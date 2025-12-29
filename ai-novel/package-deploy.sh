#!/bin/bash

# AI智能小说创作系统 - 云服务器部署包打包脚本

echo "========================================="
echo "📦 打包云服务器部署文件"
echo "========================================="

cd "$(dirname "$0")"

# 检查JAR文件
if [ ! -f "target/ai-novel-writer.jar" ]; then
    echo "❌ 错误: 未找到JAR文件"
    echo "请先运行: mvn clean package -DskipTests"
    exit 1
fi

# 创建临时目录
TEMP_DIR="ai-novel-writer-deploy"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

echo "📋 复制部署文件..."

# 复制必需文件
cp target/ai-novel-writer.jar "$TEMP_DIR/"
cp start-server.sh "$TEMP_DIR/"
cp stop-server.sh "$TEMP_DIR/"
cp restart-server.sh "$TEMP_DIR/"
cp init-database.sh "$TEMP_DIR/"
cp .env.server "$TEMP_DIR/"
cp src/main/resources/db/schema.sql "$TEMP_DIR/"
cp DEPLOYMENT.md "$TEMP_DIR/"
cp QUICK_DEPLOY.md "$TEMP_DIR/"

# 复制README
if [ -f "README_JAVA.md" ]; then
    cp README_JAVA.md "$TEMP_DIR/README.md"
fi

# 设置权限
chmod +x "$TEMP_DIR"/*.sh

# 创建目录结构说明
cat > "$TEMP_DIR/README_FIRST.txt" << 'EOF'
AI智能小说创作系统 - 云服务器部署包
=====================================

📦 部署包内容：

├── ai-novel-writer.jar          # 应用程序（83MB）
├── start-server.sh              # 启动脚本
├── stop-server.sh               # 停止脚本
├── restart-server.sh            # 重启脚本
├── init-database.sh             # 数据库初始化脚本
├── schema.sql                   # 数据库结构SQL
├── .env.server                  # 环境配置模板
├── DEPLOYMENT.md                # 详细部署文档
├── QUICK_DEPLOY.md              # 快速部署指南
└── README.md                    # 项目说明文档

🚀 快速部署步骤：

1. 上传到服务器：
   scp -r ai-novel-writer-deploy your_user@your_server:/opt/
   
2. 登录服务器并进入目录：
   ssh your_user@your_server
   cd /opt/ai-novel-writer-deploy

3. 配置环境变量：
   cp .env.server .env
   vi .env
   # 修改 QWEN_API_KEY 和数据库配置

4. 初始化数据库（首次部署）：
   ./init-database.sh

5. 启动服务：
   ./start-server.sh

📖 详细说明请查看：QUICK_DEPLOY.md 或 DEPLOYMENT.md

⚠️  系统要求：
- JDK 17+
- MySQL 5.7+
- 2GB+ 内存
- Linux 系统

EOF

# 显示文件大小
echo ""
echo "📊 文件列表："
ls -lh "$TEMP_DIR"

# 打包为tar.gz
PACKAGE_NAME="ai-novel-writer-deploy-$(date +%Y%m%d-%H%M%S).tar.gz"
echo ""
echo "🗜️  压缩打包..."
tar -czf "$PACKAGE_NAME" "$TEMP_DIR"

# 清理临时目录
rm -rf "$TEMP_DIR"

# 显示结果
FILE_SIZE=$(du -h "$PACKAGE_NAME" | cut -f1)
echo ""
echo "========================================="
echo "✅ 打包完成！"
echo "========================================="
echo "📦 文件: $PACKAGE_NAME"
echo "📏 大小: $FILE_SIZE"
echo "========================================="
echo ""
echo "📤 上传到服务器："
echo "   scp $PACKAGE_NAME your_user@your_server:/opt/"
echo ""
echo "📂 在服务器上解压："
echo "   cd /opt"
echo "   tar -xzf $PACKAGE_NAME"
echo "   cd ai-novel-writer-deploy"
echo ""
echo "⚙️  配置并启动："
echo "   cp .env.server .env"
echo "   vi .env                    # 修改配置"
echo "   ./start-server.sh          # 启动服务"
echo ""
echo "📖 详细文档："
echo "   cat DEPLOYMENT.md"
echo "========================================="
