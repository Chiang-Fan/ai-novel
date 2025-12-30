# AI小说创作系统 - 快速开始指南

## 项目结构

```
ai-write-agent/
├── ai-novel/                              # 主项目目录
│   ├── pom.xml                           # Maven 配置
│   ├── src/main/java/com/aiwriter/
│   │   ├── controller/                   # REST 控制器
│   │   ├── service/                      # 业务服务层
│   │   │   └── ai/                       # AI 相关服务
│   │   ├── repository/                   # 数据访问层
│   │   ├── entity/                       # JPA 实体
│   │   ├── dto/                          # 数据传输对象
│   │   └── common/                       # 通用工具
│   ├── src/main/resources/
│   │   ├── application.yml               # Spring 配置
│   │   └── db/migration/                 # Flyway 迁移脚本
│   └── src/main/frontend/                # Vue 前端项目
│       └── src/
│           ├── views/                    # 页面组件
│           ├── components/               # 复用组件
│           └── styles/                   # 样式文件
└── docs/                                  # 开发文档
    ├── Phase1_*.md
    ├── Phase2_*.md
    └── Phase3_*.md
```

## 环境要求

### 后端
- Java 17+
- Maven 3.8+
- Spring Boot 3.2.1
- MySQL 8.0+

### 前端
- Node.js 16+
- npm 8+ 或 yarn

### 外部服务
- Alibaba DashScope API（用于AI功能）

## 本地开发

### 1. 后端设置

```bash
# 进入项目目录
cd ai-novel

# 构建项目
mvn clean install

# 启动应用（开发环境）
mvn spring-boot:run
```

**默认端口**: `http://localhost:8080`

**数据库初始化**:
- 系统自动执行 Flyway 迁移脚本
- 首次启动时会创建所有必要的表

### 2. 前端设置

```bash
# 进入前端目录
cd ai-novel/src/main/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**默认地址**: `http://localhost:5173`

### 3. 配置文件

**后端配置** (`application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_novel?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

# Alibaba DashScope 配置
dashscope:
  api-key: ${DASHSCOPE_API_KEY}
  model: qwen-plus
```

**前端配置** (`.env`):
```
VITE_API_URL=http://localhost:8080
```

## 核心 API 使用

### 1. 小说创建 (Phase 1)

```bash
# 创建小说并获取推荐
POST /api/novels
{
  "title": "我的仙侠小说",
  "description": "一个少年的修仙之旅",
  "genre": "xianxia",
  "initialOutlineId": 1,
  "initialSceneId": 2,
  "useAiRecommendation": true
}

# 获取创意推荐
POST /api/novels/recommendations
{
  "title": "我的仙侠小说",
  "description": "一个少年的修仙之旅",
  "genre": "xianxia"
}
```

### 2. 世界观设定 (Phase 2)

```bash
# 创建世界观
POST /api/world-settings
{
  "novelId": 1,
  "name": "东方仙侠世界",
  "description": "融合了传统仙侠元素的东方幻想世界",
  "cosmicBackground": "天地灵气充沛，修炼成仙成圣可期"
}

# 添加地理位置
POST /api/world-settings/geography
{
  "worldSettingId": 1,
  "name": "青云山",
  "geographyType": "山脉",
  "terrainType": "高耸入云",
  "climate": "常年云雾缭绕"
}

# 添加种族
POST /api/world-settings/race
{
  "worldSettingId": 1,
  "name": "人类",
  "raceCategory": "主要种族",
  "abilities": "修仙资质强"
}

# 添加魔法系统
POST /api/world-settings/magic-system
{
  "worldSettingId": 1,
  "name": "修仙体系",
  "systemType": "修仙",
  "coreRules": "吸收灵气，修炼成仙"
}

# 验证世界观一致性
GET /api/world-settings/1/validate
```

### 3. 文本导入 (Phase 3)

```bash
# 上传文本文件
POST /api/text-imports/upload
Content-Type: multipart/form-data
- novelId: 1
- file: your_novel.txt

# 获取导入的章节
GET /api/text-imports/123/chapters

# 提取关键要素
POST /api/text-imports/123/extract-elements

# 确认导入
POST /api/text-imports/123/confirm
```

## 主要功能使用流程

### 流程 1: 从零开始创建小说

```
1. 创建小说
   → POST /api/novels
   → 获取 novel_id

2. 创建世界观
   → POST /api/world-settings
   → 定义地理、时代、种族、魔法系统

3. 添加大纲和场景
   → 在 UI 中操作

4. 开始创作
   → 使用 AI 续写功能（Phase 4）
```

### 流程 2: 导入已有文本

```
1. 上传文本文件
   → POST /api/text-imports/upload
   → 系统自动分章和提取要素

2. 预览导入结果
   → 检查识别的章节
   → 验证提取的地点、人物、事件

3. 映射到世界观
   → 关联提取的地点到地理设定
   → 关联人物到种族系统

4. 确认导入
   → POST /api/text-imports/123/confirm
```

## 数据库查询常用命令

```sql
-- 查看所有小说
SELECT * FROM novels;

-- 查看小说的世界观
SELECT * FROM world_settings WHERE novel_id = 1;

-- 查看世界观的所有种族
SELECT * FROM race_settings WHERE world_setting_id = 1;

-- 查看导入的章节
SELECT * FROM imported_chapters WHERE text_import_id = 1 ORDER BY chapter_number;

-- 查看导入记录的统计信息
SELECT status, COUNT(*) as count FROM text_imports GROUP BY status;
```

## 常见问题

### Q: 如何获取 Alibaba DashScope API Key？

A: 访问 https://dashscope.aliyun.com，注册账号后在控制台获取 API Key，然后设置环境变量：
```bash
export DASHSCOPE_API_KEY=your_api_key
```

### Q: 如何修改默认的分章字数（3000）？

A: 编辑 `TextImportService.java`，修改 `wordsPerChapter` 变量：
```java
private static final int WORDS_PER_CHAPTER = 3000; // 修改这个值
```

### Q: 前端如何自动重定向登录？

A: 编辑前端的路由保护中间件（下一阶段实现）

### Q: 如何导出小说内容？

A: 该功能在 Phase 4-6 中规划实现

## 开发技巧

### 1. 快速测试 API

使用 REST Client 插件（VS Code）或 Postman：

```http
### 获取所有小说
GET http://localhost:8080/api/novels

### 创建小说
POST http://localhost:8080/api/novels
Content-Type: application/json

{
  "title": "新小说",
  "description": "描述",
  "genre": "xianxia",
  "initialOutlineId": 1,
  "initialSceneId": 1
}
```

### 2. 查看 SQL 日志

在 `application.yml` 中启用：
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### 3. 热重载前端代码

Vite 已配置热模块替换，保存文件时自动刷新浏览器

### 4. 调试后端代码

在 IDE 中设置断点，然后运行：
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

## 部署准备

### 生产环境检查清单

- [ ] 更改默认密码和 API Key
- [ ] 配置数据库备份策略
- [ ] 启用 HTTPS
- [ ] 配置负载均衡
- [ ] 设置监控和告警
- [ ] 优化数据库查询性能
- [ ] 压缩和缓存前端资源
- [ ] 配置 CORS（如需）

### Docker 部署示例

```dockerfile
# 后端镜像
FROM openjdk:17-jdk-slim
COPY target/ai-novel.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# 前端镜像
FROM node:16-alpine
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
RUN npm run build
EXPOSE 5173
CMD ["npm", "run", "preview"]
```

## 更多资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 3 官方文档](https://vuejs.org/)
- [Alibaba DashScope 文档](https://dashscope.aliyun.com/api-detail)
- [Tailwind CSS 文档](https://tailwindcss.com/)

## 获取帮助

- 查看 `docs/` 目录中的详细设计文档
- 检查代码注释和 Javadoc
- 查看测试类了解使用示例

---

**最后更新**: 2025年12月30日

**当前版本**: v0.3.0 (Phase 1-3 完成)
