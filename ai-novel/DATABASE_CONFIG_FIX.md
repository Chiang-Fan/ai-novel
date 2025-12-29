# 数据库配置问题修复说明

## 🔍 问题根因

**错误**: `Schema-validation: missing table [chapters]`

**真正原因**: 
1. ✅ 数据库已初始化（15个表都创建成功）
2. ❌ **应用没有连接到MySQL数据库**
3. ❌ 配置文件中缺少MySQL数据源配置

### 配置文件分析

**application.yml** (默认配置)
```yaml
datasource:
  driver-class-name: org.h2.Driver  # ❌ 使用H2内存数据库
  url: jdbc:h2:file:./data/novels
```

**application-prod.yml** (生产环境 - 修复前)
```yaml
spring:
  jpa:
    ddl-auto: update
# ❌ 没有数据源配置，继承了默认的H2配置
```

结果：应用启动后连接的是 H2 数据库，而不是 MySQL！

## ✅ 已修复内容

### 1. application-prod.yml (生产环境)

```yaml
spring:
  # ✅ 新增MySQL数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ai_novel_writer}?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}
    hikari:
      maximum-pool-size: ${DB_MAX_POOL_SIZE:20}
      minimum-idle: ${DB_MIN_IDLE:5}

  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect  # ✅ 使用MySQL方言
    ddl-auto: update
```

### 2. application-dev.yml (开发环境)

同样添加了 MySQL 数据源配置。

### 3. application-local.yml (本地环境)

同样添加了 MySQL 数据源配置。

## 🔧 环境变量读取说明

Spring Boot 会自动读取以下来源的环境变量（优先级从高到低）：

1. **命令行参数**: `-Dspring.datasource.url=...`
2. **系统环境变量**: `export DB_HOST=localhost`
3. **`.env` 文件**: 需要通过 `source .env` 加载
4. **application.yml 中的默认值**: `${DB_HOST:localhost}`

### 启动脚本中的环境变量加载

**start-server.sh** 已经正确加载了 `.env`：

```bash
# 加载环境变量
if [ -f "$ENV_FILE" ]; then
    echo "📝 加载环境变量..."
    set -a
    source "$ENV_FILE"
    set +a
fi
```

然后通过 `sudo` 传递环境变量：

```bash
sudo nohup java $JVM_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &
```

⚠️ **注意**: `sudo` 默认不会传递环境变量，需要特别处理！

## 🔧 修复启动脚本

需要修改 `start-server.sh`，确保环境变量能传递给 Java 进程：

### 方式一：使用 sudo -E（推荐）

```bash
sudo -E nohup java $JVM_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &
```

### 方式二：显式传递环境变量

```bash
sudo nohup java $JVM_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -DDB_HOST=$DB_HOST \
    -DDB_PORT=$DB_PORT \
    -DDB_NAME=$DB_NAME \
    -DDB_USER=$DB_USER \
    -DDB_PASSWORD=$DB_PASSWORD \
    -DSPRING_AI_OPENAI_API_KEY=$SPRING_AI_OPENAI_API_KEY \
    -DSPRING_AI_OPENAI_BASE_URL=$SPRING_AI_OPENAI_BASE_URL \
    -DDASHSCOPE_API_KEY=$DASHSCOPE_API_KEY \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &
```

### 方式三：不使用 sudo（如果不需要）

如果端口不是 80/443，可以不用 sudo：

```bash
nohup java $JVM_OPTS \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &
```

## 📦 重新打包

配置文件已修复，需要重新编译打包：

```bash
cd ai-novel
mvn clean package -DskipTests
./package-deploy.sh
```

## 🚀 部署验证步骤

### 1. 上传新的部署包

```bash
scp ai-novel-writer-deploy-*.tar.gz your_user@your_server:/opt/
```

### 2. 解压并配置

```bash
cd /opt
tar -xzf ai-novel-writer-deploy-*.tar.gz
cd ai-novel-writer-deploy

cp .env.server .env
vi .env
```

### 3. 配置 .env 文件（重要！）

```bash
# AI服务配置
QWEN_API_KEY=sk-your-actual-key
SPRING_AI_OPENAI_API_KEY=${QWEN_API_KEY}
SPRING_AI_OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
DASHSCOPE_API_KEY=${QWEN_API_KEY}

# 数据库配置（必填）
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ai_novel_writer
DB_USER=root
DB_PASSWORD=your_mysql_password

# 服务配置
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

### 4. 初始化数据库

```bash
./init-database.sh
```

验证表是否创建成功：
```bash
mysql -u root -p ai_novel_writer -e "SHOW TABLES;"
```

应该看到 15 个表。

### 5. 启动应用

```bash
./start-server.sh
```

### 6. 验证数据库连接

查看日志确认连接的是 MySQL：

```bash
tail -f logs/app.log | grep -i mysql
```

应该看到类似：
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Initialized JPA EntityManagerFactory for persistence unit 'default'
```

### 7. 检查健康状态

```bash
curl http://localhost:8080/actuator/health
```

## 🎯 关键配置检查清单

- [x] MySQL 数据源配置已添加到所有环境配置文件
- [x] 数据库方言设置为 `MySQLDialect`
- [x] 环境变量通过 `.env` 文件配置
- [x] 启动脚本正确加载环境变量
- [x] 数据库初始化脚本已准备
- [ ] 重新编译打包（进行中）
- [ ] 上传到服务器验证

## 📝 总结

问题不是数据库未初始化，而是：
1. **应用配置文件中缺少 MySQL 数据源配置**
2. **默认使用了 H2 内存数据库**

解决方案：
1. ✅ 在所有环境配置文件中添加 MySQL 数据源配置
2. ✅ 确保环境变量正确传递给 Java 进程
3. ✅ 重新编译打包部署

现在配置已修复，重新打包后就可以正常连接 MySQL 了！
