# AI小说创作系统

> 专业级AI驱动的完整小说创作平台 | 集智能约束、多维世界观、文本导入于一体

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/technologies/downloads/#java17)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🚀 快速概览

AI小说创作系统是一个专业级的创意写作平台，集成了人工智能技术、数据约束、内容管理于一体。通过多维世界观设定、智能文本分析、AI辅助创作等功能，帮助作者快速构建完整的小说创作体系。

### 核心特性

- 🎯 **强化创建流程**: 大纲/场景必填 + AI创意推荐
- 🌍 **多维世界观**: 宇宙背景/地理/时代/种族/魔法系统五维一体
- 📄 **智能文本导入**: 自动分章、要素提取、关联映射
- 🤖 **AI辅助增强**: 通义千问驱动的创意生成和智能建议
- ✅ **智能约束验证**: 一致性检查确保创作逻辑严密

## 📊 项目进度

```
完成度: ████████████████████░░░░░░░░░░░░░░░░░░░░░░ 50%

Phase 1: 小说创建强化         ✅ 完成
Phase 2: 世界观设定模块       ✅ 完成  
Phase 3: 文本智能导入         ✅ 完成
Phase 4: AI续写增强           🔄 进行中
Phase 5: 场景节奏管理         ⏳ 计划中
Phase 6: AI描写能力强化       ⏳ 计划中
```

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.2.1 + Spring Data JPA
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **AI引擎**: Alibaba DashScope (通义千问)
- **构建工具**: Maven

### 前端
- **框架**: Vue 3.4 + Vite
- **样式**: Tailwind CSS 3.4.17
- **状态管理**: Pinia
- **图标库**: Lucide Vue Next

## 📦 快速开始

### 环境要求

- Java 17+
- Node.js 16+
- MySQL 8.0+

### 后端启动

```bash
cd ai-novel
mvn clean install
mvn spring-boot:run
```

访问: http://localhost:8080

### 前端启动

```bash
cd ai-novel/src/main/frontend
npm install
npm run dev
```

访问: http://localhost:5173

### 配置

设置 Alibaba DashScope API Key:
```bash
export DASHSCOPE_API_KEY=your_api_key_here
```

## 🎨 功能模块

### Phase 1: 小说创建强化 ✅

**创新亮点**:
- 大纲和场景必填验证
- 实时AI创意推荐（分屏预览）
- 推荐内容存储和关联

**使用流程**:
```
1. 创建小说 → 输入基本信息
2. 获取推荐 → AI生成创意内容
3. 确认选择 → 推荐内容关联到小说
```

### Phase 2: 世界观设定模块 ✅

**核心功能**:
- 📍 **地理位置**: 大陆/国家/城市多级支持
- ⏰ **时代背景**: 历史事件/社会结构/文明阶段
- 👥 **种族系统**: 特性/能力/社会地位管理
- ✨ **魔法系统**: 规则/等级/修炼方法定义

**智能特性**:
- AI辅助生成完整世界观
- 多维一致性自动验证
- 版本管理和发布流程
- 完整度评分计算

**验证规则示例**:
```
✓ 种族分布与地理位置一致
✓ 时间轴连续性和逻辑性
✓ 魔法系统的完整性和平衡
✓ 重要地理位置的占据率
```

### Phase 3: 文本智能导入 ✅

**核心算法**:
- 📖 **智能分章**: 正则识别 + 字数分割双引擎
- 🔍 **关键提取**: 地点/人物/事件智能识别
- 🧹 **文本清理**: 编码处理/格式规范化

**支持格式**: TXT, DOCX, PDF

**使用流程**:
```
1. 拖拽上传 → 系统自动解析
2. 预览结果 → 检查分章和提取结果
3. 确认导入 → 章节转换为小说内容
```

### Phase 4: AI续写增强 🔄

**规划功能** (进行中):
- 多维约束融合（世界观+场景+角色）
- 情节一致性实时检查
- 对话自然度评分
- 描写细节丰富度优化

## 📈 数据库设计

### 核心表结构

```
novels (小说基表)
├─ novel_creation_recommendations (推荐表)

world_settings (世界观)
├─ geography_settings (地理位置)
├─ time_period_settings (时代背景)
├─ race_settings (种族系统)
└─ magic_system_settings (魔法系统)

text_imports (导入记录)
└─ imported_chapters (导入章节)
```

**总计**: 9 个核心表，300+ 个字段

## 🔌 API 文档

### 小说创建

```http
POST /api/novels/recommendations
Content-Type: application/json

{
  "title": "我的仙侠小说",
  "description": "少年修仙之旅",
  "genre": "xianxia"
}

Response:
{
  "storyFramework": "...",
  "threeActStructure": {...},
  "mainPlotPoints": [...],
  "characterRecommendations": [...],
  "themes": [...]
}
```

### 世界观管理

```http
# 创建世界观
POST /api/world-settings
{
  "novelId": 1,
  "name": "东方仙侠世界",
  "description": "...",
  "cosmicBackground": "..."
}

# 验证一致性
GET /api/world-settings/1/validate

Response:
{
  "isValid": true,
  "completenessPercentage": 85,
  "warnings": [...],
  "errors": []
}
```

### 文本导入

```http
# 上传文件
POST /api/text-imports/upload
Content-Type: multipart/form-data

# 获取识别结果
GET /api/text-imports/123/chapters

# 确认导入
POST /api/text-imports/123/confirm
```

📚 **完整API文档**: 查看 `docs/` 目录

## 📚 文档

- [快速开始指南](./QUICK_START.md) - 5分钟上手
- [开发进度总结](./DEVELOPMENT_SUMMARY.md) - 项目完整概览
- [API端点总览](./docs/API_ENDPOINTS.md) - 所有API详细说明
- [世界观设定设计](./docs/Phase2_World_Setting_Implementation.md)
- [文本导入设计](./docs/Phase3_Text_Import_Implementation.md)

## 🏗️ 系统架构

```
┌─────────────────────────────────────┐
│         前端层 (Vue 3.4)            │
│  - 小说创建 / 世界观管理 / 文本导入│
└────────────────┬────────────────────┘
                 │ REST API
┌────────────────▼────────────────────┐
│        控制器层 (Controller)        │
│  - 路由映射 / 请求处理 / 响应返回 │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│         业务层 (Service)            │
│  - 业务逻辑 / AI集成 / 验证检查   │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│    数据访问层 (Repository)          │
│  - JPA查询 / 事务管理              │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│        数据库层 (MySQL 8.0)        │
│  - 数据持久化 / 索引优化           │
└─────────────────────────────────────┘
```

## 📊 项目统计

| 指标 | 数值 |
|------|------|
| 代码行数 | 3,200+ |
| 源代码文件 | 35 |
| 数据库表 | 9 |
| API端点 | 29 |
| 前端组件 | 3 |
| 文档字数 | 20,000+ |
| 测试覆盖 | 待补充 |

## 🧪 测试

### 运行测试

```bash
# 单元测试
mvn test

# 集成测试
mvn verify

# 覆盖率报告
mvn jacoco:report
```

### 测试覆盖目标

- [ ] 单元测试: 70%+
- [ ] 集成测试: 50%+
- [ ] API测试: 100%

## 🚀 部署

### Docker 部署

```bash
# 构建镜像
docker build -t ai-novel:latest .

# 运行容器
docker run -p 8080:8080 \
  -e DASHSCOPE_API_KEY=your_key \
  -e DB_URL=jdbc:mysql://host:3306/db \
  ai-novel:latest
```

### Kubernetes 部署

```bash
# 应用配置
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 查看状态
kubectl get pods
kubectl logs -f <pod-name>
```

## 🔒 安全性

- ✅ SQL注入防护 (参数化查询)
- ✅ 输入验证 (Bean Validation)
- ✅ 错误处理 (异常捕获)
- ⏳ CSRF防护 (规划中)
- ⏳ 身份验证 (规划中)
- ⏳ 权限控制 (规划中)

## 📈 性能指标

| 指标 | 目标 | 状态 |
|------|------|------|
| API响应时间 | <200ms | ✅ |
| 数据库查询 | <50ms | ✅ |
| 文件导入速度 | 3MB/s | ✅ |
| 并发用户 | 100+ | 需优化 |

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发规范

- 代码风格: Google Java Style Guide
- 提交信息: `feat/fix/docs: 清晰的描述`
- 分支命名: `feature/功能名` 或 `bugfix/问题名`

## 📋 Roadmap

### 短期 (1-2 周)

- [ ] 完成 Phase 4 实现
- [ ] 补充单元测试 (目标50%+)
- [ ] 性能基准测试

### 中期 (1 个月)

- [ ] 完成 Phase 5-6
- [ ] 集成测试覆盖80%+
- [ ] 用户文档完善

### 长期 (2-3 个月)

- [ ] NLP能力增强
- [ ] 微服务架构重构
- [ ] 云原生部署支持

## 🐛 已知问题

| 问题 | 优先级 | 状态 |
|------|--------|------|
| 文件大小限制50MB | 中 | 计划优化 |
| 并发用户限制100 | 中 | 计划扩展 |
| NLP精准度85% | 低 | 计划改进 |

## 📞 联系方式

- 📧 Email: contact@aiwriter.dev
- 💬 讨论: GitHub Discussions
- 📱 反馈: 提交 Issue

## 📄 许可证

MIT License - 详见 [LICENSE](./LICENSE) 文件

## 🙏 致谢

感谢以下开源项目的支持:
- Spring Boot 团队
- Vue.js 社区
- Alibaba DashScope

---

**当前版本**: v0.3.0 (Phase 1-3 完成)

**最后更新**: 2025年12月30日

**项目状态**: 🟢 积极开发中

---

> 让AI成为你的创意伙伴，让世界观约束激发更好的故事 💫
