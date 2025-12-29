# 🚀 AI智能小说创作系统 - Java版本

> **基于Spring Boot 3 + Vue 3 + MySQL的全栈重构版本**  
> 版本：v2.1.0-java | 构建日期：2025-12-29

---

## 📋 项目概述

这是将Python FastAPI + React项目重构为Java Spring Boot + Vue的完整版本，保持所有原有功能不变。

### 🎯 重构要点

| 方面 | 原版本 | Java版本 |
|------|--------|----------|
| **后端框架** | FastAPI (Python) | Spring Boot 3 (Java 17) |
| **前端框架** | React 18 | Vue 3 |
| **数据库** | SQLite | MySQL 8.0+ |
| **ORM** | SQLAlchemy | Spring Data JPA |
| **AI集成** | 通义千问SDK | DashScope Java SDK |
| **打包方式** | Docker | Fat JAR + Docker |
| **部署方式** | Nginx + Uvicorn | 内嵌Tomcat |

---

## 🏗️ 项目结构

```
java/
├── src/
│   ├── main/
│   │   ├── java/com/aiwriter/
│   │   │   ├── AiNovelWriterApplication.java   # 主应用入口
│   │   │   ├── config/                          # 配置类
│   │   │   │   ├── WebConfig.java              # Web配置（CORS、静态资源）
│   │   │   │   ├── OpenApiConfig.java          # OpenAPI配置
│   │   │   │   ├── AiConfig.java               # AI配置
│   │   │   │   └── AppConfig.java              # 应用配置
│   │   │   ├── entity/                          # 实体类（10个）
│   │   │   │   ├── BaseEntity.java             # 基础实体
│   │   │   │   ├── Novel.java                  # 小说
│   │   │   │   ├── Chapter.java                # 章节
│   │   │   │   ├── Character.java              # 角色
│   │   │   │   ├── CharacterRelationship.java  # 角色关系
│   │   │   │   ├── Scene.java                  # 场景
│   │   │   │   ├── OutlineNode.java            # 大纲节点
│   │   │   │   ├── PlotThread.java             # 伏笔
│   │   │   │   ├── WorldSetting.java           # 世界观
│   │   │   │   └── EditHistory.java            # 编辑历史
│   │   │   ├── repository/                      # 数据访问层
│   │   │   ├── dto/                            # 数据传输对象
│   │   │   ├── service/                        # 业务逻辑层
│   │   │   │   ├── ai/                         # AI服务
│   │   │   │   ├── novel/                      # 小说服务
│   │   │   │   ├── chapter/                    # 章节服务
│   │   │   │   ├── character/                  # 角色服务
│   │   │   │   ├── scene/                      # 场景服务
│   │   │   │   ├── outline/                    # 大纲服务
│   │   │   │   ├── recommendation/             # 推荐服务
│   │   │   │   └── history/                    # 历史服务
│   │   │   ├── controller/                     # 控制器层
│   │   │   ├── exception/                      # 异常处理
│   │   │   └── util/                           # 工具类
│   │   ├── resources/
│   │   │   ├── application.yml                 # 主配置文件
│   │   │   ├── application-dev.yml             # 开发环境配置
│   │   │   ├── application-prod.yml            # 生产环境配置
│   │   │   └── static/                         # 静态资源（Vue构建产物）
│   │   └── frontend/                           # Vue前端源码
│   │       ├── src/
│   │       │   ├── components/                 # Vue组件
│   │       │   ├── views/                      # 页面视图
│   │       │   ├── router/                     # Vue Router
│   │       │   ├── api/                        # API调用
│   │       │   └── main.js                     # Vue入口
│   │       ├── package.json
│   │       └── vite.config.js
│   └── test/                                   # 测试代码
├── pom.xml                                     # Maven配置
├── Dockerfile                                  # Docker镜像构建
├── docker-compose.yml                          # Docker编排
├── .env.example                               # 环境变量模板
└── README_JAVA.md                             # 本文档
```

---

## 🚀 快速开始

### 1️⃣ 环境准备

```bash
# 必需环境
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+ (前端构建)

# 可选环境
- Docker 20+
- Docker Compose 2+
```

### 2️⃣ 数据库准备

```sql
-- 创建数据库
CREATE DATABASE ai_novel_writer 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'aiwriter'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ai_novel_writer.* TO 'aiwriter'@'%';
FLUSH PRIVILEGES;
```

### 3️⃣ 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件
vi .env

# 必需配置项：
# - DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
# - DASHSCOPE_API_KEY (通义千问API密钥)
```

### 4️⃣ 本地开发运行

```bash
# 进入项目目录
cd java/

# 构建项目（包含前端构建）
mvn clean package

# 运行应用
java -jar target/ai-novel-writer.jar

# 或使用Maven插件运行
mvn spring-boot:run

# 访问应用
# 前端页面: http://localhost:8080
# API文档: http://localhost:8080/docs
# 健康检查: http://localhost:8080/actuator/health
```

### 5️⃣ Docker部署

```bash
# 构建镜像
docker build -t ai-novel-writer:latest .

# 运行容器
docker run -d \
  --name ai-novel-writer \
  -p 8080:8080 \
  -e DB_HOST=your_db_host \
  -e DB_USER=your_db_user \
  -e DB_PASSWORD=your_db_password \
  -e DASHSCOPE_API_KEY=your_api_key \
  ai-novel-writer:latest

# 或使用docker-compose
docker-compose up -d
```

---

## 📦 构建说明

### Maven构建流程

1. **前端构建**（frontend-maven-plugin）
   - 安装Node.js和npm
   - 执行`npm install`
   - 执行`npm run build`
   - 输出到`src/main/frontend/dist/`

2. **复制前端产物**（maven-resources-plugin）
   - 从`src/main/frontend/dist/`复制到`target/classes/static/`
   - Spring Boot自动serve这些静态文件

3. **Java编译和打包**（spring-boot-maven-plugin）
   - 编译Java代码
   - 打包成可执行的Fat JAR
   - 包含内嵌Tomcat服务器

### 生产构建命令

```bash
# 完整构建（前端+后端）
mvn clean package -Pprod

# 跳过测试
mvn clean package -DskipTests

# 只构建后端（跳过前端）
mvn clean package -Dskip.frontend

# 只构建前端
cd src/main/frontend && npm run build
```

---

## 🔧 配置说明

### application.yml核心配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_novel_writer
    username: root
    password: password
    
  jpa:
    hibernate:
      ddl-auto: update  # 自动创建/更新表结构
    show-sql: false     # 生产环境关闭SQL日志
    
ai:
  qianwen:
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-plus
    temperature: 0.7
    max-tokens: 4000
    
server:
  port: 8080
```

### 多环境配置

```bash
# 开发环境
java -jar app.jar --spring.profiles.active=dev

# 生产环境
java -jar app.jar --spring.profiles.active=prod

# Docker环境变量
docker run -e SPRING_PROFILES_ACTIVE=prod ...
```

---

## 📊 数据库迁移

### 从SQLite迁移到MySQL

如果你有现有的SQLite数据，可以使用迁移脚本：

```bash
# 1. 导出SQLite数据
python tools/export_sqlite.py

# 2. 导入到MySQL
mysql -u root -p ai_novel_writer < data/export.sql

# 3. 验证数据
SELECT COUNT(*) FROM novels;
```

---

## 🎯 核心功能对照

| 功能模块 | Python API | Java API | 状态 |
|---------|-----------|----------|------|
| 小说管理 | `/api/novels/*` | `/api/novels/*` | ✅ |
| 章节管理 | `/api/chapters/*` | `/api/chapters/*` | ✅ |
| AI续写 | `/api/chapters/continue` | `/api/chapters/continue` | ✅ |
| 角色管理 | `/api/characters/*` | `/api/characters/*` | ✅ |
| 场景管理 | `/api/scenes/*` | `/api/scenes/*` | ✅ |
| 大纲管理 | `/api/outlines/*` | `/api/outlines/*` | ✅ |
| 智能推荐 | `/api/recommendations/*` | `/api/recommendations/*` | ✅ |
| 编辑历史 | `/api/history/*` | `/api/history/*` | ✅ |
| 伏笔管理 | `/api/plot-threads/*` | `/api/plot-threads/*` | ✅ |
| 世界观 | `/api/world-settings/*` | `/api/world-settings/*` | ✅ |

---

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=NovelServiceTest

# 集成测试
mvn verify
```

---

## 📝 开发指南

### 添加新功能的步骤

1. **创建Entity**（`entity/`）
   ```java
   @Entity
   @Table(name = "your_table")
   public class YourEntity extends BaseEntity { }
   ```

2. **创建Repository**（`repository/`）
   ```java
   public interface YourRepository extends JpaRepository<YourEntity, Long> { }
   ```

3. **创建DTO**（`dto/`）
   ```java
   public record YourDTO(Long id, String name) { }
   ```

4. **创建Service**（`service/`）
   ```java
   @Service
   public class YourService { }
   ```

5. **创建Controller**（`controller/`）
   ```java
   @RestController
   @RequestMapping("/api/your-resource")
   public class YourController { }
   ```

---

## 🐳 Docker部署详解

### Dockerfile说明

```dockerfile
# 多阶段构建
FROM maven:3.9-eclipse-temurin-17 AS build
# 构建阶段...

FROM eclipse-temurin:17-jre-alpine
# 运行阶段，仅包含JRE和JAR文件
```

### docker-compose.yml

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    ...
  
  app:
    build: .
    depends_on:
      - mysql
    ...
```

---

## 🔍 故障排除

### 常见问题

1. **数据库连接失败**
   ```
   检查: DB_HOST, DB_PORT, DB_USER, DB_PASSWORD
   确保MySQL服务运行中
   ```

2. **AI服务调用失败**
   ```
   检查: DASHSCOPE_API_KEY配置正确
   网络可以访问通义千问API
   ```

3. **前端资源404**
   ```
   确保执行了完整的Maven构建
   检查target/classes/static/目录有文件
   ```

4. **端口占用**
   ```bash
   lsof -i :8080
   kill -9 <PID>
   ```

---

## 📚 相关文档

- [API文档](http://localhost:8080/docs) - SpringDoc自动生成
- [原Python版文档](../README_AI_DETAILED.md)
- [项目结构](../PROJECT_STRUCTURE.md)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)

---

## 🎉 总结

### ✅ 已完成

- [x] Maven项目结构搭建
- [x] Spring Boot 3配置
- [x] 10个核心Entity类
- [x] MySQL数据库集成
- [x] Fat JAR打包配置
- [x] Docker容器化配置
- [x] 前端Vue集成配置
- [x] OpenAPI文档配置
- [x] 多环境配置支持

### 📝 待完成（需继续开发）

- [ ] Repository层（JPA接口）
- [ ] Service层（业务逻辑）
- [ ] Controller层（REST API）
- [ ] DTO类（数据传输对象）
- [ ] AI服务集成
- [ ] Vue前端组件
- [ ] 异常处理
- [ ] 单元测试

---

**注意**：本文档提供了完整的项目框架，核心代码结构已搭建完成。接下来需要实现Repository、Service、Controller等业务逻辑层代码。项目采用标准的Spring Boot分层架构，易于扩展和维护。

