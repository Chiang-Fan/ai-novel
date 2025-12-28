# AI智能小说创作系统 - Spring Boot版本

> 基于Spring Boot 3 + Spring AI + MySQL的智能小说创作辅助系统

## 📋 项目概述

本项目是原Python(FastAPI)版本的Java重构版本,采用Spring Boot框架,集成Spring AI实现智能小说续写功能。

### 🎯 核心特性

- ✅ **AI智能续写**: 基于上下文的章节自动生成
- ✅ **场景管理**: 场景规划、氛围设定、进度跟踪  
- ✅ **角色管理**: 详细的人物档案、关系网络
- ✅ **大纲规划**: 树形结构的故事大纲
- ✅ **情节线管理**: 伏笔埋设与揭示
- ✅ **风格保持**: 自动分析并保持写作风格
- ✅ **智能建议**: 续写方向、场景、情节建议

### 🏗️ 技术栈

```
核心框架: Spring Boot 3.2.1
持久化: Spring Data JPA + MySQL 8.0  
AI集成: Spring AI (OpenAI Compatible)
Java版本: JDK 17
构建工具: Maven 3.8+
部署方式: Fat JAR (内嵌Tomcat)
```

## 🚀 快速开始

### 1. 环境要求

```bash
JDK 17+
Maven 3.8+
MySQL 8.0+
通义千问API Key(或其他OpenAI兼容的API)
```

### 2. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE ai_novel_writer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户(可选)
CREATE USER 'ainovel'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ai_novel_writer.* TO 'ainovel'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 配置文件

创建 `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_novel_writer?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password
  
spring.ai:
  openai:
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    api-key: your-dashscope-api-key
```

### 4. 编译打包

```bash
# 清理并打包
mvn clean package -DskipTests

# 或者包含测试
mvn clean package
```

### 5. 运行应用

```bash
# 开发环境运行
java -jar target/ai-novel-writer.jar --spring.profiles.active=dev

# 生产环境运行
java -jar target/ai-novel-writer.jar --spring.profiles.active=prod \
  --spring.datasource.password=your_password \
  --spring.ai.openai.api-key=your_api_key
```

### 6. 访问应用

- **前端页面**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

## 📁 项目结构

```
src/main/java/com/ai/novel/
├── AiNovelApplication.java          # 启动类
├── config/                           # 配置类
│   ├── AIServiceConfig.java         # AI服务配置
│   ├── WebMvcConfig.java            # Web配置
│   └── DataSourceConfig.java        # 数据源配置
├── entity/                           # 实体类
│   ├── Novel.java                   # 小说
│   ├── Chapter.java                 # 章节
│   ├── Character.java               # 角色
│   ├── Scene.java                   # 场景
│   ├── PlotThread.java              # 情节线
│   └── enums/                       # 枚举类型
├── repository/                       # 数据访问层
│   ├── NovelRepository.java
│   ├── ChapterRepository.java
│   └── ...
├── service/                          # 业务逻辑层
│   ├── ai/
│   │   ├── AIService.java           # AI服务封装
│   │   └── PromptManager.java       # 提示词管理
│   ├── NovelService.java
│   ├── ChapterService.java          # 核心续写服务
│   └── ...
├── controller/                       # 控制器层
│   ├── NovelController.java
│   ├── ChapterController.java
│   └── ...
├── dto/                              # 数据传输对象
│   ├── request/
│   └── response/
└── exception/                        # 异常处理
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yml                   # 主配置
├── application-dev.yml              # 开发配置
├── application-prod.yml             # 生产配置
├── db/migration/                     # 数据库迁移脚本
│   └── V1__Init_Schema.sql
└── static/                           # 前端静态资源
    ├── index.html
    ├── js/
    └── css/
```

## 🔧 配置说明

### AI API配置

支持通义千问(DashScope)及其他OpenAI兼容API:

```yaml
spring.ai:
  openai:
    # API基础URL
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    # API密钥
    api-key: ${AI_API_KEY}
    # 模型配置
    chat:
      options:
        model: qwen-plus  # 或 qwen-max, qwen-turbo
        temperature: 0.7
        max-tokens: 4000
```

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/${DB_NAME:ai_novel_writer}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
```

## 📝 核心API示例

### 创建小说

```bash
POST /api/novels
Content-Type: application/json

{
  "title": "穿越异世界的冒险",
  "outline": "主角穿越到异世界,开启冒险之旅...",
  "sampleText": "样本文本用于风格提取..."
}
```

### AI续写章节

```bash
POST /api/chapters/continue
Content-Type: application/json

{
  "novelId": 1,
  "writingDirection": "描述主角初到异世界的震撼和困惑",
  "targetWordCount": 2000,
  "sceneId": 1
}
```

### 查询章节列表

```bash
GET /api/novels/1/chapters
```

## 🚢 部署指南

### 方式一: 直接运行JAR

```bash
# 生产环境启动脚本
#!/bin/bash
nohup java -jar \
  -Xms512m -Xmx2g \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url=jdbc:mysql://your-db-host:3306/ai_novel_writer \
  -Dspring.datasource.password=your_password \
  -Dspring.ai.openai.api-key=your_api_key \
  target/ai-novel-writer.jar \
  > app.log 2>&1 &
```

### 方式二: Systemd服务

创建 `/etc/systemd/system/ai-novel-writer.service`:

```ini
[Unit]
Description=AI Novel Writer Service
After=syslog.target network.target

[Service]
Type=simple
User=your_user
WorkingDirectory=/path/to/app
ExecStart=/usr/bin/java -jar \
  -Xms512m -Xmx2g \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.password=${DB_PASSWORD} \
  -Dspring.ai.openai.api-key=${AI_API_KEY} \
  /path/to/app/ai-novel-writer.jar

StandardOutput=journal
StandardError=journal
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务:

```bash
sudo systemctl daemon-reload
sudo systemctl enable ai-novel-writer
sudo systemctl start ai-novel-writer
sudo systemctl status ai-novel-writer
```

### 方式三: Docker部署

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/ai-novel-writer.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t ai-novel-writer:1.0.0 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ai_novel_writer \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e SPRING_AI_OPENAI_API_KEY=your_api_key \
  --name ai-novel-writer \
  ai-novel-writer:1.0.0
```

## 📊 性能优化建议

### JVM参数优化

```bash
java -jar \
  -Xms1g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/heapdump.hprof \
  ai-novel-writer.jar
```

### 数据库连接池优化

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
```

## 🔍 监控与日志

### 日志配置

日志文件位置: `logs/ai-novel-writer.log`

```yaml
logging:
  level:
    root: INFO
    com.ai.novel: DEBUG
  file:
    name: logs/ai-novel-writer.log
    max-size: 10MB
    max-history: 30
```

### 健康检查

```bash
# 基础健康检查
curl http://localhost:8080/actuator/health

# 详细信息
curl http://localhost:8080/actuator/health | jq .

# 指标查看
curl http://localhost:8080/actuator/metrics
```

## 🐛 常见问题

### 1. 数据库连接失败

```
解决方案:
1. 检查MySQL是否启动
2. 确认数据库名称和用户权限
3. 检查防火墙设置
4. 验证JDBC URL格式
```

### 2. AI API调用失败

```
解决方案:
1. 确认API Key是否正确
2. 检查网络连接
3. 验证API配额是否充足
4. 查看日志中的具体错误信息
```

### 3. 前端资源404

```
解决方案:
1. 确认前端构建产物已打包到jar中
2. 检查静态资源路径配置
3. 清理Maven缓存重新打包
```

## 📄 许可证

本项目采用 MIT 许可证

## 👥 贡献指南

欢迎提交Issue和Pull Request!

---

**开发团队**: AI Novel Writer Team  
**版本**: v1.0.0  
**更新时间**: 2025-12-27
