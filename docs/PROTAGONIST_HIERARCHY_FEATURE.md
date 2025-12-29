# AI小说创作系统 - 主角层级区分功能说明

**实现时间**: 2025-12-29  
**功能版本**: 2.2.0  
**需求来源**: 叙事结构优化需求  

---

## 📋 功能概述

本次更新实现了对**整体主角**与**章节视角角色（临时主角）**的明确区分，使AI能够准确识别小说的叙事结构层级，理解各个角色在整体叙事中的定位和功能。

### 核心概念

1. **整体主角（Global Protagonist）**
   - 贯穿全书始终的核心人物
   - 是整部小说的真正主角
   - 推动主线剧情发展的关键人物

2. **视角角色/临时主角（POV Character）**
   - 某个章节或场景中的视角人物
   - 可能是配角、反派或次要角色
   - 通过其视角推进主线剧情、揭示关键信息、制造戏剧冲突

3. **层级关系**
   - 临时主角服务于整体主角
   - 不同视角丰富故事层次
   - 多视角叙事增强戏剧张力

---

## 🎯 实现的功能

### 1. 数据模型扩展

#### Character（角色实体）
新增字段：
```java
@Column(name = "is_global_protagonist")
private Boolean isGlobalProtagonist = false;  // 是否为整本书的主角

@Column(name = "importance_level")
private Integer importanceLevel = 5;  // 角色重要性级别（1-10）
```

**重要性级别规则**：
- 10: 整体主角
- 9: 核心反派
- 6: 重要配角
- 3: 次要角色
- 1: 龙套

#### Chapter（章节实体）
新增字段：
```java
@Column(name = "viewpoint_character_id")
private Long viewpointCharacterId;  // 章节视角角色ID
```

#### ContentAnalysis（内容分析实体）
新增字段：
```java
@Column(name = "viewpoint_character", length = 100)
private String viewpointCharacter;  // 章节视角角色名称

@Column(name = "is_global_protagonist_pov")
private Boolean isGlobalProtagonistPov = true;  // 是否使用整体主角视角

@Column(name = "narrative_perspective", length = 50)
private String narrativePerspective;  // 叙述视角（第一/三人称等）
```

### 2. AI分析增强

#### 更新的分析提示词
```
【重要概念】关于主角层级的区分：
1. **整体主角（Global Protagonist）**：贯穿全书始终的核心人物
2. **视角角色/临时主角（POV Character）**：某个章节的视角人物
3. 临时主角的作用：推进主线剧情、揭示信息、制造冲突

【分析要点】：
- 区分"这个人物在当前章节是焦点"和"这个人物是整本书的主角"
- 通过出场频率、情节关键性、情感投入等判断重要性级别
- 明确标注视角角色和整体主角的关系
```

#### 新增的JSON分析字段
```json
{
  "protagonist": "整体主角名称（如果能确定）",
  "viewpoint_character": "当前章节视角角色名称",
  "is_global_protagonist_pov": true/false,
  "narrative_perspective": "第一人称/第三人称限知/第三人称全知",
  "characters": [
    {
      "name": "角色名",
      "role": "整体主角/配角/龙套",
      "is_global_protagonist": true/false,
      "importance_level": 8,
      ...
    }
  ]
}
```

### 3. 自动角色提取优化

**ChapterService.autoCreateCharacters() 方法增强**：

```java
// 设置是否为整体主角
if (dto.getIsGlobalProtagonist() != null) {
    character.setIsGlobalProtagonist(dto.getIsGlobalProtagonist());
} else {
    character.setIsGlobalProtagonist("PROTAGONIST".equals(character.getRoleType()));
}

// 设置重要性级别
if (dto.getImportanceLevel() != null) {
    character.setImportanceLevel(dto.getImportanceLevel());
} else {
    character.setImportanceLevel(getDefaultImportanceLevel(character.getRoleType()));
}
```

**默认重要性映射**：
- PROTAGONIST → 10
- ANTAGONIST → 9
- SUPPORTING → 6
- MINOR → 3

### 4. 前端展示优化

#### ChapterWriteEnhanced.vue（章节续写页面）
新增视角信息展示：
```html
<!-- 整体主角 -->
📚 整体主角：[主角名称]

<!-- 章节视角角色 -->
👁️ 当前视角：[视角角色]  [临时主角]标签

<!-- 叙述视角 -->
📖 叙述视角：第一人称/第三人称限知
```

#### SmartWriting.vue（智能创作页面）
优化角色视角展示：
- 区分整体主角和当前视角
- 标注临时主角身份
- 显示叙述视角类型

---

## 💡 使用场景示例

### 场景1：多视角小说
**《冰与火之歌》式多POV叙事**

```
第1章（整体主角视角）
- 整体主角：艾德·史塔克
- 当前视角：艾德·史塔克
- 是否整体主角视角：是
- 叙述视角：第三人称限知

第2章（配角视角）
- 整体主角：艾德·史塔克
- 当前视角：提利昂·兰尼斯特
- 是否整体主角视角：否 [临时主角]
- 叙述视角：第三人称限知
```

### 场景2：单主角偶尔切换视角
**传统武侠小说**

```
第1-10章
- 整体主角：张无忌
- 当前视角：张无忌
- 是否整体主角视角：是

第11章（反派视角）
- 整体主角：张无忌
- 当前视角：成昆
- 是否整体主角视角：否 [临时主角]
- 作用：揭示反派阴谋，制造悬念
```

### 场景3：第一人称叙事
**悬疑推理小说**

```
全书视角
- 整体主角：侦探华生
- 当前视角：华生
- 是否整体主角视角：是
- 叙述视角：第一人称
```

---

## 🔄 数据流程

### 1. 内容创建/续写流程
```
1. 用户输入章节内容
   ↓
2. AI分析内容
   - 识别整体主角
   - 识别视角角色
   - 判断是否为主角视角
   - 确定叙述视角
   ↓
3. 自动创建/更新角色
   - 设置isGlobalProtagonist
   - 设置importanceLevel
   ↓
4. 保存分析结果
   - 关联章节视角角色ID
   ↓
5. 前端展示
   - 显示视角层级信息
```

### 2. 续写上下文构建
```
构建上下文时：
1. 获取整体主角信息
2. 获取当前章节视角角色
3. 在提示词中明确区分：
   "整体主角是[X]，但当前章节从[Y]的视角展开..."
```

---

## 📊 数据库字段说明

### characters表
| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| is_global_protagonist | BOOLEAN | 是否为整体主角 | true/false |
| importance_level | INTEGER | 重要性级别 | 1-10 |

### chapters表
| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| viewpoint_character_id | BIGINT | 视角角色ID | 123 |

### content_analysis表
| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| viewpoint_character | VARCHAR(100) | 视角角色名 | "提利昂" |
| is_global_protagonist_pov | BOOLEAN | 是否主角视角 | false |
| narrative_perspective | VARCHAR(50) | 叙述视角 | "第三人称限知" |

---

## 🎨 前端UI示例

### 章节续写页面上下文展示

```
┌─────────────────────────────────────────┐
│ 🔍 内容分析                              │
├─────────────────────────────────────────┤
│ 📚 整体主角：艾德·史塔克               │
│ 👁️ 当前视角：提利昂 [临时主角]        │
│ 📖 叙述视角：第三人称限知              │
│ ⚔️ 当前冲突：兰尼斯特家族的阴谋       │
│ 💭 情感基调：紧张                       │
└─────────────────────────────────────────┘
```

### 智能创作分析结果

```
┌─────────────────────────────────────────┐
│ 🌟 角色视角                              │
├─────────────────────────────────────────┤
│ 整体主角                                 │
│ ├─ 艾德·史塔克                          │
│                                          │
│ 当前视角                                 │
│ ├─ 提利昂·兰尼斯特 [临时主角]          │
│                                          │
│ 叙述视角                                 │
│ ├─ 第三人称限知                         │
└─────────────────────────────────────────┘
```

---

## 🔧 技术实现细节

### 1. AI提示词优化
- **位置**：`ContentAnalysisService.buildAnalysisSystemPrompt()`
- **增强点**：
  - 明确主角层级概念定义
  - 要求区分整体主角和视角角色
  - 提供重要性级别判断标准
  - 强调临时主角的功能定位

### 2. 数据解析增强
- **位置**：`ContentAnalysisService.parseAnalysisResult()`
- **新增解析**：
  ```java
  analysis.setViewpointCharacter(root.get("viewpoint_character").asText());
  analysis.setIsGlobalProtagonistPov(root.get("is_global_protagonist_pov").asBoolean());
  analysis.setNarrativePerspective(root.get("narrative_perspective").asText());
  ```

### 3. DTO传输优化
- **位置**：`ContentAnalysisResponse`
- **新增字段**：viewpointCharacter, isGlobalProtagonistPov, narrativePerspective
- **ExtractedCharacterDTO扩展**：isGlobalProtagonist, importanceLevel

### 4. 前端响应式展示
- **视觉区分**：
  - 整体主角：紫色背景 `bg-purple-50`
  - 视角角色：蓝色背景 `bg-blue-50`
  - 临时主角标签：黄色徽章 `bg-yellow-200`

---

## 📈 功能优势

### 1. 更准确的角色理解
- AI能区分章节焦点人物和真正的主角
- 避免将临时视角角色误判为主角
- 准确理解多视角叙事结构

### 2. 更连贯的情节续写
- 续写时明确当前视角
- 保持视角一致性
- 合理安排视角切换

### 3. 更智能的角色管理
- 自动标记角色重要性
- 区分核心角色和次要角色
- 优化角色库展示顺序

### 4. 更好的用户体验
- 直观展示视角层级
- 清晰标注临时主角
- 帮助作者理解叙事结构

---

## 🎯 最佳实践建议

### 1. 创作多视角小说
```
✅ 推荐做法：
- 在章节开始明确标注当前视角
- 保持同一章节视角统一
- 视角切换要有明确的分章

❌ 避免做法：
- 同一章节频繁切换视角
- 不标注视角就切换人物
- 临时视角占比过大
```

### 2. 设置整体主角
```
✅ 推荐做法：
- 在角色管理中明确标记整体主角
- 确保主角出场频率最高
- 主角推动主线剧情

❌ 避免做法：
- 多个角色都标记为整体主角
- 整体主角长期不出场
- 主角与主线脱节
```

### 3. 使用临时视角
```
✅ 推荐做法：
- 临时视角用于揭示信息
- 创造悬念和戏剧冲突
- 丰富故事层次

❌ 避免做法：
- 过度使用临时视角
- 临时视角不服务主线
- 视角角色特征不鲜明
```

---

## 🔍 测试验证

### 测试场景1：创建多视角章节
```
1. 创建章节1（主角视角）
   - 输入主角视角内容
   - 验证AI识别整体主角
   - 验证isGlobalProtagonistPov=true

2. 创建章节2（配角视角）
   - 输入配角视角内容
   - 验证AI识别视角角色
   - 验证isGlobalProtagonistPov=false
   - 验证显示"临时主角"标签
```

### 测试场景2：角色重要性
```
1. 分析包含多个角色的章节
2. 验证角色重要性级别合理
3. 验证整体主角标记正确
4. 验证角色排序符合重要性
```

### 测试场景3：前端展示
```
1. 打开章节续写页面
2. 验证显示整体主角
3. 验证显示视角角色
4. 验证临时主角标签
5. 验证叙述视角信息
```

---

## 📝 数据库迁移

### 新增字段SQL（H2数据库）
```sql
-- characters表
ALTER TABLE characters ADD COLUMN is_global_protagonist BOOLEAN DEFAULT FALSE;
ALTER TABLE characters ADD COLUMN importance_level INTEGER DEFAULT 5;

-- chapters表
ALTER TABLE chapters ADD COLUMN viewpoint_character_id BIGINT;

-- content_analysis表
ALTER TABLE content_analysis ADD COLUMN viewpoint_character VARCHAR(100);
ALTER TABLE content_analysis ADD COLUMN is_global_protagonist_pov BOOLEAN DEFAULT TRUE;
ALTER TABLE content_analysis ADD COLUMN narrative_perspective VARCHAR(50);
```

**注意**：H2数据库使用`ddl-auto: update`，会自动创建新字段，无需手动执行SQL。

---

## 🚀 后续优化方向

### 1. 视角切换分析
- 检测视角切换的合理性
- 提示视角切换时机
- 统计各角色视角占比

### 2. 智能视角建议
- 根据剧情发展建议切换视角
- 推荐适合的视角角色
- 平衡各角色出场机会

### 3. 视角一致性检查
- 检查视角是否一致
- 标记视角混乱段落
- 提供视角修正建议

### 4. 角色关系图谱
- 可视化角色层级关系
- 展示主配角网络
- 分析角色互动频率

---

## 📚 参考资料

### 叙事视角类型
1. **第一人称**：我/我们
2. **第二人称**：你
3. **第三人称限知**：聚焦单一角色视角
4. **第三人称全知**：上帝视角，了解所有
5. **多视角**：多个第三人称限知的组合

### 多POV经典作品
- 《冰与火之歌》（乔治·R·R·马丁）
- 《云图》（大卫·米切尔）
- 《白夜行》（东野圭吾）
- 《嫌疑人X的献身》（东野圭吾）

---

## ✅ 功能清单

- ✅ Character实体扩展：isGlobalProtagonist, importanceLevel
- ✅ Chapter实体扩展：viewpointCharacterId
- ✅ ContentAnalysis实体扩展：视角相关字段
- ✅ AI分析提示词增强：主角层级区分
- ✅ JSON解析逻辑更新：新字段解析
- ✅ DTO扩展：传输视角信息
- ✅ 自动角色提取优化：标记整体主角和重要性
- ✅ 前端ChapterWriteEnhanced展示优化
- ✅ 前端SmartWriting展示优化
- ✅ 编译测试通过
- ✅ 服务部署成功

---

## 🎉 总结

本次功能更新实现了对小说叙事结构的深度理解，能够准确区分整体主角与章节视角角色，为创作多视角复杂叙事提供了强大支持。通过AI的智能分析和自动化处理，作者可以更专注于故事创作本身，系统会自动管理和追踪角色层级关系。

**核心价值**：
- 🎯 准确识别叙事层级
- 🔄 支持多视角叙事
- 🤖 智能角色管理
- 📊 清晰视角展示
- ✨ 提升创作体验

---

*文档版本: 1.0*  
*最后更新: 2025-12-29*  
*功能状态: 已部署上线*
