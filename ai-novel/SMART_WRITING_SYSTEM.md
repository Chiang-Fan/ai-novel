# 智能小说创作与续写系统

## 🎯 系统概述

这是一个基于AI的智能小说创作辅助系统，提供四大核心功能模块，帮助作者进行小说创作与续写。

## ✨ 核心功能模块

### 1️⃣ 正文输入模块
**功能描述**：
- 提供大文本输入框，支持粘贴和编辑小说片段
- 实时统计字数
- 最少50字符的输入验证
- 支持一键分析按钮

**使用方式**：
1. 在"正文输入"区域输入或粘贴小说片段
2. 至少输入50个字符
3. 点击"智能分析"按钮开始分析

### 2️⃣ 智能分析模块
**功能描述**：
- 自动识别主角
- 提取所有角色及其特征
- 分析当前场景（位置、时间、氛围）
- 检测写作风格
- 识别情节冲突和情感基调
- 提供完整性得分

**分析结果包含**：
```json
{
  "protagonist": "主角名称",
  "characters": [
    {
      "name": "角色名",
      "role": "主角/配角/龙套",
      "traits": "性格特征"
    }
  ],
  "scene": {
    "location": "地点",
    "time": "时间",
    "atmosphere": "氛围",
    "description": "场景描述"
  },
  "writingStyle": {
    "style": "风格类型",
    "description": "风格描述",
    "plotPace": "节奏"
  },
  "currentConflict": "当前冲突",
  "emotionalTone": "情感基调",
  "completenessScore": 85
}
```

**数据持久化**：
- 所有分析结果自动保存到数据库
- 支持查看历史分析记录
- 可随时调用历史数据保持故事连贯性

### 3️⃣ 续写模块
**功能描述**：
- AI智能推荐3-5个续写方向
- 每个方向包含详细信息：
  - 标题和描述
  - 可能的故事发展
  - 对整体故事的影响
  - 情节走向（上升/下降/转折）
  - 涉及角色
  - 难度系数（1-5）
  - 优先级（1-10）

**推荐示例**：
```json
{
  "title": "激烈对决",
  "description": "主角与敌军展开正面交锋",
  "storyDevelopment": "张明率领精锐部队突围...",
  "impact": "这将是故事的第一个高潮...",
  "plotDirection": "上升",
  "involvedCharacters": "张明,李勇,敌军统帅",
  "difficultyLevel": 4,
  "priority": 9
}
```

**交互功能**：
- 点击建议查看详情
- 支持采用建议进行续写
- 可生成更多建议
- 采用状态持久化记录

### 4️⃣ 侧边栏显示模块
**功能描述**：
- 实时展示当前小说状态
- 显示总章节数和总字数
- 展示分析历史记录
- 提供快捷操作按钮

**显示内容**：
- 当前状态（策划中/创作中/已完成）
- 统计数据（章节数、字数）
- 最近5条分析记录
- 快捷操作（快速分析、快速建议、一键操作）

## 🗄️ 数据库设计

### content_analysis 表
存储内容分析结果
```sql
CREATE TABLE content_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id BIGINT NOT NULL,
    chapter_id BIGINT,
    content_snippet TEXT NOT NULL,
    protagonist_name VARCHAR(100),
    extracted_characters TEXT,  -- JSON数组
    current_scene TEXT,
    scene_location VARCHAR(200),
    scene_time VARCHAR(100),
    scene_atmosphere VARCHAR(50),
    writing_style VARCHAR(100),
    style_description TEXT,
    plot_pace VARCHAR(50),
    current_conflict TEXT,
    potential_foreshadowing TEXT,  -- JSON数组
    character_relationships TEXT,  -- JSON数组
    emotional_tone VARCHAR(50),
    analysis_version VARCHAR(20),
    completeness_score INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_id (chapter_id)
);
```

### continuation_suggestions 表
存储续写方向推荐
```sql
CREATE TABLE continuation_suggestions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id BIGINT NOT NULL,
    analysis_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    story_development TEXT,
    impact TEXT,
    plot_direction VARCHAR(50),
    involved_characters VARCHAR(500),
    expected_word_count INT,
    difficulty_level INT,
    priority INT NOT NULL DEFAULT 5,
    is_adopted BOOLEAN DEFAULT FALSE,
    generated_chapter_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_novel_id (novel_id),
    INDEX idx_analysis_id (analysis_id)
);
```

## 📡 API接口文档

### 内容分析API

#### 1. 分析内容
```
POST /api/smart-writing/analyze
```
**请求体**：
```json
{
  "novelId": 1,
  "chapterId": null,
  "content": "小说片段内容...",
  "deepAnalysis": true
}
```

**响应**：
```json
{
  "code": 200,
  "message": "分析完成",
  "data": {
    "id": 1,
    "protagonistName": "张明",
    "extractedCharacters": [...],
    "sceneInfo": {...},
    "styleInfo": {...},
    "completenessScore": 85
  }
}
```

#### 2. 获取最新分析
```
GET /api/smart-writing/analysis/novel/{novelId}/latest
```

#### 3. 获取分析历史
```
GET /api/smart-writing/analysis/novel/{novelId}/history
```

### 续写建议API

#### 1. 生成续写建议
```
POST /api/smart-writing/suggestions/generate
```
**请求体**：
```json
{
  "novelId": 1,
  "analysisId": 1,
  "count": 3,
  "expectedWordCount": 2000
}
```

**响应**：
```json
{
  "code": 200,
  "message": "生成成功",
  "data": [
    {
      "id": 1,
      "title": "激烈对决",
      "description": "...",
      "storyDevelopment": "...",
      "impact": "...",
      "priority": 9
    }
  ]
}
```

#### 2. 获取未采用建议
```
GET /api/smart-writing/suggestions/novel/{novelId}/unadopted
```

#### 3. 采用建议
```
PUT /api/smart-writing/suggestions/{id}/adopt?chapterId=123
```

### 组合API

#### 一键分析并生成建议
```
POST /api/smart-writing/analyze-and-suggest
```
**请求体**：
```json
{
  "novelId": 1,
  "content": "小说片段...",
  "suggestionCount": 3
}
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "analysis": {...},
    "suggestions": [...]
  }
}
```

## 🚀 使用流程

### 标准工作流程：

1. **输入内容**
   - 在正文输入框中输入或粘贴小说片段
   - 确保至少50个字符

2. **智能分析**
   - 点击"智能分析"按钮
   - AI自动提取角色、场景、风格等信息
   - 分析结果显示在中间栏

3. **查看建议**
   - 系统自动生成3个续写方向建议
   - 点击建议查看详细的故事发展和影响分析
   - 可点击"生成更多"获取额外建议

4. **选择续写**
   - 点击感兴趣的建议查看详情
   - 点击"采用此方向续写"开始创作
   - 系统会跳转到续写页面

5. **历史追溯**
   - 右侧面板显示所有历史分析记录
   - 点击历史记录可查看之前的分析结果
   - 确保多次续写时保持故事连贯性

### 快捷操作：

- **快速分析**：一键执行内容分析
- **快速建议**：基于当前分析生成新建议
- **一键操作**：同时执行分析和建议生成

## 🎨 前端页面结构

```
智能创作工作台 (SmartWriting.vue)
├── 头部区域
│   ├── 标题和说明
│   └── 小说信息卡片
│
├── 主体区域（三栏布局）
│   ├── 左栏：正文输入模块 (50%)
│   │   ├── 文本输入框
│   │   ├── 字数统计
│   │   ├── 智能分析按钮
│   │   └── 续写建议列表
│   │
│   ├── 中栏：智能分析结果 (25%)
│   │   ├── 完整性得分
│   │   ├── 主角信息
│   │   ├── 角色列表
│   │   ├── 场景信息
│   │   ├── 写作风格
│   │   ├── 当前冲突
│   │   └── 情感基调
│   │
│   └── 右栏：状态面板 (25%)
│       ├── 实时状态卡片
│       ├── 统计数据
│       ├── 分析历史
│       └── 快捷操作
```

## 🔧 技术实现

### 后端技术栈
- Spring Boot 3.2.1
- Spring Data JPA
- MySQL 8.x
- 通义千问AI API

### 前端技术栈
- Vue 3 (Composition API)
- Vue Router
- Axios
- Tailwind CSS

### AI集成
- 使用阿里云通义千问API
- 结构化提示词工程
- JSON格式响应解析
- 上下文管理

## 📊 AI提示词设计

### 内容分析提示词
```
你是一个专业的小说分析专家，擅长从文本中提取结构化信息。

任务：
1. 识别主角
2. 提取角色信息
3. 分析场景
4. 检测写作风格
5. 情节分析

返回JSON格式结果...
```

### 续写建议提示词
```
你是一个经验丰富的小说策划专家，擅长为故事提供多样化的发展方向。

任务：
为当前故事提供N个续写方向建议，每个建议包含：
- 标题
- 描述
- 故事发展
- 影响分析
- 情节走向
- 涉及角色
- 难度和优先级

返回JSON数组格式...
```

## 🔐 数据持久化策略

### 分析结果持久化
- 每次分析自动保存到`content_analysis`表
- 关联小说ID和章节ID
- 保留完整的分析历史
- 支持按时间倒序查询

### 续写建议持久化
- 每次生成的建议保存到`continuation_suggestions`表
- 关联分析ID追踪来源
- 记录采用状态
- 支持按优先级排序

### 历史数据调用
- 查询最近N条分析记录
- 加载历史分析详情
- 获取未采用的建议
- 追踪已采用建议的章节

## 🎯 故事连贯性保证

1. **上下文传递**
   - 每次分析都保存完整的场景和角色信息
   - 续写时可调用历史分析数据
   - 保持角色特征一致性

2. **风格延续**
   - 记录已识别的写作风格
   - 续写建议考虑现有风格
   - 保持情节节奏连贯

3. **情节连续**
   - 追踪当前冲突状态
   - 记录潜在伏笔
   - 建议考虑故事整体发展

## 📱 响应式设计

- 支持桌面端（最佳体验）
- 平板适配（三栏转两栏）
- 移动端支持（单栏纵向布局）

## 🔄 工作流程示例

```
用户输入内容
    ↓
点击"智能分析"
    ↓
AI分析内容
    ↓
保存分析结果到数据库
    ↓
自动生成续写建议
    ↓
保存建议到数据库
    ↓
用户查看并选择建议
    ↓
采用建议进行续写
    ↓
更新建议采用状态
    ↓
生成的章节关联到建议
```

## 🎬 使用示例

### 场景一：新手作者
1. 输入自己写的开头片段
2. 查看AI分析的角色和场景
3. 从多个续写方向中选择一个
4. 基于建议继续创作

### 场景二：遇到瓶颈
1. 粘贴当前卡住的段落
2. 分析当前故事状态
3. 获取突破性的情节建议
4. 选择合适方向继续写作

### 场景三：保持连贯性
1. 查看历史分析记录
2. 回顾之前的角色设定
3. 确认场景和风格一致
4. 基于历史数据继续创作

## 🛠️ 部署说明

### 数据库初始化
系统使用JPA自动建表，启动时会自动创建`content_analysis`和`continuation_suggestions`两张新表。

### 环境变量
确保配置了以下环境变量：
```bash
SPRING_AI_OPENAI_API_KEY=your-api-key
SPRING_AI_OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ai_novel_writer
DB_USER=root
DB_PASSWORD=your-password
```

### 启动应用
```bash
cd java
./start-java.sh
```

访问：`http://localhost:8080/novel/{id}/smart-writing`

## 📈 性能优化

- AI调用采用异步处理
- 分析结果缓存机制
- 历史记录分页加载
- 前端响应式加载

## 🔮 未来扩展

- [ ] 批量分析多个章节
- [ ] 角色关系图可视化
- [ ] 情节线追踪
- [ ] 写作风格模板
- [ ] 多人协作创作
- [ ] 导出分析报告

## 📞 技术支持

如有问题，请查看：
- API文档：http://localhost:8080/docs
- 系统日志：java/app.log

---

**系统版本**: v2.1.0  
**最后更新**: 2025-12-29
