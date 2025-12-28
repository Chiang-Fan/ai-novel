# 🐳 AI Novel Writer - Docker 完整部署方案

## 📋 目录

1. [部署架构](#部署架构)
2. [前置要求](#前置要求)
3. [快速开始](#快速开始)
4. [详细配置](#详细配置)
5. [数据持久化](#数据持久化)
6. [验证部署](#验证部署)
7. [常见问题](#常见问题)
8. [运维命令](#运维命令)

---

## 🏗️ 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                      宿主机 (Host)                           │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Docker Network (ai-novel-network)          │  │
│  │                                                      │  │
│  │  ┌─────────────────┐      ┌────────────────────┐   │  │
│  │  │  MySQL 8.0      │      │  Spring Boot App   │   │  │
│  │  │  Container      │◄─────┤  Container         │   │  │
│  │  │                 │      │                    │   │  │
│  │  │  Port: 3306     │      │  Port: 8080        │   │  │
│  │  │                 │      │                    │   │  │
│  │  └────────┬────────┘      └──────────┬─────────┘   │  │
│  │           │                          │             │  │
│  └───────────┼──────────────────────────┼─────────────┘  │
│              │                          │                │
│              ▼                          ▼                │
│  ┌───────────────────┐     ┌────────────────────┐       │
│  │ mysql_data        │     │  app_logs          │       │
│  │ (持久化数据卷)     │     │  (日志目录)         │       │
│  │ ./docker/data/    │     │  ./docker/logs/    │       │
│  │     mysql/        │     │                    │       │
│  └───────────────────┘     └────────────────────┘       │
│                                                          │
│  映射端口:                                                │
│  - 3306 → MySQL                                          │
│  - 8080 → Spring Boot API + 前端页面                      │
└──────────────────────────────────────────────────────────┘
```

---

## 📦 前置要求

### 必备软件

- **Docker**: 20.10+ 
- **Docker Compose**: 2.0+ (或 docker-compose 1.29+)
- **操作系统**: Linux / macOS / Windows (WSL2)
- **最小资源**: 2GB RAM, 10GB 磁盘空间

### 安装Docker

#### Ubuntu/Debian
```bash
curl -fsSL https://get.docker.com | bash -s docker
sudo usermod -aG docker $USER
```

#### macOS
```bash
brew install docker docker-compose
# 或下载 Docker Desktop: https://www.docker.com/products/docker-desktop
```

#### CentOS/RHEL
```bash
sudo yum install -y docker docker-compose
sudo systemctl enable docker
sudo systemctl start docker
```

---

## ⚡️ 快速开始（3分钟部署）

### 步骤1: 初始化配置

```bash
# 1. 初始化配置文件
./docker-deploy.sh --init

# 2. 编辑 .env 文件，配置API密钥
vim .env
# 必须修改: DASHSCOPE_API_KEY=sk-your-real-api-key
```

### 步骤2: 一键部署

```bash
# 构建并启动服务（首次部署）
./docker-deploy.sh -b -u
```

### 步骤3: 验证部署

部署脚本会自动验证，或手动执行：

```bash
./docker-deploy.sh --verify
```

**成功标志**: 
- ✅ 输出 "部署成功！"
- ✅ 健康检查返回 `"status": "UP"`

---

## 🔧 详细配置

### 1. 环境变量配置 (.env)

创建或编辑 `.env` 文件：

```bash
# 复制示例文件
cp .env.example .env

# 编辑配置
vim .env
```

**关键配置项**:

```bash
# ============= 必填项 =============
# AI服务密钥（必须配置！）
DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxx

# MySQL密码（建议修改）
MYSQL_ROOT_PASSWORD=your_strong_password_here
MYSQL_PASSWORD=your_app_password_here

# ============= 可选项 =============
# 端口配置
APP_PORT=8080
MYSQL_PORT=3306

# 数据持久化路径
MYSQL_DATA_PATH=./docker/data/mysql
APP_LOGS_PATH=./docker/logs

# Spring配置
SPRING_PROFILES_ACTIVE=prod

# JVM配置
JAVA_OPTS=-Xms512m -Xmx1024m
```

### 2. MySQL配置文件

位置: `docker/mysql/conf.d/my.cnf`

已包含优化配置：
- UTF-8字符集
- 最大连接数500
- InnoDB缓冲池512M
- 慢查询日志

可根据需求调整参数。

### 3. 应用配置文件

编辑 `src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境使用validate
    show-sql: false
```

---

## 💾 数据持久化方案

### 关键设计

Docker Compose配置了**三个数据卷**，确保数据不会因容器重启而丢失：

#### 1. MySQL数据卷 (最重要！)

```yaml
volumes:
  mysql_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: ./docker/data/mysql  # 宿主机目录
```

**映射关系**:
```
宿主机: ./docker/data/mysql
  ↓
容器内: /var/lib/mysql
```

#### 2. 应用日志卷

```yaml
app_logs:
  driver: local
  driver_opts:
    device: ./docker/logs
```

#### 3. 应用数据卷

```yaml
app_data:
  driver: local
  driver_opts:
    device: ./docker/data/app
```

### 数据目录结构

```
docker/
├── data/
│   ├── mysql/          # MySQL数据文件（持久化）
│   │   ├── ibdata1
│   │   ├── mysql/
│   │   └── ai_novel_writer/
│   └── app/            # 应用数据文件
└── logs/               # 应用日志
    ├── app.log
    └── gc.log
```

### 验证持久化

```bash
# 1. 创建测试数据
curl -X POST http://localhost:8080/api/novels \
  -H "Content-Type: application/json" \
  -d '{"title":"测试小说","author":"测试"}'

# 2. 重启容器
docker-compose restart

# 3. 验证数据仍存在
curl http://localhost:8080/api/novels
```

---

## ✅ 验证部署

### 自动验证

```bash
./docker-deploy.sh --verify
```

### 手动验证步骤

#### 1. 检查容器状态

```bash
docker-compose ps

# 预期输出:
# NAME              STATUS        PORTS
# ai-novel-mysql    Up (healthy)  0.0.0.0:3306->3306/tcp
# ai-novel-app      Up (healthy)  0.0.0.0:8080->8080/tcp
```

#### 2. 健康检查

```bash
# 应用健康检查
curl http://localhost:8080/api/health

# 预期响应:
{
  "code": 200,
  "message": "Success",
  "data": {
    "status": "UP",
    "timestamp": "2025-12-27T10:00:00",
    "application": "AI Novel Writer",
    "version": "1.0.0"
  }
}
```

#### 3. 数据库连接测试

```bash
# 进入MySQL容器
docker exec -it ai-novel-mysql mysql -unovel_user -p

# 输入密码后，执行:
USE ai_novel_writer;
SHOW TABLES;
SELECT COUNT(*) FROM novels;
```

#### 4. 查看应用日志

```bash
# 实时查看应用日志
docker-compose logs -f app

# 查看最近100行
docker-compose logs --tail=100 app
```

#### 5. 测试API功能

```bash
# 创建小说
curl -X POST http://localhost:8080/api/novels \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试小说",
    "author": "张三",
    "type": "玄幻",
    "targetWordCount": 100000
  }'

# 查询小说列表
curl http://localhost:8080/api/novels

# AI续写测试
curl -X POST http://localhost:8080/api/chapters/continue \
  -H "Content-Type: application/json" \
  -d '{
    "novelId": 1,
    "chapterNumber": 1,
    "direction": "主角穿越到异世界",
    "temperature": 0.7,
    "maxLength": 1000
  }'
```

---

## 🌐 前端页面访问验证

### 1. 浏览器访问

打开浏览器访问: **http://localhost:8080**

#### 如果有前端页面：
- 应该看到React应用的首页
- 可以进行小说创建、章节管理等操作

#### 如果没有前端（仅API）：
- 会返回404或默认的Spring Boot页面
- **解决方法**: 构建前端并打包到JAR中

### 2. 前端集成（如果前端已开发）

```bash
# 1. 构建前端
cd frontend
npm install
npm run build

# 2. 复制到Spring Boot静态资源目录
cp -r build/* ../src/main/resources/static/

# 3. 重新构建Docker镜像
cd ..
./docker-deploy.sh -b -u
```

### 3. API文档访问

如果配置了Swagger/OpenAPI:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API文档**: http://localhost:8080/v3/api-docs

### 4. 使用Postman测试

导入API测试集合:

```bash
# 导出Postman Collection
curl http://localhost:8080/v3/api-docs -o api-docs.json
```

---

## 🔧 运维命令

### 日常操作

```bash
# 查看服务状态
./docker-deploy.sh -s
docker-compose ps

# 查看日志
./docker-deploy.sh -l
docker-compose logs -f app      # 应用日志
docker-compose logs -f mysql    # MySQL日志

# 重启服务
./docker-deploy.sh -r
docker-compose restart

# 停止服务（保留数据）
./docker-deploy.sh -d
docker-compose down

# 启动服务
./docker-deploy.sh -u
docker-compose up -d
```

### 数据备份

```bash
# 备份MySQL数据
docker exec ai-novel-mysql mysqldump \
  -unovel_user -pnovel_pass_123456 \
  ai_novel_writer > backup_$(date +%Y%m%d).sql

# 备份数据目录
tar -czf mysql_data_backup_$(date +%Y%m%d).tar.gz \
  docker/data/mysql
```

### 数据恢复

```bash
# 恢复MySQL数据
docker exec -i ai-novel-mysql mysql \
  -unovel_user -pnovel_pass_123456 \
  ai_novel_writer < backup_20251227.sql
```

### 容器管理

```bash
# 进入应用容器
docker exec -it ai-novel-app bash

# 进入MySQL容器
docker exec -it ai-novel-mysql bash

# 查看容器资源使用
docker stats ai-novel-app ai-novel-mysql

# 查看容器日志（最近100行）
docker logs --tail=100 ai-novel-app
```

### 镜像管理

```bash
# 查看镜像
docker images | grep ai-novel

# 删除旧镜像
docker image prune -a

# 重新构建镜像
./docker-deploy.sh -b
```

---

## ❓ 常见问题

### Q1: 容器启动失败

**问题**: `docker-compose up -d` 后容器立即退出

**排查步骤**:
```bash
# 1. 查看容器日志
docker-compose logs app
docker-compose logs mysql

# 2. 检查端口占用
lsof -i :8080
lsof -i :3306

# 3. 检查.env配置
cat .env | grep DASHSCOPE_API_KEY
```

**常见原因**:
- API密钥未配置或无效
- 端口被占用
- MySQL启动失败（密码错误）

---

### Q2: 数据库连接失败

**问题**: 应用日志显示 "Connection refused" 或 "Access denied"

**解决方法**:
```bash
# 1. 检查MySQL容器状态
docker ps | grep mysql

# 2. 检查数据库连接配置
docker exec ai-novel-app env | grep DATASOURCE

# 3. 测试数据库连接
docker exec ai-novel-mysql mysql -unovel_user -pnovel_pass_123456 -e "SELECT 1"

# 4. 重启MySQL容器
docker-compose restart mysql
```

---

### Q3: AI续写无响应

**问题**: API调用超时或返回错误

**排查步骤**:
```bash
# 1. 检查API密钥
docker exec ai-novel-app env | grep DASHSCOPE_API_KEY

# 2. 测试网络连接
docker exec ai-novel-app curl https://dashscope.aliyuncs.com

# 3. 查看应用日志
docker-compose logs app | grep -i "ai\|error"
```

---

### Q4: 数据丢失

**问题**: 重启后数据消失

**检查数据卷**:
```bash
# 1. 查看数据卷配置
docker volume inspect ai-write-agent_mysql_data

# 2. 检查数据目录
ls -lh docker/data/mysql

# 3. 验证数据卷挂载
docker inspect ai-novel-mysql | grep -A 10 "Mounts"
```

**预防措施**:
- 定期备份数据
- 使用命名卷而非匿名卷
- 不要使用 `docker-compose down -v`（会删除卷）

---

### Q5: 内存不足

**问题**: 容器OOM或性能差

**优化方案**:
```bash
# 1. 调整JVM内存（编辑.env）
JAVA_OPTS=-Xms256m -Xmx512m

# 2. 调整MySQL内存（编辑docker/mysql/conf.d/my.cnf）
innodb_buffer_pool_size=256M

# 3. 重启服务
./docker-deploy.sh -r
```

---

### Q6: 前端页面404

**问题**: 访问 http://localhost:8080 返回404

**解决方法**:

**方案1: 确认前端已打包**
```bash
# 检查静态资源
ls -lh src/main/resources/static/

# 如果为空，构建前端
cd frontend && npm run build
cp -r build/* ../src/main/resources/static/
```

**方案2: 重新构建镜像**
```bash
./docker-deploy.sh -b -u
```

**方案3: 直接访问API**
```bash
# API接口正常访问
curl http://localhost:8080/api/health
curl http://localhost:8080/api/novels
```

---

## 🚀 高级配置

### 1. 反向代理（Nginx）

如果需要HTTPS或域名访问，添加Nginx服务:

```yaml
# 在docker-compose.yml中添加:
nginx:
  image: nginx:alpine
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./nginx.conf:/etc/nginx/nginx.conf:ro
    - ./ssl:/etc/nginx/ssl:ro
  depends_on:
    - app
```

### 2. 多实例部署

```bash
# 启动多个应用实例
docker-compose up -d --scale app=3
```

### 3. Docker Swarm集群

```bash
# 初始化Swarm
docker swarm init

# 部署Stack
docker stack deploy -c docker-compose.yml ai-novel
```

---

## 📊 监控与日志

### Prometheus + Grafana

添加监控服务（可选）:

```yaml
prometheus:
  image: prom/prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana
  ports:
    - "3000:3000"
  depends_on:
    - prometheus
```

### 日志收集

```bash
# 集中查看日志
docker-compose logs -f --tail=100

# 导出日志
docker-compose logs > app_logs_$(date +%Y%m%d).log
```

---

## 🎉 总结

### 部署完成检查清单

- ✅ Docker和Docker Compose已安装
- ✅ `.env` 文件已配置（API密钥）
- ✅ 数据持久化目录已创建
- ✅ MySQL容器运行正常（健康检查通过）
- ✅ 应用容器运行正常（健康检查通过）
- ✅ API接口可正常访问
- ✅ 数据库连接正常
- ✅ AI续写功能测试通过
- ✅ 前端页面可访问（如果已集成）
- ✅ 数据持久化验证通过

### 关键命令速查

| 操作 | 命令 |
|-----|------|
| 初始化 | `./docker-deploy.sh --init` |
| 构建启动 | `./docker-deploy.sh -b -u` |
| 查看状态 | `./docker-deploy.sh -s` |
| 查看日志 | `./docker-deploy.sh -l` |
| 重启服务 | `./docker-deploy.sh -r` |
| 停止服务 | `./docker-deploy.sh -d` |
| 验证部署 | `./docker-deploy.sh --verify` |

### 访问地址

- **应用首页**: http://localhost:8080
- **健康检查**: http://localhost:8080/api/health
- **API基地址**: http://localhost:8080/api
- **MySQL**: localhost:3306

---

**部署成功！** 🎊

如有问题，请查看[常见问题](#常见问题)或检查容器日志。
