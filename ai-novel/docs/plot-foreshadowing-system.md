# 伏笔管理系统 (Plot Foreshadowing System)

## 系统概述

伏笔管理系统是一个专门用于管理和跟踪小说中伏笔、线索和未来方向的系统。它与原有的伏笔系统（Plot Hook）一起，构成了四维风格画像中的第四维——伏笔画像，为AI续写提供完整的情节连贯性支持。

## 核心功能

### 1. 伏笔生命周期管理
- **种植（Planting）**：在特定章节植入伏笔
- **触发（Triggering）**：当情节发展到相关节点时触发伏笔
- **解决（Resolving）**：当情节解决伏笔时标记为已解决
- **追踪（Tracking）**：全程追踪伏笔状态变化

### 2. 状态管理
- `PENDING`：待解决，表示伏笔已种植但尚未触发
- `TRIGGERED`：已触发，表示伏笔已在某个章节被触发
- `RESOLVED`：已解决，表示伏笔已被完全解决

### 3. 优先级与类型
- **优先级**：1-10级，10为最高优先级
- **类型**：
  - `EXPLICIT`：明确的伏笔
  - `IMPLICIT`：隐含的伏笔
  - `CHEKHOV_GUN`：契诃夫之枪类型的伏笔

## 数据模型

### PlotForeshadowing 实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 伏笔唯一标识 |
| novelId | Long | 所属小说ID |
| chapterId | Long | 关联章节ID |
| plantedInChapter | Integer | 种植章节号 |
| expectedChapter | Integer | 预期解决章节号 |
| triggeredInChapter | Integer | 实际触发章节号 |
| resolvedInChapter | Integer | 实际解决章节号 |
| title | String | 伏笔标题 |
| description | String | 伏笔详细描述 |
| type | String | 伏笔类型 |
| priority | Integer | 优先级（1-10） |
| status | String | 当前状态 |
| isAutoDetected | Boolean | 是否为自动检测 |
| contentReference | String | 内容引用/原文片段 |
| notes | String | 备注信息 |
| resolutionNote | String | 解决说明 |

## REST API 接口

### 获取小说的所有伏笔
```
GET /api/plot-foreshadowing/novel/{novelId}
```

### 获取特定章节的伏笔
```
GET /api/plot-foreshadowing/chapter/{chapterId}
```

### 创建新伏笔
```
POST /api/plot-foreshadowing
Content-Type: application/json

{
  "novelId": 1,
  "chapterId": 1,
  "plantedInChapter": 1,
  "title": "伏笔标题",
  "description": "伏笔描述",
  "type": "EXPLICIT",
  "priority": 8,
  "expectedChapter": 5
}
```

### 更新伏笔
```
PUT /api/plot-foreshadowing/{id}
Content-Type: application/json
```

### 删除伏笔
```
DELETE /api/plot-foreshadowing/{id}
```

### 标记伏笔为已触发
```
POST /api/plot-foreshadowing/{id}/trigger?chapterNumber=3
```

### 标记伏笔为已解决
```
POST /api/plot-foreshadowing/{id}/resolve?chapterNumber=5&resolutionNote=解决说明
```

## 与AI续写集成

伏笔管理系统与以下组件深度集成：

### 1. NovelWritingStyleService
- 在四维风格画像的第四维（伏笔画像）中整合新旧两套伏笔系统
- 提供完整的伏笔信息给AI续写引擎

### 2. PromptBuilderService
- 在构建AI续写提示词时包含完整的伏笔信息
- 确保AI续写内容与已有伏笔保持一致
- 引导AI在续写中自然触发和解决伏笔

## 使用场景

### 1. 手动管理伏笔
作者可以在创作过程中手动添加、编辑和管理伏笔，确保情节的连贯性和逻辑性。

### 2. 自动检测伏笔
系统可以分析已有内容，自动检测潜在的伏笔并标记为待处理状态。

### 3. 智能续写建议
AI续写引擎根据当前的伏笔状态，提供符合情节发展的续写内容。

### 4. 一致性检查
系统会检查续写内容是否与现有伏笔保持一致，避免出现逻辑冲突。

## 系统优势

1. **完整性**：与原有伏笔系统并行工作，确保四维风格画像的完整性
2. **灵活性**：支持多种伏笔类型和复杂的生命周期管理
3. **可追溯性**：完整记录伏笔从种植到解决的全过程
4. **智能化**：与AI续写引擎深度集成，提升续写质量
5. **可视化**：通过API接口支持前端可视化展示和管理