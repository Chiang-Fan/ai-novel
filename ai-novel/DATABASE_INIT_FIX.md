# 数据库初始化问题已修复 - 部署说明

## 🔧 问题说明

**原始错误**: `nested exception is org.hibernate.tool.schema.spi.SchemaManagementException: Schema-validation: missing table [chapters]`

**原因**: 
1. 生产环境配置 `application-prod.yml` 中 `ddl-auto: validate` 只验证不创建表
2. 缺少完整的数据库初始化SQL脚本

## ✅ 已完成的修复

### 1. 创建完整的数据库初始化脚本

**文件**: `src/main/resources/db/schema.sql` (13KB)

包含所有表结构：
- ✅ 核心业务表: `novels`, `chapters`, `users`
- ✅ 创作辅助表: `characters`, `scenes`, `outlines`
- ✅ 辅助功能表: `edit_history`, `character_relationships`, `world_settings`, `plot_threads`, `content_analyses`, `continuation_suggestions`, `outline_nodes`

共 **15 个表** 的完整结构定义。

### 2. 创建数据库初始化脚本

**文件**: `init-database.sh` (3.3KB)

功能：
- ✅ 自动读取 `.env` 配置
- ✅ 检查 MySQL 连接
- ✅ 执行 `schema.sql` 初始化表结构
- ✅ 显示创建的表列表
- ✅ 友好的错误提示

### 3. 修改生产环境配置

**文件**: `application-prod.yml`

```yaml
# 修改前
ddl-auto: validate  # 只验证，不创建表

# 修改后
ddl-auto: update    # 自动创建/更新表结构
```

### 4. 增强启动脚本

**文件**: `start-server.sh`

新增功能：
- ✅ 启动前检查数据库连接
- ✅ 如果数据库未初始化，提示运行 `init-database.sh`
- ✅ 可选择继续或取消启动

### 5. 更新打包脚本

**文件**: `package-deploy.sh`

新增打包内容：
- ✅ `init-database.sh` - 数据库初始化脚本
- ✅ `schema.sql` - 数据库结构SQL
- ✅ `QUICK_DEPLOY.md` - 快速部署指南

## 📦 部署包内容

已生成: **ai-novel-writer-deploy-20251229-200125.tar.gz** (81MB)

```
ai-novel-writer-deploy/
├── ai-novel-writer.jar          # 应用程序 (84MB)
├── start-server.sh              # 启动脚本
├── stop-server.sh               # 停止脚本
├── restart-server.sh            # 重启脚本
├── init-database.sh             # 🆕 数据库初始化脚本
├── schema.sql                   # 🆕 数据库结构SQL
├── .env.server                  # 环境配置模板
├── DEPLOYMENT.md                # 详细部署文档
├── QUICK_DEPLOY.md              # 快速部署指南
├── README.md                    # 项目说明
└── README_FIRST.txt             # 快速入门
```

## 🚀 云服务器部署步骤

### 1. 上传部署包

```bash
scp ai-novel-writer-deploy-20251229-200125.tar.gz your_user@your_server:/opt/
```

### 2. 解压

```bash
ssh your_user@your_server
cd /opt
tar -xzf ai-novel-writer-deploy-20251229-200125.tar.gz
cd ai-novel-writer-deploy
```

### 3. 配置环境

```bash
cp .env.server .env
vi .env
```

修改以下必填项：
```bash
# AI服务配置（必填）
QWEN_API_KEY=sk-your-actual-api-key

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ai_novel_writer
DB_USER=root
DB_PASSWORD=your_mysql_password

# 服务端口
SERVER_PORT=8080
```

### 4. 初始化数据库（首次部署必须）

```bash
./init-database.sh
```

这会：
- ✅ 创建数据库 `ai_novel_writer`
- ✅ 创建所有15个数据表
- ✅ 显示表列表确认

### 5. 启动服务

```bash
./start-server.sh
```

成功后会显示：
```
✅ 启动成功！
📚 前端页面: http://localhost:8080
📖 API文档: http://localhost:8080/docs
🔍 健康检查: http://localhost:8080/actuator/health
```

## 🔍 验证部署

### 1. 检查数据库表

```bash
mysql -u root -p ai_novel_writer -e "SHOW TABLES;"
```

应该看到 15 个表：
- chapters
- character_relationships
- characters
- content_analyses
- continuation_suggestions
- edit_history
- novels
- outline_nodes
- outlines
- plot_threads
- scenes
- users
- world_settings

### 2. 检查应用状态

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 查看日志
tail -f logs/app.log

# 查看进程
ps aux | grep ai-novel-writer
```

## 🛠️ 常用命令

```bash
# 启动服务
./start-server.sh

# 停止服务
./stop-server.sh

# 重启服务
./restart-server.sh

# 查看实时日志
tail -f logs/app.log

# 查看进程
cat app.pid
ps aux | grep $(cat app.pid)
```

## ⚠️ 注意事项

1. **首次部署必须先初始化数据库**
   - 运行 `./init-database.sh` 创建表结构
   - 或者手动执行 `mysql < schema.sql`

2. **数据库配置**
   - 确保 MySQL 服务正在运行
   - 确保数据库用户有足够权限
   - 建议使用 utf8mb4 字符集

3. **JDK 版本**
   - 必须使用 JDK 17 或更高版本
   - 检查: `java -version`

4. **防火墙配置**
   - 开放 8080 端口（或配置的端口）
   - `sudo ufw allow 8080/tcp` (Ubuntu)
   - `sudo firewall-cmd --add-port=8080/tcp --permanent` (CentOS)

5. **内存配置**
   - 默认 JVM 配置: `-Xms512m -Xmx2g`
   - 如需调整，修改 `.env` 中的 `JVM_OPTS`

## 📖 详细文档

- **QUICK_DEPLOY.md** - 快速部署指南
- **DEPLOYMENT.md** - 完整部署文档（含 Nginx、HTTPS、Systemd 配置）

## 🎉 部署完成

所有问题已修复，部署包已准备好！可以直接上传到云服务器进行部署了。
