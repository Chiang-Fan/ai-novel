# AI Novel Writer - API文档

## 概述

本文档描述了AI Novel Writer的RESTful API接口规范。所有API返回统一的JSON格式响应。

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **编码**: `UTF-8`

## 统一响应格式

```json
{
  "code": 200,
  "message": "Success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|-----|------|------|
| code | Integer | 响应码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据 |

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 小说管理 API

### 1.1 创建小说

**接口**: `POST /api/novels`

**请求示例**:
```json
{
  "title": "修仙传奇",
  "description": "一个关于修仙的故事",
  "author": "张三",
  "type": "玄幻",
  "writingStyle": "轻松幽默，节奏明快",
  "targetWordCount": 1000000
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "title": "修仙传奇",
    "description": "一个关于修仙的故事",
    "author": "张三",
    "type": "玄幻",
    "writingStyle": "轻松幽默，节奏明快",
    "targetWordCount": 1000000,
    "currentWordCount": 0,
    "chapterCount": 0,
    "status": "PLANNING",
    "createdAt": "2025-12-27T10:00:00",
    "updatedAt": "2025-12-27T10:00:00"
  }
}
```

### 1.2 更新小说

**接口**: `PUT /api/novels/{id}`

**请求示例**:
```json
{
  "title": "修仙传奇（新）",
  "status": "WRITING"
}
```

### 1.3 获取小说详情

**接口**: `GET /api/novels/{id}`

**响应**: 同创建小说响应

### 1.4 分页查询小说列表

**接口**: `GET /api/novels?page=0&size=20&status=WRITING`

**查询参数**:
- `page`: 页码，从0开始（默认0）
- `size`: 每页大小（默认20）
- `status`: 小说状态（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

### 1.5 删除小说

**接口**: `DELETE /api/novels/{id}`

---

## 2. 章节管理 API

### 2.1 创建章节

**接口**: `POST /api/chapters`

**请求示例**:
```json
{
  "novelId": 1,
  "chapterNumber": 1,
  "title": "第一章 初入仙门",
  "content": "章节正文内容...",
  "summary": "主角初次来到仙门",
  "sceneId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "novelId": 1,
    "chapterNumber": 1,
    "title": "第一章 初入仙门",
    "content": "章节正文内容...",
    "summary": "主角初次来到仙门",
    "wordCount": 2000,
    "status": "DRAFT",
    "sceneId": 1,
    "sceneName": "仙门入口",
    "createdAt": "2025-12-27T10:00:00",
    "updatedAt": "2025-12-27T10:00:00"
  }
}
```

### 2.2 AI续写章节 ⭐️

**接口**: `POST /api/chapters/continue`

**请求示例**:
```json
{
  "novelId": 1,
  "chapterNumber": 2,
  "sceneId": 1,
  "direction": "主角遇到了第一个考验",
  "temperature": 0.7,
  "maxLength": 2000
}
```

**响应**: 同创建章节响应，content字段包含AI生成的内容

### 2.3 获取章节详情

**接口**: `GET /api/chapters/{id}`

### 2.4 分页查询章节列表

**接口**: `GET /api/chapters/novel/{novelId}?page=0&size=20`

### 2.5 更新章节

**接口**: `PUT /api/chapters/{id}`

### 2.6 删除章节

**接口**: `DELETE /api/chapters/{id}`

---

## 3. 角色管理 API

### 3.1 创建角色

**接口**: `POST /api/characters`

**请求示例**:
```json
{
  "novelId": 1,
  "name": "李逍遥",
  "description": "主角，年轻有为的修仙者",
  "personality": "正直、勇敢、富有正义感",
  "backstory": "出身寒门，因缘际会踏上修仙之路",
  "importance": "MAIN",
  "age": 18,
  "gender": "男",
  "appearance": "身材修长，剑眉星目",
  "abilities": "基础剑法、灵力感知",
  "goals": "成为一代剑仙",
  "currentState": "刚入仙门，正在学习基础功法"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "novelId": 1,
    "name": "李逍遥",
    "description": "主角，年轻有为的修仙者",
    "personality": "正直、勇敢、富有正义感",
    "backstory": "出身寒门，因缘际会踏上修仙之路",
    "importance": "MAIN",
    "age": 18,
    "gender": "男",
    "appearance": "身材修长，剑眉星目",
    "abilities": "基础剑法、灵力感知",
    "goals": "成为一代剑仙",
    "currentState": "刚入仙门，正在学习基础功法",
    "appearanceCount": 0,
    "firstAppearance": null,
    "lastAppearance": null,
    "createdAt": "2025-12-27T10:00:00",
    "updatedAt": "2025-12-27T10:00:00"
  }
}
```

### 3.2 更新角色

**接口**: `PUT /api/characters/{id}`

### 3.3 获取角色详情

**接口**: `GET /api/characters/{id}`

### 3.4 查询小说的所有角色

**接口**: `GET /api/characters/novel/{novelId}`

### 3.5 分页查询角色

**接口**: `GET /api/characters/novel/{novelId}/page?page=0&size=20`

### 3.6 删除角色

**接口**: `DELETE /api/characters/{id}`

---

## 4. 健康检查 API

### 4.1 健康检查

**接口**: `GET /api/health`

**响应示例**:
```json
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

---

## 5. 枚举类型说明

### 5.1 小说状态 (NovelStatus)

| 值 | 说明 |
|----|------|
| PLANNING | 规划中 |
| WRITING | 写作中 |
| PAUSED | 暂停 |
| COMPLETED | 已完成 |
| ABANDONED | 已弃坑 |

### 5.2 章节状态 (ChapterStatus)

| 值 | 说明 |
|----|------|
| DRAFT | 草稿 |
| IN_PROGRESS | 进行中 |
| COMPLETED | 已完成 |
| REVISED | 已修订 |
| PUBLISHED | 已发布 |

### 5.3 角色重要性 (CharacterImportance)

| 值 | 说明 |
|----|------|
| MAIN | 主要角色 |
| SUPPORTING | 配角 |
| MINOR | 次要角色 |
| EXTRA | 群演 |

### 5.4 场景状态 (SceneStatus)

| 值 | 说明 |
|----|------|
| PLANNING | 规划中 |
| IN_PROGRESS | 进行中 |
| COMPLETED | 已完成 |

---

## 6. 使用示例

### 6.1 创建一个完整的小说项目

```bash
# 1. 创建小说
curl -X POST http://localhost:8080/api/novels \
  -H "Content-Type: application/json" \
  -d '{
    "title": "修仙传奇",
    "author": "张三",
    "type": "玄幻",
    "writingStyle": "轻松幽默",
    "targetWordCount": 1000000
  }'

# 2. 创建主角
curl -X POST http://localhost:8080/api/characters \
  -H "Content-Type: application/json" \
  -d '{
    "novelId": 1,
    "name": "李逍遥",
    "importance": "MAIN",
    "age": 18,
    "gender": "男"
  }'

# 3. AI续写第一章
curl -X POST http://localhost:8080/api/chapters/continue \
  -H "Content-Type: application/json" \
  -d '{
    "novelId": 1,
    "chapterNumber": 1,
    "direction": "主角初入仙门",
    "temperature": 0.7,
    "maxLength": 2000
  }'
```

---

## 7. 注意事项

1. **参数校验**: 所有必填参数必须提供，否则返回400错误
2. **资源依赖**: 创建章节/角色前必须先创建小说
3. **字符限制**: 标题、描述等字段有最大长度限制
4. **AI续写**: 需要配置DASHSCOPE_API_KEY环境变量
5. **分页查询**: 默认按更新时间倒序排列

---

## 8. 扩展功能（待实现）

- 场景管理API
- 大纲管理API
- 情节线管理API
- 世界观设定API
- 智能推荐API
- 编辑历史API
