# 云服务器部署快速指南

## 📦 方式一：使用打包脚本（推荐）

### 1. 本地打包

```bash
cd ai-novel
./package-deploy.sh
```

这将生成 `ai-novel-writer-deploy-YYYYMMDD-HHMMSS.tar.gz` 压缩包（约84MB）

### 2. 上传到服务器

```bash
scp ai-novel-writer-deploy-*.tar.gz your_user@your_server:/opt/
```

### 3. 在服务器上解压和部署

```bash
# 登录服务器
ssh your_user@your_server

# 解压
cd /opt
tar -xzf ai-novel-writer-deploy-*.tar.gz
cd ai-novel-writer-deploy

# 配置环境
cp .env.server .env
vi .env
# 修改以下配置：
# - QWEN_API_KEY=你的API密钥
# - DB_PASSWORD=你的数据库密码

# 初始化数据库（首次部署必须）
./init-database.sh

# 启动服务
./start-server.sh
```

## 📦 方式二：只上传JAR包

如果服务器上已有配置和脚本，只需更新JAR包：

```bash
# 本地
cd ai-novel
scp target/ai-novel-writer.jar your_user@your_server:/opt/ai-novel-writer/

# 服务器上
ssh your_user@your_server
cd /opt/ai-novel-writer
./restart-server.sh
```

## 🔧 服务器快速命令

```bash
# 启动服务
./start-server.sh

# 停止服务
./stop-server.sh

# 重启服务
./restart-server.sh

# 查看日志
tail -f logs/app.log

# 查看状态
curl http://localhost:8080/actuator/health
```

## ⚙️ 环境配置说明

编辑 `.env` 文件：

```bash
# 必填项
QWEN_API_KEY=sk-xxxxxxxxxxxxxx          # 通义千问API密钥
DB_PASSWORD=your_mysql_password         # MySQL密码

# 可选项（有默认值）
SERVER_PORT=8080                        # 服务端口
DB_HOST=localhost                       # 数据库地址
DB_USER=root                            # 数据库用户
DB_NAME=ai_novel_writer                 # 数据库名称
```

## 🚀 首次部署检查清单

- [ ] 安装 JDK 17+
- [ ] 安装 MySQL 5.7+
- [ ] 创建数据库 `ai_novel_writer`（或运行 init-database.sh）
- [ ] 获取通义千问 API Key
- [ ] 配置防火墙（开放8080端口）
- [ ] 配置 .env 文件
- [ ] 运行 init-database.sh（首次部署必须）
- [ ] 运行 start-server.sh

## 🌐 访问地址

部署成功后访问：
- **前端页面**: http://your_server_ip:8080
- **API文档**: http://your_server_ip:8080/docs
- **健康检查**: http://your_server_ip:8080/actuator/health

## 📖 详细文档

完整部署文档请查看：`DEPLOYMENT.md`
