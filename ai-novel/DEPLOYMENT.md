# AI智能小说创作系统 - 云服务器部署指南

## 📋 部署准备

### 系统要求

- **操作系统**: Linux (Ubuntu 20.04+, CentOS 7+, 或其他发行版)
- **Java**: JDK 17 或更高版本
- **数据库**: MySQL 5.7+ 或 MySQL 8.0+
- **内存**: 至少 2GB 可用内存
- **磁盘**: 至少 500MB 可用空间

### 必需软件安装

#### 1. 安装 JDK 17

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
java -version
```

**CentOS/RHEL:**
```bash
sudo yum install java-17-openjdk java-17-openjdk-devel -y
java -version
```

#### 2. 安装 MySQL

**Ubuntu/Debian:**
```bash
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql
sudo mysql_secure_installation
```

**CentOS/RHEL:**
```bash
sudo yum install mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld
sudo mysql_secure_installation
```

#### 3. 创建数据库

```bash
mysql -u root -p
```

在MySQL中执行：
```sql
CREATE DATABASE ai_novel_writer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'aiwriter'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ai_novel_writer.* TO 'aiwriter'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

或者使用提供的初始化脚本（推荐）：
```bash
# 将在后续步骤中使用 init-database.sh
```

## 🚀 部署步骤

### 方式一：使用部署脚本（推荐）

#### 1. 上传文件到服务器

将以下文件上传到服务器（如 `/opt/ai-novel-writer/`）：

```
ai-novel-writer/
├── ai-novel-writer.jar          # 应用JAR包 (必需)
├── start-server.sh              # 启动脚本 (必需)
├── stop-server.sh               # 停止脚本 (必需)
├── restart-server.sh            # 重启脚本 (可选)
├── init-database.sh             # 数据库初始化脚本 (必需)
├── schema.sql                   # 数据库结构SQL (必需)
├── .env.server                  # 环境配置模板 (必需)
└── DEPLOYMENT.md                # 本文档
```

**使用 scp 上传：**
```bash
# 在本地执行
cd ai-novel
scp ai-novel-writer.jar *.sh .env.server your_user@your_server:/opt/ai-novel-writer/
```

**或使用 rsync：**
```bash
rsync -avz ai-novel-writer.jar *.sh .env.server your_user@your_server:/opt/ai-novel-writer/
```

#### 2. 配置环境变量

```bash
cd /opt/ai-novel-writer
cp .env.server .env
vi .env
```

修改以下配置：
```bash
# AI服务配置
QWEN_API_KEY=sk-your-actual-api-key-here

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ai_novel_writer
DB_USER=aiwriter
DB_PASSWORD=your_mysql_password_here

# 服务端口
SERVER_PORT=8080
```

#### 3. 设置脚本权限

```bash
chmod +x start-server.sh stop-server.sh restart-server.sh init-database.sh
```

#### 4. 初始化数据库

```bash
# 方式一：使用初始化脚本（推荐）
./init-database.sh

# 方式二：手动执行SQL
mysql -u root -p ai_novel_writer < schema.sql
```

#### 5. 启动应用

```bash
./start-server.sh
```

启动成功后会显示：
```
=========================================
✅ 启动成功！
=========================================
📚 前端页面: http://localhost:8080
📖 API文档: http://localhost:8080/docs
🔍 健康检查: http://localhost:8080/actuator/health
=========================================
```

#### 5. 验证部署

```bash
# 检查健康状态
curl http://localhost:8080/actuator/health

# 查看日志
tail -f logs/app.log

# 查看进程
ps aux | grep ai-novel-writer
```

### 方式二：手动部署

#### 1. 创建目录结构

```bash
sudo mkdir -p /opt/ai-novel-writer/{logs,config}
cd /opt/ai-novel-writer
```

#### 2. 上传JAR包

```bash
scp target/ai-novel-writer.jar your_user@your_server:/opt/ai-novel-writer/
```

#### 3. 创建配置文件

```bash
cat > /opt/ai-novel-writer/.env << 'EOF'
QWEN_API_KEY=your_api_key
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ai_novel_writer
DB_USER=root
DB_PASSWORD=your_password
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
EOF
```

#### 4. 启动应用

```bash
cd /opt/ai-novel-writer
source .env

# 初始化数据库（首次部署）
mysql -u root -p < schema.sql

# 或创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ai_novel_writer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

sudo nohup java -Xms512m -Xmx2g \
    -Dspring.profiles.active=prod \
    -Dserver.port=8080 \
    -jar ai-novel-writer.jar \
    > logs/app.log 2>&1 &

echo $! > app.pid
```

## 🔧 服务管理

### 查看服务状态

```bash
# 查看进程
ps aux | grep ai-novel-writer

# 查看端口
netstat -tlnp | grep 8080

# 查看实时日志
tail -f logs/app.log

# 查看健康状态
curl http://localhost:8080/actuator/health
```

### 停止服务

```bash
./stop-server.sh
```

### 重启服务

```bash
./restart-server.sh
```

### 查看日志

```bash
# 查看最新日志
tail -100 logs/app.log

# 实时监控日志
tail -f logs/app.log

# 搜索错误日志
grep ERROR logs/app.log

# 搜索启动日志
grep "Started AiNovelWriterApplication" logs/app.log
```

## 🌐 反向代理配置（可选）

### 使用 Nginx

#### 1. 安装 Nginx

```bash
sudo apt install nginx -y  # Ubuntu
sudo yum install nginx -y  # CentOS
```

#### 2. 配置 Nginx

创建配置文件：`/etc/nginx/sites-available/ai-novel-writer`

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 访问日志
    access_log /var/log/nginx/ai-novel-writer.access.log;
    error_log /var/log/nginx/ai-novel-writer.error.log;

    # 反向代理到Java应用
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://localhost:8080;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

#### 3. 启用配置

```bash
sudo ln -s /etc/nginx/sites-available/ai-novel-writer /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 配置 HTTPS（使用 Let's Encrypt）

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取SSL证书
sudo certbot --nginx -d your-domain.com

# 自动续期（已自动配置）
sudo certbot renew --dry-run
```

## 🔒 安全加固

### 1. 防火墙配置

```bash
# Ubuntu (UFW)
sudo ufw allow 22/tcp      # SSH
sudo ufw allow 80/tcp      # HTTP
sudo ufw allow 443/tcp     # HTTPS
sudo ufw enable

# CentOS (Firewalld)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 2. 限制文件权限

```bash
cd /opt/ai-novel-writer
sudo chown -R your_user:your_group .
chmod 600 .env
chmod 700 *.sh
chmod 644 ai-novel-writer.jar
```

### 3. 配置系统服务（Systemd）

创建服务文件：`/etc/systemd/system/ai-novel-writer.service`

```ini
[Unit]
Description=AI Novel Writer Application
After=network.target mysql.service

[Service]
Type=simple
User=your_user
Group=your_group
WorkingDirectory=/opt/ai-novel-writer
EnvironmentFile=/opt/ai-novel-writer/.env
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar /opt/ai-novel-writer/ai-novel-writer.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/ai-novel-writer/logs/app.log
StandardError=append:/opt/ai-novel-writer/logs/app.log

[Install]
WantedBy=multi-user.target
```

启用服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable ai-novel-writer
sudo systemctl start ai-novel-writer
sudo systemctl status ai-novel-writer
```

## 📊 监控和维护

### 日志管理

配置日志轮转：`/etc/logrotate.d/ai-novel-writer`

```
/opt/ai-novel-writer/logs/*.log {
    daily
    rotate 7
    compress
    delaycompress
    notifempty
    create 0640 your_user your_group
    sharedscripts
    postrotate
        systemctl reload ai-novel-writer > /dev/null 2>&1 || true
    endscript
}
```

### 性能监控

```bash
# CPU和内存使用
top -p $(cat /opt/ai-novel-writer/app.pid)

# JVM内存详情
jmap -heap $(cat /opt/ai-novel-writer/app.pid)

# 线程堆栈
jstack $(cat /opt/ai-novel-writer/app.pid)

# GC日志（需要在JVM_OPTS中添加GC日志参数）
# -Xlog:gc*:file=logs/gc.log:time,uptime:filecount=10,filesize=10M
```

## 🔍 故障排查

### 常见问题

#### 1. 启动失败

```bash
# 查看详细日志
tail -100 logs/app.log

# 检查端口占用
netstat -tlnp | grep 8080

# 检查Java版本
java -version

# 测试数据库连接
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD -e "SELECT 1;"
```

#### 2. 数据库连接失败

```bash
# 检查MySQL服务
systemctl status mysql

# 检查防火墙
sudo iptables -L | grep 3306

# 测试连接
telnet localhost 3306
```

#### 3. 内存不足

```bash
# 调整JVM参数
vi .env
# 添加: JVM_OPTS=-Xms256m -Xmx1g

# 或在启动脚本中修改
vi start-server.sh
```

#### 4. API密钥问题

```bash
# 验证环境变量
cat .env | grep QWEN_API_KEY

# 测试API连接
curl -X POST "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions" \
  -H "Authorization: Bearer $QWEN_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"qwen-plus","messages":[{"role":"user","content":"test"}]}'
```

## 📞 技术支持

如遇到问题，请提供以下信息：

1. 操作系统版本：`cat /etc/os-release`
2. Java版本：`java -version`
3. 应用日志：`tail -100 logs/app.log`
4. 环境配置：`cat .env`（隐藏敏感信息）
5. 错误信息和重现步骤

## 📝 更新日志

- **v1.0.0** (2025-12-29): 初始云服务器部署版本
  - 支持sudo nohup后台运行
  - 完整的环境配置
  - 优雅的启停脚本
  - 详细的部署文档
