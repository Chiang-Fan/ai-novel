# Phase 4 快速参考指南

## 🎯 API 快速导航

### 1. 生成增强型续写（核心功能）

```bash
POST /api/enhanced-continuation/generate
Content-Type: application/json

{
  "chapterId": 1,                          # 章节 ID（必填）
  "sourceText": "主角走进...",             # 上文内容（必填）
  "style": "SUSPENSE",                     # 风格：SERIOUS|LIGHT|SUSPENSE|ROMANTIC|ACTION|NEUTRAL
  "length": "PARAGRAPH",                   # 长度：SENTENCE|PARAGRAPH|SECTION|LONG
  "enableConstraints": true,               # 启用多维约束
  "checkPlotConsistency": true,            # 检查情节一致性
  "enableQualityScore": true,              # 评分质量
  "iterativeRefinement": false,            # 迭代优化
  "minimumQualityScore": 60,               # 最低质量分数（0-100）
  "maxIterations": 3,                      # 最大迭代次数
  "debugMode": false                       # 调试模式
}

Response:
{
  "code": 0,
  "message": "续写生成成功",
  "data": {
    "id": 123,
    "continuationText": "生成的续写文本",
    "constraints": { /* 约束信息 */ },
    "consistencyCheck": { /* 一致性检查结果 */ },
    "qualityScore": { /* 质量评分 */ },
    "recommendation": { /* 应用建议 */ }
  }
}
```

### 2. 查看多维约束

```bash
GET /api/enhanced-continuation/constraints/{novelId}/{chapterId}

Response:
{
  "novelId": 1,
  "chapterId": 1,
  "worldConstraints": {
    "universeRules": "...",
    "geographicalLimitations": [...],
    "magicSystemRules": "...",
    "constraintStrength": 75
  },
  "sceneConstraints": {
    "sceneName": "黑暗房间",
    "location": "古堡地下室",
    "atmosphere": "压抑、诡异"
  },
  "characterConstraints": {
    "protagonist": {
      "name": "主角名",
      "personality": "谨慎、聪慧",
      "abilities": "..."
    }
  },
  "totalConstraintStrength": 72
}
```

### 3. 验证约束违规

```bash
POST /api/enhanced-continuation/validate-constraints
{
  "novelId": 1,
  "chapterId": 1,
  "continuationText": "生成的续写文本"
}

Response:
{
  "valid": false,
  "violations": [
    {
      "category": "世界观",
      "message": "包含禁止元素：时空穿梭"
    }
  ]
}
```

### 4. 检查情节一致性

```bash
POST /api/enhanced-continuation/check-consistency
{
  "novelId": 1,
  "chapterId": 1,
  "continuationText": "新续写文本",
  "previousContent": "前文内容"
}

Response:
{
  "consistencyScore": 85,
  "isConsistent": true,
  "conflicts": [],
  "logicalIssues": [],
  "styleConsistency": {
    "isConsistent": true,
    "similarityScore": 0.92
  },
  "characterConsistency": [...],
  "timelineCheck": {
    "isValid": true
  },
  "recommendations": [...]
}
```

### 5. 质量评分

```bash
POST /api/enhanced-continuation/quality-score
Content-Type: text/plain

待评分的文本内容...

Response:
{
  "overallScore": 82,
  "naturalness": 85,
  "detailLevel": 78,
  "dialogueScore": {
    "score": 80,
    "naturalness": 82,
    "characterDistinction": 88
  },
  "psychologicalScore": {
    "score": 85,
    "refinement": 88
  },
  "environmentScore": {
    "score": 75,
    "visualDetail": 80
  },
  "emotionalScore": {
    "score": 82,
    "authenticity": 85
  },
  "actionScore": {
    "score": 78,
    "vividness": 82
  },
  "feedbacks": [...],
  "suggestions": [...]
}
```

## 📊 评分标准速查

### 一致性评分

| 分数 | 评级 | 说明 |
|------|------|------|
| 85-100 | 优秀 | 完全一致，直接应用 |
| 70-84 | 良好 | 基本一致，可应用 |
| 50-69 | 合格 | 有问题但可修改 |
| 0-49 | 不合格 | 存在严重冲突 |

### 质量评分

| 分数 | 评级 | 说明 |
|------|------|------|
| 90-100 | 优秀 | 描写生动，细节丰富 |
| 75-89 | 良好 | 描写清晰，质量不错 |
| 60-74 | 合格 | 基本满足，可接受 |
| 0-59 | 不合格 | 需要重新生成 |

## 🔧 常见场景代码片段

### 场景 1：基础续写

```java
EnhancedContinuationRequest request = new EnhancedContinuationRequest();
request.setChapterId(1L);
request.setSourceText("主角走进了黑暗的房间...");
request.setStyle("SUSPENSE");
request.setLength("PARAGRAPH");
request.setEnableConstraints(true);
request.setCheckPlotConsistency(true);
request.setEnableQualityScore(true);

EnhancedContinuationResponse response = 
  enhancedContinuationService.generateEnhancedContinuation(request);

System.out.println("续写评分: " + response.getQualityScore().getOverallScore());
```

### 场景 2：高质量迭代优化

```java
EnhancedContinuationRequest request = new EnhancedContinuationRequest();
request.setChapterId(1L);
request.setSourceText("...");
request.setIterativeRefinement(true);      // 启用迭代
request.setMinimumQualityScore(80);        // 目标分数 80
request.setMaxIterations(5);               // 最多迭代 5 次

EnhancedContinuationResponse response = 
  enhancedContinuationService.generateEnhancedContinuation(request);

if (response.getRecommendation().getRecommendToApply()) {
  // 质量满足，可以应用
  applyContinuation(response.getId());
}
```

### 场景 3：约束查询与验证

```java
// 获取约束信息
CompositeConstraints constraints = 
  constraintEngine.buildCompositeConstraints(novelId, chapterId);

System.out.println("世界观约束强度: " + 
  constraints.getWorldConstraints().getConstraintStrength());

// 验证文本是否违反约束
ConstraintValidationResult result = 
  constraintEngine.validateAgainstConstraints(continuationText, constraints);

if (!result.getValid()) {
  result.getViolations().forEach(v -> 
    System.out.println("违规: " + v.getCategory() + " - " + v.getMessage())
  );
}
```

### 场景 4：一致性手动检查

```java
PlotConsistencyCheckResult result = 
  consistencyCheckService.checkPlotConsistency(
    novelId, 
    chapterId, 
    newText, 
    previousText
  );

System.out.println("一致性评分: " + result.getConsistencyScore());

if (!result.getConflicts().isEmpty()) {
  result.getConflicts().forEach(conflict -> 
    System.out.println("冲突: " + conflict.getType() + " - " + 
                      conflict.getDescription())
  );
}
```

## 🎯 参数调优指南

### 不同场景的参数设置

#### 快速生成模式
```json
{
  "enableConstraints": false,
  "checkPlotConsistency": false,
  "enableQualityScore": false,
  "iterativeRefinement": false
}
```
**特点**: 快速，但质量无保证

#### 标准模式（推荐）
```json
{
  "enableConstraints": true,
  "checkPlotConsistency": true,
  "enableQualityScore": true,
  "iterativeRefinement": false,
  "minimumQualityScore": 60
}
```
**特点**: 平衡，质量有保证

#### 高质量模式
```json
{
  "enableConstraints": true,
  "checkPlotConsistency": true,
  "enableQualityScore": true,
  "iterativeRefinement": true,
  "minimumQualityScore": 80,
  "maxIterations": 5
}
```
**特点**: 慢但质量最高

## 📈 响应数据结构

### qualityScore 结构
```
{
  "overallScore": 82,           # 综合评分 (0-100)
  "naturalness": 85,            # 自然度 (0-100)
  "detailLevel": 78,            # 细节度 (0-100)
  "dialogueScore": {...},       # 对话评分
  "psychologicalScore": {...},  # 心理描写评分
  "environmentScore": {...},    # 环境描写评分
  "emotionalScore": {...},      # 情绪描写评分
  "actionScore": {...},         # 动作描写评分
  "feedbacks": [...],           # 详细反馈
  "suggestions": [...]          # 改进建议
}
```

### consistencyCheck 结构
```
{
  "consistencyScore": 85,       # 一致性评分 (0-100)
  "isConsistent": true,         # 是否通过
  "conflicts": [...],           # 冲突列表
  "logicalIssues": [...],       # 逻辑问题
  "styleConsistency": {...},    # 风格一致性
  "characterConsistency": [...],# 角色一致性
  "timelineCheck": {...},       # 时间线检查
  "recommendations": [...]      # 建议
}
```

## 🐛 调试技巧

### 1. 查看生成的约束提示词
```bash
GET /api/enhanced-continuation/debug/constraint-prompt/{novelId}/{chapterId}
```

### 2. 启用 DEBUG 模式
```json
{
  "debugMode": true
}
```
响应中会包含更多调试信息

### 3. 版本比较
```bash
POST /api/enhanced-continuation/compare-versions
{
  "continuationVersions": ["版本1", "版本2", "版本3"]
}
```

## ⚡ 性能优化建议

1. **约束缓存**: 同一小说的约束一般不变，可缓存 1 小时
2. **批量评分**: 使用版本比较功能批量评分多个版本
3. **异步处理**: 迭代优化可异步执行，不阻塞用户
4. **数据库索引**: 已优化关键查询的索引

## 🔗 相关资源

- 📖 [完整技术文档](./docs/Phase4_AI_Enhancement_Implementation.md)
- 📊 [完成总结](./PHASE4_COMPLETION_SUMMARY.md)
- 📋 [最终状态报告](./FINAL_STATUS_REPORT.md)

---

**最后更新**: 2024-2025  
**版本**: Phase 4 ✅ 完成
