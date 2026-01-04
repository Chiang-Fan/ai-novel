# NovelCraft - AI 长篇小说创作协作系统

基于 Spring Boot 和阿里云通义千问 API 的 AI 驱动小说创作平台，支持从零开始或基于已有文本进行长篇小说创作。

## 项目特性

- 📚 小说管理：创建、编辑、删除小说
- ✍️ AI 章节生成：基于上下文和风格约束的智能生成
- 👥 角色生命周期管理：跟踪角色发展弧线
- 🔗 伏笔系统：自动管理故事线索
- 🎨 写作风格建模：保持文学风格一致性
- 📐 节奏控制：管理故事张力和节奏
- 🎯 **新增：带方向的AI续写**：作者可提供具体续写方向，使生成内容更符合预期

## 技术栈

- **后端**: Spring Boot 3.2+, Java 17+
- **AI SDK**: Spring AI Alibaba (兼容 OpenAI API 的通义千问)
- **数据库**: MySQL 8.0+
- **前端**: React + Vite
- **构建工具**: Maven

## 带方向的AI续写功能

根据 `project_add_ai.md` 文档，我们实现了"带方向的 AI 续写"功能，允许作者在AI生成前提供具体的续写方向：

### 前端交互
- 在章节编辑器中添加"AI续写(带方向)..."按钮
- 弹出续写方向模态框，包含以下字段：
  - 续写方向（必填）：自然语言描述本章内容
  - 禁止事项（可选）：逗号分隔的禁止元素
  - 情绪基调（可选）：选择章节情绪氛围
  - 是否高重要场景：手动覆盖系统建议

### 后端处理
- 新增 API 端点：`POST /api/v1/novels/{novelId}/chapters/directed`
- 扩展 [Chapter](file:///Users/jiangfan/workspace/ai-project/ai-write-agent/src/main/java/com/ainovel/novelcraft/entity/Chapter.java#L8-L68) 实体，添加方向相关信息存储
- 增强 [PromptBuilderService](file:///Users/jiangfan/workspace/ai-project/ai-write-agent/src/main/java/com/ainovel/novelcraft/service/PromptBuilderService.java#L26-L273) 以支持在提示词中加入作者方向
- 优先级规则：作者方向 > 节奏自动控制 > 风格约束

## 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+ (前端)
- MySQL 8.0+
- 阿里云 DashScope API Key

## 快速开始

### 1. 数据库设置

创建数据库:
```sql
CREATE DATABASE novelcraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 环境变量配置

```bash
export AI_DASHSCOPE_API_KEY="your_dashscope_api_key"
export DASHSCOPE_BASE_URL="https://dashscope.aliyuncs.com/compatible-mode/v1"
```

### 3. 启动后端服务

```bash
# 设置 JAVA_HOME 指向 JDK 17
export JAVA_HOME=/path/to/jdk17

# 启动应用
mvn spring-boot:run
```

### 4. 启动前端 (开发模式)

```bash
cd frontend
npm install
npm run dev
```

## API 接口

### 小说管理

- `GET /api/v1/novels` - 获取所有小说
- `GET /api/v1/novels/{id}` - 获取指定小说
- `POST /api/v1/novels` - 创建新小说
- `PUT /api/v1/novels/{id}` - 更新小说
- `DELETE /api/v1/novels/{id}` - 删除小说

### 章节管理

- `GET /api/v1/novels/{novelId}/chapters` - 获取小说所有章节
- `GET /api/v1/novels/{novelId}/chapters/{chapterNumber}` - 获取指定章节
- `POST /api/v1/novels/{novelId}/chapters?chapterNumber={n}` - 生成新章节
- `POST /api/v1/novels/{novelId}/chapters/directed` - **新增：带方向生成新章节**

### 小说导入

- `POST /api/v1/novels/import` - 导入 TXT/DOCX 文件为小说

## 核心功能

### AI 章节生成

系统通过建模以下维度来生成符合要求的章节：

1. **写作风格约束**：句长、用词习惯、叙事视角等
2. **角色一致性**：确保角色行为符合其发展弧线
3. **伏笔管理**：自动展开待触发的伏笔
4. **节奏控制**：避免连续高张力场景
5. **作者方向**：**新增：根据作者提供的具体方向生成**

### 风格建模

系统自动从初始内容中提取或允许用户定义以下风格维度：

- 平均句长
- 高频动词列表
- 禁用词列表
- 叙事视角（沉浸式/疏离式）
- 道德立场（中立/批判/同情）
- 幽默风格（无/讽刺/荒诞）
- 描写配置
- 自定义规则

## 项目结构

```
src/main/java/com/ainovel/novelcraft/
├── controller/          # API 控制器
├── service/             # 业务逻辑服务
├── entity/              # JPA 实体
├── repository/          # 数据访问层
├── dto/                 # 数据传输对象
├── config/              # 配置类
└── NovelCraftApplication.java  # 主应用类

frontend/
├── src/
│   ├── components/      # React 组件
│   ├── App.jsx          # 主应用组件
│   └── main.jsx         # 应用入口
├── package.json         # 前端依赖
└── vite.config.js       # Vite 配置
```

## 开发计划

### V1 (当前)
- 支持创建、导入、风格保持、基础生成
- **新增：带方向的AI续写功能**

### V2
- 伏笔自动检测与回收建议

### V3
- 用户修改反馈 → 风格微调

### V4
- 多角色 POV 切换生成

## 贡献

欢迎提交 Issue 和 Pull Request 来改进项目。

## 许可证

MIT