[<title="NovelCraft 系统完整需求与设计方案（含 Vue3 前端）">]

# **NovelCraft — 长篇小说 AI 创作协作系统**
**完整需求规格 & 技术设计方案（含 Vue3 前端）**  
版本：1.0  
日期：2026年1月4日

---

## 一、系统目标

构建一个 **AI 辅助的长篇小说协同创作平台**，支持：
- 从零创作或导入已有小说；
- 自动建模写作风格、角色弧光、伏笔网络；
- 智能生成符合文学性、一致性、节奏控制的新章节；
- 提供专注、沉浸、低干扰的创作体验。

> **核心理念**：AI 是“协作者”，不是“替代者”。

---

## 二、整体架构

```
┌──────────────┐     REST API     ┌───────────────────────┐       ┌──────────────┐
│   Vue3 前端   │ ◄──────────────► │ Spring Boot 后端 (Java)│ ◄───► │ MySQL 数据库 │
└──────────────┘                  └───────────┬───────────┘       └──────────────┘
                                              │
                                              ▼
                                     ┌───────────────────────┐
                                     │ Alibaba Qwen-Max API  │
                                     └───────────────────────┘
```

---

## 三、后端需求与设计（简要）

### 3.1 核心功能
- 小说创建 / 导入（TXT/DOCX）
- 写作风格建模（自动提取 + 手动约束）
- 角色生命周期管理
- 伏笔（Plot Hook）系统（V2）
- 节奏控制（限制连续高潮）
- AI 章节生成（基于 Qwen-Max）

### 3.2 关键数据模型
| 实体 | 字段（关键） |
|------|-------------|
| `Novel` | id, title, outline, coreThemes |
| `WritingStyle` | novelId, avgSentenceLength, bannedWords, descriptionProfile |
| `Character` | name, coreBelief, currentArcStage, lastUpdatedChapter |
| `Chapter` | chapterNumber, content, sceneType, isHighStakes, summary |
| `PlotHook` | description, minChapter, maxChapter, status |

### 3.3 API 接口（草案）
- `POST /api/v1/novels` — 创建小说
- `POST /api/v1/novels/import` — 导入文件
- `POST /api/v1/novels/{id}/chapters?chapterNumber=N` — AI 生成第 N 章
- `GET /api/v1/novels/{id}` — 获取小说元信息
- `GET /api/v1/novels/{id}/characters` — 获取角色列表
- `GET /api/v1/novels/{id}/plot-hooks` — 获取伏笔列表（V2）

---

## 四、前端需求（Vue3）

### 4.1 技术栈
- **框架**：Vue 3 (Composition API)
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **UI 库**：Tailwind CSS + Headless UI
- **富文本编辑器**：Tiptap（支持 Markdown、自定义节点）
- **HTTP 客户端**：Axios
- **文件处理**：
   - `.txt`：原生 FileReader
   - `.docx`：`mammoth` 库解析
- **构建工具**：Vite

### 4.2 页面结构
```
src/
├── views/
│   ├── DashboardView.vue          → 小说仪表盘
│   ├── NovelWorkspaceView.vue     → 小说工作台（主界面）
│   ├── ChapterEditorView.vue      → 章节编辑器（嵌入在工作台中）
│   ├── CharactersView.vue         → 角色面板（V1 只读，V2 可编辑）
│   ├── PlotHooksView.vue          → 伏笔管理（V2）
│   └── OutlineEditorView.vue      → 大纲编辑（V2）
├── components/
│   ├── NovelCard.vue
│   ├── ChapterList.vue
│   ├── StyleProfileCard.vue
│   ├── PlotHookSuggestionBar.vue  → V2：伏笔提示条
│   └── AiGenerateButton.vue
├── stores/
│   ├── novelStore.js              → 小说状态
│   └── uiStore.js                 → 全局 UI 状态（如加载中）
└── router/
    └── index.js
```

---

## 五、V1 前端功能详述（MVP）

### 5.1 DashboardView（/dashboard）
- 显示小说卡片列表（标题、章节数、最后编辑时间）
- 操作按钮：
   - “+ 新建小说” → 弹出模态框（输入标题，可选大纲）
   - “↑ 导入小说” → 弹出文件上传框（支持 .txt/.docx）

### 5.2 NovelWorkspaceView（/novel/:id）
- **左侧边栏（Sidebar）**：
   - 小说标题（可编辑）
   - 章节列表（按编号展开，点击跳转）
   - 导航菜单：章节 / 角色 / 风格
- **主内容区**：
   - 动态加载 `ChapterEditorView` 或其他子页面

### 5.3 ChapterEditorView（核心）
- **顶部工具栏**：
   - 章节标题输入框
   - “AI 续写下一章” 按钮（调用生成 API）
   - “重写本章” 按钮
- **正文编辑区**：
   - Tiptap 富文本编辑器
   - 自动保存（防丢）
   - AI 生成内容显示灰色“AI 生成”标签
- **底部状态条**：
   - 字数统计
   - 节奏提示：“当前为铺垫章节” / “已连续 2 章高张力”

### 5.4 CharactersView（/novel/:id/characters）
- 表格展示角色信息（只读）：
   - 姓名、核心信念、当前弧光阶段、最后出现章节

### 5.5 StyleProfileCard（组件）
- 以卡片形式展示风格配置（只读）：
  ```text
  • 平均句长：28 字
  • 高频动词：凝视、颤抖、低语
  • 描写等级：高细节
  • 心理描写：行为暗示为主
  ```

### 5.6 导入流程
- 使用 `react-dropzone` 风格的拖拽上传区
- 提交后显示进度条 + 提示“正在分析小说结构...”
- 完成后自动跳转至新小说工作台

---

## 六、V2 前端增强功能

### 6.1 PlotHooksView（/novel/:id/plot-hooks）
- 伏笔列表（表格）：
   - 描述、状态（待触发 / 已触发 / 已回收）
   - 可展开章节区间
   - 操作：“标记已回收”、“编辑”
- “+ 添加伏笔” 按钮 → 弹出表单（描述 + min/max 章节）

### 6.2 OutlineEditorView（/novel/:id/outline）
- 树形大纲编辑器（支持折叠/展开）
- 每个节点可关联伏笔或角色

### 6.3 ChapterEditor 增强
- **伏笔提示条**（位于编辑器上方）：
  > 💡 本章可展开伏笔：**“怀表停在3:15”**（来自第 2 章）
- “AI 续写” 按钮下拉菜单：
   - 普通续写
   - 强制回收指定伏笔
   - 插入高张力场景

### 6.4 CharactersView 升级
- 支持手动编辑角色信念与阶段
- 显示角色出场热力图（柱状图）

---

## 七、UI/UX 设计原则

| 原则 | 实现方式 |
|------|--------|
| **沉浸写作** | 编辑器支持全屏模式（F11），隐藏侧边栏 |
| **AI 透明** | 所有 AI 内容带 badge，hover 显示生成 Prompt 摘要 |
| **零打断** | 自动保存 + 本地缓存，无确认弹窗 |
| **上下文可见** | 侧边栏始终显示角色/伏笔状态 |
| **响应式** | 桌面优先，移动端可读不可写 |

---

## 八、演进路线

| 版本 | 后端重点 | 前端重点 |
|------|--------|--------|
| **V1** | 小说管理、风格建模、基础生成 | 仪表盘、章节编辑器、角色/风格只读面板 |
| **V2** | 伏笔检测、大纲联动、节奏优化 | 伏笔管理、大纲编辑、角色可编辑、生成干预 |
| **V3** | 用户反馈学习风格微调 | 风格编辑器、A/B 生成对比 |
| **V4** | 多 POV 生成 | 视角切换器、角色专属章节 |

---

## 九、开发建议

1. **先做 V1 核心流**：
   - 创建小说 → 写第一章 → AI 续写第二章 → 查看角色
2. **Mock API 数据**：
   - 用 `msw`（Mock Service Worker）模拟后端，快速验证 UI
3. **富文本集成**：
   - Tiptap 支持自定义扩展，可未来加入“伏笔高亮”节点
4. **性能注意**：
   - 大小说（100+章）需虚拟滚动章节列表
   - 导入大文件时使用 Web Worker 避免卡死

---

> ✅ 此文档可直接用于 **前后端并行开发**。  
> 前端团队可基于此搭建 Vue3 项目骨架，后端团队实现 API。  
> 如需组件原型、Pinia store 结构或 Tiptap 配置示例，可进一步提供。

祝你开发顺利！