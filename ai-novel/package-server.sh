#!/bin/bash

# AI智能小说创作系统 - 云服务器部署包打包脚本 V2
# 使用外部 application-prod.yml 配置文件

echo "========================================="
echo "📦 打包云服务器部署文件 V2"
echo "========================================="

cd "$(dirname "$0")"

# 检查JAR文件
if [ ! -f "target/ai-novel-writer.jar" ]; then
    echo "❌ 错误: 未找到JAR文件"
    echo "请先运行: mvn clean package -DskipTests"
    exit 1
fi

# 创建临时目录
TEMP_DIR="ai-novel-writer-server"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

echo "📋 复制部署文件..."

# 复制必需文件
cp target/ai-novel-writer.jar "$TEMP_DIR/"
cp start-server-v2.sh "$TEMP_DIR/start-server.sh"
cp stop-server.sh "$TEMP_DIR/"
cp restart-server.sh "$TEMP_DIR/"
cp init-database-v2.sh "$TEMP_DIR/init-database.sh"
cp application-prod.yml "$TEMP_DIR/"
cp src/main/resources/db/schema.sql "$TEMP_DIR/"
cp fix-continuation-suggestions.sql "$TEMP_DIR/"
cp fix-table.sh "$TEMP_DIR/"

# 复制文档
cp DEPLOYMENT.md "$TEMP_DIR/" 2>/dev/null || true
if [ -f "README_JAVA.md" ]; then
    cp README_JAVA.md "$TEMP_DIR/README.md"
fi

# 设置权限
chmod +x "$TEMP_DIR"/*.sh

# 创建快速入门文档
cat > "$TEMP_DIR/快速开始.md" << 'EOF'
# AI智能小说创作系统 - 快速开始

## 📦 部署包内容

```
ai-novel-writer-server/
├── ai-novel-writer.jar          # 应用程序 (84MB)
├── application-prod.yml         # 生产环境配置文件 ⭐
├── schema.sql                   # 数据库初始化SQL
├── start-server.sh              # 启动脚本
├── stop-server.sh               # 停止脚本
├── restart-server.sh            # 重启脚本
├── init-database.sh             # 数据库初始化脚本
└── 快速开始.md                  # 本文档
```

## 🚀 部署步骤

### 1. 上传到服务器

```bash
scp -r ai-novel-writer-server your_user@your_server:/opt/
```

### 2. 配置数据库和API

**编辑 `application-prod.yml`**

```bash
cd /opt/ai-novel-writer-server
vi application-prod.yml
```

**必须修改的配置：**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_novel_writer?...
    username: root
    password: your_mysql_password_here        # ⭐ 修改这里

  ai:
    openai:
      api-key: sk-your-qwen-api-key-here      # ⭐ 修改这里

ai:
  qianwen:
    api-key: sk-your-qwen-api-key-here        # ⭐ 修改这里
```

**可选配置：**

```yaml
server:
  port: 8080                                  # 服务端口

spring:
  jpa:
    hibernate:
      ddl-auto: update                        # update/validate/none
```

### 3. 初始化数据库

```bash
./init-database.sh
```

这会：
- 自动从 `application-prod.yml` 读取数据库配置
- 创建数据库和15个数据表
- 显示创建的表列表

### 4. 启动服务

```bash
./start-server.sh
```

启动成功后会显示：

```
✅ 启动成功！
📚 前端页面: http://localhost:8080
📖 API文档: http://localhost:8080/docs
🔍 健康检查: http://localhost:8080/actuator/health
```

## 🔧 常用命令

```bash
# 启动服务
./start-server.sh

# 停止服务
./stop-server.sh

# 重启服务
./restart-server.sh

# 查看实时日志
tail -f logs/app.log

# 检查健康状态
curl http://localhost:8080/actuator/health

# 查看进程
cat app.pid
ps aux | grep $(cat app.pid)
```

## 📝 配置说明

### Spring Boot 外部配置加载顺序

Spring Boot 会按以下顺序加载配置（后面的会覆盖前面的）：

1. JAR包内的 `application.yml`
2. JAR包内的 `application-{profile}.yml`
3. **外部的 `application-prod.yml`** ⭐ (推荐)
4. 命令行参数 `-Dkey=value`
5. 系统环境变量

### 为什么使用外部配置文件？

✅ **优点：**
- 配置集中管理，一个文件包含所有配置
- 修改配置无需重新打包
- 支持 YAML 语法，层次清晰
- 更符合 Spring Boot 最佳实践
- 便于版本控制和团队协作

❌ **不使用 .env 的原因：**
- 需要额外的脚本处理
- Spring Boot 不原生支持
- 环境变量在 sudo 时容易丢失
- 配置分散，不便管理

## ⚠️ 注意事项

1. **配置文件安全**
   ```bash
   chmod 600 application-prod.yml
   ```

2. **数据库字符集**
   - 必须使用 `utf8mb4` 字符集
   - 必须使用 `utf8mb4_unicode_ci` 排序规则

3. **JDK 版本**
   - 必须使用 JDK 17 或更高版本
   - 检查: `java -version`

4. **防火墙**
   ```bash
   sudo ufw allow 8080/tcp        # Ubuntu
   sudo firewall-cmd --add-port=8080/tcp --permanent  # CentOS
   ```

5. **首次部署**
   - 建议使用 `ddl-auto: update` 自动创建表
   - 稳定运行后改为 `validate` 只验证

## 🔍 故障排查

### 启动失败

```bash
# 查看完整日志
tail -100 logs/app.log

# 查看错误信息
grep ERROR logs/app.log

# 检查端口占用
netstat -tlnp | grep 8080
```

### 数据库连接失败

```bash
# 检查MySQL服务
systemctl status mysql

# 测试连接
mysql -h localhost -P 3306 -u root -p

# 检查配置
grep -A10 "datasource:" application-prod.yml
```

### API密钥问题

```bash
# 检查配置
grep "api-key:" application-prod.yml

# 测试API
curl -X POST "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions" \
  -H "Authorization: Bearer your-api-key" \
  -H "Content-Type: application/json" \
  -d '{"model":"qwen-plus","messages":[{"role":"user","content":"test"}]}'
```

## 🎉 部署完成

访问：http://your_server_ip:8080

开始使用 AI智能小说创作系统！
EOF

# 显示文件大小
echo ""
echo "📊 文件列表："
ls -lh "$TEMP_DIR"

# 打包为tar.gz
PACKAGE_NAME="ai-novel-writer-server-$(date +%Y%m%d-%H%M%S).tar.gz"
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
echo "   cd ai-novel-writer-server"
echo ""
echo "⚙️  配置并启动："
echo "   vi application-prod.yml    # 修改数据库密码和API密钥"
echo "   ./init-database.sh         # 初始化数据库"
echo "   ./start-server.sh          # 启动服务"
echo ""
echo "📖 详细文档："
echo "   cat 快速开始.md"
echo "========================================="
