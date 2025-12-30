# 📚 AI智能小说创作系统 - 核心功能优化完整规划

**文档版本**: v1.0  
**创建日期**: 2025-12-30  
**优化周期**: 5-8周  
**目标**: 建立专业级、全链条、智能约束的小说创作平台

---

## 目录
1. [执行摘要](#执行摘要)
2. [需求详细分析](#需求详细分析)
3. [技术架构设计](#技术架构设计)
4. [数据模型扩展](#数据模型扩展)
5. [实现路线图](#实现路线图)
6. [核心模块详设](#核心模块详设)

---

## 执行摘要

### 当前项目状态
- ✅ 已实现：基础CRUD、内容分析、续写建议、版本管理、伏笔管理、角色成长等核心功能
- ⏳ 需优化：创建流程、AI约束、场景管理、描写质量、导入功能等

### 优化目标
通过 **6大核心功能优化**，打造完整的创作链条：
```
大纲 → 世界观 → 场景 → 角色 → 描写 → 续写 → 修订
 ↑                                              ↓
 └────────────── AI智能约束与辅助 ──────────────┘
```

### 预期收益
- 🎯 **创意效率** ↑40% - AI智能推荐和约束指导
- 📝 **内容质量** ↑35% - 多维约束和细节优化
- ⏱️ **编写速度** ↑50% - 文本导入和自动分章
- 🔗 **逻辑连贯** ↑60% - 世界观约束和节奏管理

---

## 需求详细分析

### 需求1：小说创建强化

#### 问题分析
- **现状**：创建小说流程简单，用户缺乏创意框架指导
- **问题**：
  - 新手用户起手无策，创意门槛高
  - 大纲和场景缺乏前置验证
  - 无智能建议支持

#### 优化方案

**1.1 表单结构优化**
```
小说创建表单
├─ 基础信息 (不变)
│  ├─ 小说名称 *
│  ├─ 小说类型 * (下拉)
│  └─ 简介
├─ 大纲输入 * (必填)
│  ├─ 输入框 (提示：请输入创作大纲，3-500字)
│  ├─ AI推荐按钮 💡
│  └─ 推荐面板 (右侧滑入)
├─ 初始场景 * (必填)
│  ├─ 输入框 (提示：请描述开篇场景，3-200字)
│  ├─ AI推荐按钮 💡
│  └─ 推荐面板 (右侧滑入)
└─ 提交
   ├─ 验证: 大纲+场景 ≥ 最小字数
   └─ 创建或下一步
```

**1.2 AI推荐集成**

前端交互：
```javascript
// 点击AI推荐按钮的流程
1. 用户输入 (100字) 或直接点击 (空输入)
2. 触发加载状态 (显示骨架屏)
3. 调用 API: POST /api/suggestions/generate-for-outline
   {
     "genre": "武侠",
     "theme": "侠骨柔情",
     "existingContext": "现有输入文本" // 可选
   }
4. 返回推荐列表 (3-5条)
   [
     {
       "id": "uuid",
       "title": "传统侠客恩怨",
       "description": "以江湖恩怨为主线...",
       "keyEvents": ["..."],
       "score": 8.5,
       "preview": "..." // 完整内容
     },
     ...
   ]
5. 用户操作:
   - 查看预览 → 展开详情
   - 一键采用 → 替换输入框内容
   - 刷新建议 → 重新调用API
```

后端实现：
```java
// Controller
@PostMapping("/suggestions/generate-for-outline")
public ApiResponse<List<SuggestionResponse>> generateOutlineSuggestions(
    @RequestBody OutlineGenerationRequest req) {
  // 调用 AutoSuggestionService
  // 基于 genre + theme 生成建议
  // 返回评分排序的建议列表
}

// Service
public List<SuggestionResponse> generateOutlineSuggestions(
    String genre, String theme, String context) {
  // 构建Prompt
  String prompt = buildOutlinePrompt(genre, theme, context);
  
  // 调用AI
  List<String> suggestions = aiService.generateListJson(
    prompt, 
    "outline_suggestions",
    5 // 生成5条建议
  );
  
  // 评分排序
  return suggestions.stream()
    .map(this::parseAndScore)
    .sorted(comparing(SuggestionResponse::getScore).reversed())
    .limit(5)
    .collect(toList());
}
```

**1.3 新建流程交互**

页面流程：
```
┌─────────────────┐
│ 输入基础信息     │
└────────┬────────┘
         ↓
┌─────────────────┐
│ 编写大纲(必填)   │──→ 📊 AI推荐面板
│ 长度验证        │    (3-5个建议)
└────────┬────────┘
         ↓
┌─────────────────┐
│ 描述初始场景     │──→ 📊 AI推荐面板
│ (必填)          │    (环境/人物)
└────────┬────────┘
         ↓
┌─────────────────┐
│ 验证并创建       │
└─────────────────┘
```

---

### 需求2：世界观设定模块

#### 问题分析
- **现状**：无专门的世界观管理模块，创作无统一规则框架
- **问题**：
  - 世界观设定分散不成体系
  - AI续写易违背已设定规则
  - 无约束检验机制

#### 优化方案

**2.1 数据模型设计**

```sql
-- 世界观主表
CREATE TABLE novel_worldview (
  id BIGINT PRIMARY KEY,
  novel_id BIGINT UNIQUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE
);

-- 背景设定子表
CREATE TABLE worldview_background (
  id BIGINT PRIMARY KEY,
  worldview_id BIGINT,
  category VARCHAR(50), -- "时代背景" / "地理环境" / "社会结构" / "历史设定"
  name VARCHAR(255),
  description TEXT,
  importance INT DEFAULT 3, -- 1-5等级
  constraint_level VARCHAR(20) -- "严格" / "参考" / "建议"
);

-- 规则体系子表
CREATE TABLE worldview_rules (
  id BIGINT PRIMARY KEY,
  worldview_id BIGINT,
  rule_type VARCHAR(50), -- "魔法/科技" / "社会法则" / "禁忌事项" / "特殊限制"
  rule_name VARCHAR(255),
  description TEXT,
  constraints TEXT, -- JSON: {"character": "", "action": "", "result": ""}
  violation_consequence VARCHAR(200),
  enforcement_level VARCHAR(20) -- "严格" / "可破坏" / "建议"
);

-- 特殊要素子表
CREATE TABLE worldview_elements (
  id BIGINT PRIMARY KEY,
  worldview_id BIGINT,
  element_type VARCHAR(50), -- "种族" / "文明" / "物质" / "神秘力量"
  name VARCHAR(255),
  characteristics TEXT, -- JSON: {属性列表}
  interaction_rules TEXT,
  plot_influence INT DEFAULT 3 -- 对情节的影响程度 1-5
);

-- 世界观与其他模块的关联检验表
CREATE TABLE worldview_constraint_checks (
  id BIGINT PRIMARY KEY,
  novel_id BIGINT,
  check_target VARCHAR(50), -- "outline" / "scene" / "character" / "continuation"
  target_id BIGINT,
  violations TEXT, -- JSON: 冲突列表
  severity VARCHAR(20), -- "critical" / "warning" / "info"
  last_checked TIMESTAMP
);
```

**2.2 编辑页面设计**

前端结构：
```
┌─────────────────────────────────────────────────────┐
│ Header: 世界观设定 │ 保存 │ 预览 │ 约束检验         │
├─────────────────────────────────────────────────────┤
│ TabBar:                                             │
│  [背景设定] [规则体系] [特殊要素] [关联检验]        │
├─────────────────────┬─────────────────────────────┤
│ BackgroundTab       │ RealTimePreview             │
│  ┌─────────────────┐│ 📋 世界观总览              │
│  │时代背景         ││ ├─ 背景: 3项              │
│  │  [编辑] [删除]  ││ ├─ 规则: 5项              │
│  │地理环境         ││ └─ 要素: 4项              │
│  │  [编辑] [删除]  ││                            │
│  │[+ 新增背景]     ││ 与其他模块关系:           │
│  └─────────────────┘│ ├─ 大纲: ✓ 一致           │
│                     │ ├─ 场景: ⚠️ 1项冲突       │
│ RulesTab            │ └─ 角色: ✓ 一致           │
│  ┌─────────────────┐│                            │
│  │魔法系统         ││ 最后检验: 2分钟前          │
│  │  [编辑] [删除]  ││ [立即检验] 按钮            │
│  │社会法则         ││                            │
│  │  [编辑] [删除]  ││                            │
│  │[+ 新增规则]     ││                            │
│  └─────────────────┘│                            │
│                     │                            │
│ ElementsTab         │                            │
│  ┌─────────────────┐│                            │
│  │精灵种族         ││                            │
│  │  [编辑] [删除]  ││                            │
│  │魔晶矿物         ││                            │
│  │  [编辑] [删除]  ││                            │
│  │[+ 新增要素]     ││                            │
│  └─────────────────┘│                            │
└─────────────────────┴─────────────────────────────┘
```

**2.3 后端服务**

```java
// WorldViewService.java
@Service
@RequiredArgsConstructor
public class WorldViewService {
  
  private final WorldViewRepository worldViewRepo;
  private final WorldViewBackgroundRepository bgRepo;
  private final WorldViewRulesRepository rulesRepo;
  private final WorldViewElementsRepository elemRepo;
  private final NovelRepository novelRepo;
  private final ConstraintEngineService constraintEngine;
  
  // 创建或更新世界观
  public WorldViewResponse upsertWorldView(Long novelId, 
                                          WorldViewRequest req) {
    Novel novel = novelRepo.findById(novelId)
      .orElseThrow(() -> new EntityNotFoundException("小说不存在"));
    
    NovelWorldview worldView = worldViewRepo
      .findByNovelId(novelId)
      .orElseGet(() -> {
        NovelWorldview nw = new NovelWorldview();
        nw.setNovelId(novelId);
        return worldViewRepo.save(nw);
      });
    
    // 保存背景设定
    saveBackgroundSettings(worldView.getId(), req.getBackgrounds());
    
    // 保存规则体系
    saveRules(worldView.getId(), req.getRules());
    
    // 保存特殊要素
    saveElements(worldView.getId(), req.getElements());
    
    return toResponse(worldView);
  }
  
  // 约束检验
  public WorldViewConstraintCheckResult validateWithConstraints(
      Long novelId) {
    NovelWorldview worldView = getWorldView(novelId);
    
    WorldViewConstraintCheckResult result = 
      new WorldViewConstraintCheckResult();
    
    // 检验大纲是否符合世界观
    result.setOutlineViolations(
      constraintEngine.checkOutlineAgainstWorldView(
        novelId, worldView
      )
    );
    
    // 检验场景是否符合世界观
    result.setSceneViolations(
      constraintEngine.checkScenesAgainstWorldView(
        novelId, worldView
      )
    );
    
    // 检验角色设定是否符合世界观
    result.setCharacterViolations(
      constraintEngine.checkCharactersAgainstWorldView(
        novelId, worldView
      )
    );
    
    // 检验已生成续写是否符合世界观
    result.setContinuationViolations(
      constraintEngine.checkContinuationsAgainstWorldView(
        novelId, worldView
      )
    );
    
    return result;
  }
}

// ConstraintEngine 示例
@Service
@RequiredArgsConstructor
public class ConstraintEngineService {
  
  public List<ConstraintViolation> checkOutlineAgainstWorldView(
      Long novelId, NovelWorldview worldView) {
    List<ConstraintViolation> violations = new ArrayList<>();
    
    Outline outline = outlineRepo.findByNovelId(novelId);
    if (outline == null) return violations;
    
    // 检查大纲中涉及的魔法/科技是否符合规则
    List<WorldViewRules> rules = rulesRepo
      .findByWorldViewId(worldView.getId());
    
    for (String keyEvent : outline.getKeyEvents()) {
      for (WorldViewRules rule : rules) {
        if (violatesRule(keyEvent, rule)) {
          violations.add(
            new ConstraintViolation(
              "大纲",
              keyEvent,
              rule.getRuleName(),
              "冲突原因说明"
            )
          );
        }
      }
    }
    
    return violations;
  }
  
  private boolean violatesRule(String text, WorldViewRules rule) {
    // 基于AI判断文本是否违反规则
    String checkPrompt = String.format(
      "检查以下文本是否违反规则:\n" +
      "规则: %s (%s)\n" +
      "约束: %s\n" +
      "文本: %s\n" +
      "判断: 违反 / 不违反",
      rule.getRuleName(),
      rule.getDescription(),
      rule.getConstraints(),
      text
    );
    
    String response = aiService.chat(checkPrompt);
    return response.contains("违反");
  }
}
```

---

### 需求3：文本智能导入功能

#### 问题分析
- **现状**：无导入功能，用户无法批量导入现有文本
- **问题**：
  - 长篇幅手动输入体验差
  - 无自动分章能力
  - 无关键要素自动提取

#### 优化方案

**3.1 核心功能需求**

```
上传文本
   ↓
┌─ 格式识别 (.txt / .doc / .docx)
├─ 编码检测 (UTF-8 / GBK)
├─ 内容验证 (大小 < 10MB)
└─ 预处理 (清理空行 / 规范化)
   ↓
自动分章
   ├─ 识别第x章/第x回/x 等分章标记
   ├─ 基于字数阈值分章 (1000-3000字/章)
   ├─ 基于内容断裂点分章 (场景/人物切换)
   └─ 用户手动调整分章点
   ↓
关键要素提取
   ├─ 大纲: 识别关键情节点和剧情线
   ├─ 场景: 提取环境描写和场景信息
   ├─ 人物: 识别所有人物及其描述
   └─ 伏笔: 检测潜在的伏笔和悬念
   ↓
预览确认
   ├─ 分章预览
   ├─ 要素预览
   └─ 冲突检测 (与现有小说的冲突)
   ↓
导入模式选择
   ├─ 新建小说
   └─ 补充到现有小说
   ↓
入库并关联
```

**3.2 数据模型**

```sql
-- 文本导入日志
CREATE TABLE text_import_log (
  id BIGINT PRIMARY KEY,
  novel_id BIGINT,
  original_filename VARCHAR(255),
  file_size INT,
  encoding VARCHAR(20),
  total_chapters INT,
  extracted_outlines INT,
  extracted_scenes INT,
  extracted_characters INT,
  extracted_hooks INT,
  conflicts_detected INT,
  status VARCHAR(20), -- "processing" / "completed" / "failed"
  error_message TEXT,
  created_at TIMESTAMP,
  completed_at TIMESTAMP
);

-- 导入的章节映射
CREATE TABLE import_chapter_mapping (
  id BIGINT PRIMARY KEY,
  import_log_id BIGINT,
  chapter_id BIGINT,
  original_chapter_number INT,
  raw_content TEXT,
  extracted_content TEXT,
  auto_extracted_elements JSON, -- {outlines, scenes, chars, hooks}
  manual_adjustments JSON,
  status VARCHAR(20) -- "pending" / "imported" / "rejected"
);

-- 提取的要素临时表
CREATE TABLE import_extracted_elements (
  id BIGINT PRIMARY KEY,
  import_log_id BIGINT,
  element_type VARCHAR(50), -- "outline" / "scene" / "character" / "hook"
  source_chapter INT,
  content TEXT,
  confidence DECIMAL(3,2), -- AI提取的置信度
  user_confirmed BOOLEAN,
  status VARCHAR(20) -- "pending" / "applied" / "rejected"
);
```

**3.3 后端实现**

```java
// TextImportService.java
@Service
@RequiredArgsConstructor
public class TextImportService {
  
  private final TextImportRepository importRepo;
  private final ChapterRepository chapterRepo;
  private final NovelRepository novelRepo;
  private final AiService aiService;
  private final ContentAnalysisService analysisService;
  
  // 步骤1: 上传并预处理
  @Async
  public TextImportLog uploadAndPreprocess(
      MultipartFile file,
      Long novelId) throws IOException {
    
    // 验证文件
    validateFile(file);
    
    // 读取文件内容
    String content = readFileContent(file);
    
    // 检测编码
    String encoding = detectEncoding(content);
    
    // 预处理
    String cleaned = preprocessContent(content);
    
    // 创建导入日志
    TextImportLog log = new TextImportLog();
    log.setNovelId(novelId);
    log.setOriginalFilename(file.getOriginalFilename());
    log.setFileSize((int) file.getSize());
    log.setEncoding(encoding);
    log.setStatus("processing");
    
    return importRepo.save(log);
  }
  
  // 步骤2: 智能分章
  public List<ImportedChapter> autoChapterize(
      TextImportLog log,
      String content) {
    
    List<ImportedChapter> chapters = new ArrayList<>();
    
    // 方案1: 识别标记
    chapters.addAll(splitByMarks(content));
    
    if (chapters.isEmpty()) {
      // 方案2: 字数分割
      chapters.addAll(splitByWordCount(content, 2000));
    }
    
    if (chapters.isEmpty()) {
      // 方案3: AI识别断裂点
      chapters.addAll(splitByAI(content));
    }
    
    return chapters;
  }
  
  private List<ImportedChapter> splitByMarks(String content) {
    List<ImportedChapter> chapters = new ArrayList<>();
    
    // 匹配 "第X章" "第X回" "X. " 等模式
    Pattern pattern = Pattern.compile(
      "第[\\d\\u4e00-\\u9fff]+[章回]|第[\\d]+[部分]|^\\d+\\.\\s",
      Pattern.MULTILINE
    );
    
    Matcher matcher = pattern.matcher(content);
    int lastEnd = 0;
    int chapterNum = 1;
    
    while (matcher.find()) {
      String chapterContent = content.substring(
        lastEnd, matcher.start()
      );
      
      if (chapterContent.length() > 50) { // 最小章节长度
        ImportedChapter chapter = new ImportedChapter();
        chapter.setChapterNumber(chapterNum++);
        chapter.setRawContent(chapterContent.trim());
        chapters.add(chapter);
      }
      
      lastEnd = matcher.start();
    }
    
    // 最后一章
    if (lastEnd < content.length()) {
      String lastChapter = content.substring(lastEnd);
      if (lastChapter.length() > 50) {
        ImportedChapter chapter = new ImportedChapter();
        chapter.setChapterNumber(chapterNum);
        chapter.setRawContent(lastChapter.trim());
        chapters.add(chapter);
      }
    }
    
    return chapters;
  }
  
  // 步骤3: 关键要素提取
  @Async
  public void extractKeyElements(TextImportLog log,
                                 List<ImportedChapter> chapters) {
    
    for (ImportedChapter chapter : chapters) {
      String chapterContent = chapter.getRawContent();
      
      // 提取大纲信息
      extractOutlines(log.getId(), chapter.getChapterNumber(), 
                     chapterContent);
      
      // 提取场景信息
      extractScenes(log.getId(), chapter.getChapterNumber(), 
                   chapterContent);
      
      // 提取人物信息
      extractCharacters(log.getId(), chapter.getChapterNumber(), 
                       chapterContent);
      
      // 提取伏笔信息
      extractPlotHooks(log.getId(), chapter.getChapterNumber(), 
                      chapterContent);
    }
  }
  
  private void extractOutlines(Long importLogId, int chapterNum, 
                              String content) {
    String prompt = String.format(
      "分析以下文本内容，提取关键情节点和剧情走向。\n" +
      "要求返回JSON格式:\n" +
      "{\n" +
      "  \"outlines\": [\n" +
      "    {\"event\": \"事件名称\", \"type\": \"rising/climax/falling\", " +
      "\"position\": \"第几段\"}\n" +
      "  ]\n" +
      "}\n" +
      "文本:\n%s",
      content.substring(0, Math.min(3000, content.length()))
    );
    
    String result = aiService.chatJson(prompt, "outline_extraction");
    
    // 解析结果并保存
    saveExtractedElements(importLogId, chapterNum, "outline", result);
  }
  
  private void extractScenes(Long importLogId, int chapterNum, 
                            String content) {
    String prompt = String.format(
      "从以下文本中提取所有场景信息 (地点、环境、氛围等)。\n" +
      "返回JSON格式:\n" +
      "{\n" +
      "  \"scenes\": [\n" +
      "    {\"location\": \"地点名称\", \"description\": \"描写内容\", " +
      "\"atmosphere\": \"氛围\"}\n" +
      "  ]\n" +
      "}\n" +
      "文本:\n%s",
      content.substring(0, Math.min(3000, content.length()))
    );
    
    String result = aiService.chatJson(prompt, "scene_extraction");
    saveExtractedElements(importLogId, chapterNum, "scene", result);
  }
  
  private void extractCharacters(Long importLogId, int chapterNum, 
                                String content) {
    String prompt = String.format(
      "从以下文本中识别所有人物及其特征。\n" +
      "返回JSON格式:\n" +
      "{\n" +
      "  \"characters\": [\n" +
      "    {\"name\": \"人物名称\", \"role\": \"角色类型\", " +
      "\"traits\": \"特征描写\", \"relationships\": \"关系\"}\n" +
      "  ]\n" +
      "}\n" +
      "文本:\n%s",
      content.substring(0, Math.min(3000, content.length()))
    );
    
    String result = aiService.chatJson(prompt, "character_extraction");
    saveExtractedElements(importLogId, chapterNum, "character", result);
  }
  
  private void extractPlotHooks(Long importLogId, int chapterNum, 
                               String content) {
    String prompt = String.format(
      "从以下文本中检测潜在的伏笔和悬念 (悬而未决的问题、暗示等)。\n" +
      "返回JSON格式:\n" +
      "{\n" +
      "  \"hooks\": [\n" +
      "    {\"description\": \"伏笔描述\", \"type\": \"explicit/implicit\", " +
      "\"probability\": 0.9}\n" +
      "  ]\n" +
      "}\n" +
      "文本:\n%s",
      content.substring(0, Math.min(3000, content.length()))
    );
    
    String result = aiService.chatJson(prompt, "hook_extraction");
    saveExtractedElements(importLogId, chapterNum, "hook", result);
  }
  
  // 步骤4: 冲突检测 (针对现有小说)
  public List<ImportConflict> detectConflicts(Long importLogId,
                                             List<ImportedChapter> chapters) {
    List<ImportConflict> conflicts = new ArrayList<>();
    
    TextImportLog log = importRepo.findById(importLogId)
      .orElseThrow();
    
    if (log.getNovelId() != null) {
      // 检测与现有世界观冲突
      conflicts.addAll(
        checkWorldViewConflicts(log.getNovelId(), chapters)
      );
      
      // 检测与现有角色冲突
      conflicts.addAll(
        checkCharacterConflicts(log.getNovelId(), chapters)
      );
      
      // 检测与现有场景冲突
      conflicts.addAll(
        checkSceneConflicts(log.getNovelId(), chapters)
      );
    }
    
    return conflicts;
  }
  
  // 步骤5: 最终导入
  @Transactional
  public void finalizeImport(Long importLogId, 
                            ImportConfiguration config) {
    TextImportLog log = importRepo.findById(importLogId)
      .orElseThrow();
    
    try {
      if (config.isNewNovel()) {
        // 创建新小说
        createNovelFromImport(log, config);
      } else {
        // 补充到现有小说
        appendToExistingNovel(log, config);
      }
      
      log.setStatus("completed");
      log.setCompletedAt(LocalDateTime.now());
    } catch (Exception e) {
      log.setStatus("failed");
      log.setErrorMessage(e.getMessage());
    }
    
    importRepo.save(log);
  }
}
```

---

### 需求4：增强AI续写能力

#### 问题分析
- **现状**：续写功能存在但缺乏多维约束
- **问题**：
  - 续写易偏离世界观设定
  - 场景状态切换不连贯
  - 目标效果控制不精准
  - 无法保证逻辑一致性

#### 优化方案

**4.1 多维约束融合框架**

```
┌──────────────────────────────────────────────┐
│         续写请求处理流程                      │
├──────────────────────────────────────────────┤
│                                              │
│  1. 加载上下文 (Context Loading)            │
│     ├─ 前文内容 (lastChapterContent)        │
│     ├─ 当前场景 (currentScene)              │
│     ├─ 相关角色 (activeCharacters)          │
│     └─ 世界观 (worldViewConstraints)        │
│                                              │
│  2. 构建约束条件 (Constraint Building)      │
│     ├─ 世界观约束                            │
│     │  ├─ 魔法/科技规则                      │
│     │  ├─ 社会法则                          │
│     │  └─ 禁忌事项                          │
│     ├─ 场景约束                              │
│     │  ├─ 当前位置和环境                    │
│     │  ├─ 场景属性 (气氛/温度/时间)        │
│     │  └─ 在场人物列表                      │
│     ├─ 角色约束                              │
│     │  ├─ 角色属性和能力                    │
│     │  ├─ 当前情绪状态                      │
│     │  └─ 性格特征                          │
│     ├─ 章节约束                              │
│     │  ├─ 目标字数范围 (1000±200)          │
│     │  ├─ 节奏要求 (快速/缓慢/高潮)        │
│     │  └─ 情感曲线                          │
│     └─ 大纲约束                              │
│        ├─ 接下来的关键事件                  │
│        └─ 不应触发的事件                    │
│                                              │
│  3. 优化Prompt生成 (Prompt Engineering)    │
│     └─ 将约束条件序列化为详细指令          │
│                                              │
│  4. AI续写 (AI Generation)                  │
│     ├─ 流式输出 (Streaming)                 │
│     └─ 实时字数统计                          │
│                                              │
│  5. 约束验证后处理 (Post-Processing)        │
│     ├─ 字数校准                              │
│     ├─ 约束冲突检测                          │
│     ├─ 逻辑连贯性检查                        │
│     └─ 自动修正或用户手动调整              │
│                                              │
│  6. 反馈与优化 (Feedback)                   │
│     ├─ 用户反馈 (质量评分)                  │
│     └─ 微调约束条件                          │
│                                              │
└──────────────────────────────────────────────┘
```

**4.2 Prompt工程优化**

```javascript
// 动态Prompt生成示例

const generateContinuationPrompt = (context) => {
  const {
    lastContent,      // 前文末尾 (200字)
    worldView,       // 世界观约束
    currentScene,    // 场景信息
    characters,      // 角色信息
    chapterGoal,     // 章节目标
    outlineNext      // 大纲下一步
  } = context;

  return `你是一位专业的网络文学创作者。

【世界观约束】
${formatWorldViewConstraints(worldView)}

【当前场景】
${formatSceneContext(currentScene)}

【在场人物及状态】
${formatCharacterContext(characters)}

【创作目标与要求】
- 字数范围: ${chapterGoal.wordCountMin}-${chapterGoal.wordCountMax}字 (严格遵守)
- 节奏要求: ${chapterGoal.rhythmType} (${chapterGoal.rhythmDescription})
- 情感走向: ${chapterGoal.emotionalCurve}
- 下一关键事件: ${outlineNext}

【前文末尾】
${lastContent}

【续写要求】
1. 严格遵守世界观约束，任何魔法/科技/社会规则都要符合设定
2. 保持场景的连贯性，不突兀切换场景或时间
3. 角色行为符合性格设定和当前情绪状态
4. 字数恰好在${chapterGoal.wordCountMin}-${chapterGoal.wordCountMax}字之间
5. ${chapterGoal.rhythmDescription}
6. 朝${outlineNext}推进，但不要一步到位
7. 加强[环境氛围/心理刻画/情感表达]的细节描写

【禁止事项】
- 违反世界观中的规则
- 突然改变角色性格或已有决定
- 过快达到预定目标 (留足后文空间)
- 逻辑跳跃或设定矛盾

请开始续写:`;
};
```

**4.3 实时字数反馈**

```java
// StreamingContinuationController.java
@PostMapping("/continuations/stream")
public void streamContinuation(
    @RequestBody ContinuationRequest req,
    HttpServletResponse response) throws IOException {
  
  response.setContentType("text/event-stream");
  response.setCharacterEncoding("UTF-8");
  response.setHeader("Cache-Control", "no-cache");
  
  PrintWriter writer = response.getWriter();
  
  try (SseEmitter emitter = new SseEmitter()) {
    // 异步处理续写
    continuationService.generateAndStream(
      req,
      new ContinuationStreamListener() {
        
        @Override
        public void onChunkReceived(String chunk, int totalWords) {
          try {
            writer.write("data: ");
            writer.write(objectMapper.writeValueAsString(
              new StreamEvent(
                "content",
                chunk,
                totalWords,
                calculateMetrics(chunk)
              )
            ));
            writer.write("\n\n");
            writer.flush();
          } catch (IOException e) {
            log.error("Stream write error", e);
          }
        }
        
        @Override
        public void onConstraintViolation(ConstraintViolation violation) {
          try {
            writer.write("data: ");
            writer.write(objectMapper.writeValueAsString(
              new StreamEvent(
                "constraint_warning",
                violation.getMessage(),
                null,
                null
              )
            ));
            writer.write("\n\n");
            writer.flush();
          } catch (IOException e) {
            log.error("Stream write error", e);
          }
        }
        
        @Override
        public void onComplete(String fullContent, 
                              ContinuationMetrics metrics) {
          try {
            writer.write("data: ");
            writer.write(objectMapper.writeValueAsString(
              new StreamEvent(
                "complete",
                fullContent,
                metrics.getTotalWords(),
                metrics
              )
            ));
            writer.write("\n\n");
            writer.flush();
          } catch (IOException e) {
            log.error("Stream write error", e);
          } finally {
            writer.close();
          }
        }
      }
    );
  }
}

// 前端侦听流
async function streamContinuation(req) {
  const response = await fetch('/api/continuations/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req)
  });
  
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n\n');
    
    buffer = lines[lines.length - 1];
    
    for (let i = 0; i < lines.length - 1; i++) {
      const line = lines[i];
      if (line.startsWith('data: ')) {
        const event = JSON.parse(line.slice(6));
        
        if (event.type === 'content') {
          // 追加内容
          appendContent(event.chunk);
          // 更新字数显示
          updateWordCount(event.totalWords);
        } else if (event.type === 'constraint_warning') {
          // 显示约束警告
          showConstraintWarning(event.message);
        } else if (event.type === 'complete') {
          // 完成
          finalizeContinuation(event);
        }
      }
    }
  }
}
```

---

### 需求5：场景节奏管理优化

#### 问题分析
- **现状**：无专门的节奏管理模块
- **问题**：
  - 续写容易超出/不足字数
  - 节奏设定无法精确控制
  - 无实时反馈机制
  - 情节跳跃难以检测

#### 优化方案

**5.1 节奏可视化管理**

数据模型：
```sql
-- 场景节奏配置
CREATE TABLE scene_rhythm_config (
  id BIGINT PRIMARY KEY,
  scene_id BIGINT,
  target_word_count INT, -- 目标字数
  word_count_tolerance INT DEFAULT 200, -- 容差 ±200
  rhythm_type VARCHAR(50), -- "action" / "dialogue" / "description" / "introspection"
  rhythm_description TEXT,
  short_sentence_ratio INT, -- 短句占比 0-100
  long_sentence_ratio INT,
  action_ratio INT,
  dialogue_ratio INT,
  description_ratio INT,
  emotional_curve TEXT, -- JSON: [{chapter: 1, emotion: "紧张", intensity: 8}]
  pacing_transitions TEXT, -- 节奏转变规则
  created_at TIMESTAMP
);

-- 节奏验证记录
CREATE TABLE rhythm_validation_record (
  id BIGINT PRIMARY KEY,
  chapter_id BIGINT,
  scene_id BIGINT,
  target_word_count INT,
  actual_word_count INT,
  word_count_deviation INT,
  sentence_length_analysis JSON,
  emotional_continuity BOOLEAN,
  pacing_smooth BOOLEAN,
  violations TEXT, -- JSON: 节奏问题列表
  validated_at TIMESTAMP
);
```

前端设计：
```vue
<!-- SceneRhythmManager.vue -->
<template>
  <div class="rhythm-manager">
    <!-- 上: 导航 -->
    <div class="header">
      <select v-model="selectedChapter" @change="loadChapterScenes">
        <option>选择章节</option>
      </select>
      <button @click="addScene">+ 新增场景</button>
    </div>

    <!-- 中: 可视化曲线 -->
    <div class="visualization">
      <div class="rhythm-chart">
        <!-- ECharts 曲线图 -->
        <echarts-component 
          :option="rhythmChartOption"
          style="height: 300px"
        />
      </div>
    </div>

    <!-- 下: 场景列表 -->
    <div class="scene-list">
      <div v-for="scene in scenes" :key="scene.id" class="scene-row">
        <div class="scene-name">{{ scene.name }}</div>
        <div class="word-count">
          <input v-model.number="scene.config.targetWordCount" />
          <span class="status" :class="getWordCountStatus(scene)">
            {{ scene.config.targetWordCount - 200 }}-
            {{ scene.config.targetWordCount + 200 }}
          </span>
        </div>
        <div class="rhythm-type">
          <select v-model="scene.config.rhythmType">
            <option value="action">快速推进 (短句/对话多)</option>
            <option value="description">细致描写 (长句/描写多)</option>
            <option value="introspection">内心戏 (独白/心理多)</option>
            <option value="dialogue">对话场景 (对话为主)</option>
          </select>
        </div>
        <div class="emotional-tone">
          <input v-model="scene.config.emotionalCurve" 
                 placeholder="如: 紧张→期待→释然" />
        </div>
        <div class="actions">
          <button @click="editScene(scene)">编辑</button>
          <button @click="validateScene(scene)">验证</button>
        </div>
      </div>
    </div>

    <!-- 右: 详细编辑面板 -->
    <div v-if="selectedScene" class="detail-editor">
      <h3>{{ selectedScene.name }} - 详细配置</h3>
      
      <div class="config-section">
        <label>字数范围:</label>
        <div class="slider-group">
          <input type="range" v-model="selectedScene.config.targetWordCount"
                 min="500" max="5000" step="100" />
          <input type="number" v-model.number="selectedScene.config.targetWordCount" />
        </div>
        <span class="hint">
          容差: ±{{ selectedScene.config.wordCountTolerance }}
        </span>
      </div>

      <div class="config-section">
        <label>句式分布:</label>
        <div class="slider-group">
          <label>短句占比:</label>
          <input type="range" v-model.number="selectedScene.config.shortSentenceRatio"
                 min="0" max="100" step="5" />
          <span>{{ selectedScene.config.shortSentenceRatio }}%</span>
        </div>
        <div class="slider-group">
          <label>长句占比:</label>
          <input type="range" v-model.number="selectedScene.config.longSentenceRatio"
                 min="0" max="100" step="5" />
          <span>{{ selectedScene.config.longSentenceRatio }}%</span>
        </div>
      </div>

      <div class="config-section">
        <label>内容分布:</label>
        <div class="distribution">
          <div>行动: {{ selectedScene.config.actionRatio }}%</div>
          <div>对话: {{ selectedScene.config.dialogueRatio }}%</div>
          <div>描写: {{ selectedScene.config.descriptionRatio }}%</div>
        </div>
      </div>

      <div class="config-section">
        <label>情感走向:</label>
        <textarea v-model="selectedScene.config.emotionalCurve"
                  placeholder="如: 【开始】平静 → 【中部】紧张升温 → 【高潮】极度紧张 → 【结尾】释然"
        />
      </div>

      <div class="conflict-check">
        <h4>⚠️ 连贯性检查</h4>
        <ul v-for="conflict in selectedScene.conflicts" :key="conflict.id">
          <li>{{ conflict.description }}</li>
        </ul>
      </div>

      <button @click="saveSceneConfig">保存配置</button>
    </div>
  </div>
</template>
```

**5.2 后端约束验证**

```java
// SceneRhythmService.java
@Service
@RequiredArgsConstructor
public class SceneRhythmService {
  
  private final SceneRhythmConfigRepository rhythmConfigRepo;
  private final ChapterRepository chapterRepo;
  private final SceneRepository sceneRepo;
  private final ConstraintEngineService constraintEngine;
  private final AiService aiService;
  
  // 保存节奏配置
  @Transactional
  public SceneRhythmConfig saveRhythmConfig(
      Long sceneId, 
      SceneRhythmConfigRequest req) {
    
    SceneRhythmConfig config = rhythmConfigRepo
      .findBySceneId(sceneId)
      .orElseGet(() -> {
        SceneRhythmConfig c = new SceneRhythmConfig();
        c.setSceneId(sceneId);
        return c;
      });
    
    config.setTargetWordCount(req.getTargetWordCount());
    config.setWordCountTolerance(req.getWordCountTolerance());
    config.setRhythmType(req.getRhythmType());
    config.setRhythmDescription(req.getRhythmDescription());
    config.setShortSentenceRatio(req.getShortSentenceRatio());
    config.setLongSentenceRatio(req.getLongSentenceRatio());
    config.setActionRatio(req.getActionRatio());
    config.setDialogueRatio(req.getDialogueRatio());
    config.setDescriptionRatio(req.getDescriptionRatio());
    config.setEmotionalCurve(req.getEmotionalCurve());
    
    return rhythmConfigRepo.save(config);
  }
  
  // 验证章节节奏是否连贯
  public RhythmValidationResult validateChapterRhythm(Long chapterId) {
    Chapter chapter = chapterRepo.findById(chapterId)
      .orElseThrow();
    
    List<SceneRhythmConfig> configs = rhythmConfigRepo
      .findByChapterId(chapterId);
    
    RhythmValidationResult result = new RhythmValidationResult();
    
    // 1. 验证各场景字数
    for (SceneRhythmConfig config : configs) {
      Scene scene = sceneRepo.findById(config.getSceneId())
        .orElseThrow();
      
      int actualWordCount = scene.getDescription().length();
      
      if (Math.abs(actualWordCount - config.getTargetWordCount()) 
          > config.getWordCountTolerance()) {
        result.addViolation(
          "字数超差",
          String.format(
            "%s: 目标%d字, 实际%d字, 超差%d字",
            scene.getName(),
            config.getTargetWordCount(),
            actualWordCount,
            actualWordCount - config.getTargetWordCount()
          )
        );
      }
    }
    
    // 2. 验证整章节奏平滑度
    result.setPacingSmooth(
      checkPacingContinuity(chapter, configs)
    );
    
    // 3. 验证情感连贯性
    result.setEmotionalContinuity(
      checkEmotionalContinuity(chapter, configs)
    );
    
    return result;
  }
  
  private boolean checkPacingContinuity(Chapter chapter,
                                       List<SceneRhythmConfig> configs) {
    // 检查场景间节奏是否过度跳跃
    // 如: 快速推进 → 细致描写 的转变是否合理
    
    for (int i = 0; i < configs.size() - 1; i++) {
      SceneRhythmConfig current = configs.get(i);
      SceneRhythmConfig next = configs.get(i + 1);
      
      // 计算节奏差异
      int rhythmDifference = 
        calculateRhythmDifference(current, next);
      
      // 若差异过大 (>50) 则需要检查过渡是否合理
      if (rhythmDifference > 50) {
        String prompt = String.format(
          "判断以下场景过渡是否合理:\n" +
          "场景1: %s (%s)\n" +
          "场景2: %s (%s)\n" +
          "是否存在突兀感？(是/否)",
          current.getRhythmDescription(),
          current.getRhythmType(),
          next.getRhythmDescription(),
          next.getRhythmType()
        );
        
        String response = aiService.chat(prompt);
        if (response.contains("是")) {
          return false;
        }
      }
    }
    
    return true;
  }
  
  private boolean checkEmotionalContinuity(Chapter chapter,
                                          List<SceneRhythmConfig> configs) {
    // 检查各场景的情感走向是否连贯
    
    List<String> emotionalCurves = configs.stream()
      .map(SceneRhythmConfig::getEmotionalCurve)
      .collect(toList());
    
    String prompt = String.format(
      "检查以下场景序列的情感走向是否连贯:\n%s\n" +
      "是否存在突兀的情感转变?(是/否)",
      String.join(" → ", emotionalCurves)
    );
    
    String response = aiService.chat(prompt);
    return !response.contains("是");
  }
  
  // 根据节奏配置生成续写约束
  public String buildRhythmConstraint(SceneRhythmConfig config) {
    return String.format(
      "【节奏约束】\n" +
      "- 字数严格控制: %d字 (容差±%d字)\n" +
      "- 节奏类型: %s (%s)\n" +
      "- 短句占比: %d%%, 长句占比: %d%%\n" +
      "- 行动/对话/描写 = %d/%d/%d\n" +
      "- 情感曲线: %s\n",
      config.getTargetWordCount(),
      config.getWordCountTolerance(),
      config.getRhythmType(),
      config.getRhythmDescription(),
      config.getShortSentenceRatio(),
      config.getLongSentenceRatio(),
      config.getActionRatio(),
      config.getDialogueRatio(),
      config.getDescriptionRatio(),
      config.getEmotionalCurve()
    );
  }
}
```

---

### 需求6：AI描写能力强化

#### 问题分析
- **现状**：AI生成内容质量波动，缺乏细节雕琢
- **问题**：
  - 环境描写缺乏沉浸感
  - 人物心理刻画浅表
  - 情绪表达不够细腻
  - 节奏变化不足

#### 优化方案

**6.1 描写增强模块架构**

```java
// EnhancedDescriptionService.java
@Service
@RequiredArgsConstructor
public class EnhancedDescriptionService {
  
  private final AiService aiService;
  private final DescriptionPromptBuilder promptBuilder;
  
  // 生成细致的环境描写
  public String generateAtmosphereDescription(
      AtmosphereContext ctx) {
    
    String prompt = promptBuilder.buildAtmospherePrompt(
      ctx.getLocation(),
      ctx.getTimeOfDay(),
      ctx.getWeather(),
      ctx.getMood(),
      ctx.getScenetype()
    );
    
    String description = aiService.chat(prompt);
    
    // 后处理: 增强细节
    description = enhanceWithSensoryDetails(description);
    
    return description;
  }
  
  // 生成深层心理刻画
  public String generatePsychologicalDescription(
      CharacterPsychologyContext ctx) {
    
    String prompt = promptBuilder.buildPsychologyPrompt(
      ctx.getCharacterName(),
      ctx.getCharacterTraits(),
      ctx.getCurrentEmotion(),
      ctx.getInnerConflict(),
      ctx.getMotivation(),
      ctx.getBackstory()
    );
    
    String description = aiService.chat(prompt);
    
    // 后处理: 增加心理层次感
    description = addPsychologicalLayers(description);
    
    return description;
  }
  
  // 生成情感表达序列 (短→长→短)
  public String generateEmotionalExpression(
      EmotionalExpressionContext ctx) {
    
    String prompt = promptBuilder.buildEmotionalPrompt(
      ctx.getEmotion(),
      ctx.getIntensity(),
      ctx.getCharacterPersonality(),
      ctx.getExpressionStyle()
    );
    
    String expression = aiService.chat(prompt);
    
    // 后处理: 调整句式长度
    expression = modulateSentenceLength(expression);
    
    return expression;
  }
}

// Prompt 构建器
@Component
public class DescriptionPromptBuilder {
  
  public String buildAtmospherePrompt(String location, String time,
                                     String weather, String mood,
                                     String sceneType) {
    return String.format(
      "你是一位擅长环境描写的文学大师。\n\n" +
      
      "【场景信息】\n" +
      "地点: %s\n" +
      "时间: %s\n" +
      "天气: %s\n" +
      "氛围: %s\n" +
      "类型: %s\n\n" +
      
      "【描写要求】\n" +
      "1. 从多个感官维度描写 (视觉/听觉/嗅觉/触觉/味觉)\n" +
      "2. 使用具体的意象和比喻，避免模糊描述\n" +
      "3. 通过环境来烘托氛围和角色情绪\n" +
      "4. 描写长度: 150-300字\n" +
      "5. 句式变化: 长句+短句结合\n" +
      "6. 细致传神，要有代入感\n\n" +
      
      "请生成符合上述要求的环境描写:",
      location, time, weather, mood, sceneType
    );
  }
  
  public String buildPsychologyPrompt(String charName,
                                     String traits,
                                     String emotion,
                                     String conflict,
                                     String motivation,
                                     String backstory) {
    return String.format(
      "你是一位擅长心理描写的文学家。\n\n" +
      
      "【人物信息】\n" +
      "姓名: %s\n" +
      "性格: %s\n" +
      "背景: %s\n\n" +
      
      "【当前心理状态】\n" +
      "表面情绪: %s\n" +
      "内心矛盾: %s\n" +
      "深层动机: %s\n\n" +
      
      "【心理描写要求】\n" +
      "1. 挖掘人物的真实想法，包括潜意识层面\n" +
      "2. 展现内心冲突和纠缠的过程\n" +
      "3. 通过细微动作/表情反映心理变化\n" +
      "4. 避免直白说教，要含蓄而深刻\n" +
      "5. 长度: 200-400字\n" +
      "6. 多角度呈现: 思维流+感受+判断\n\n" +
      
      "请生成符合要求的心理描写:",
      charName, traits, backstory, emotion, conflict, motivation
    );
  }
  
  public String buildEmotionalPrompt(String emotion,
                                    int intensity,
                                    String personality,
                                    String expressionStyle) {
    return String.format(
      "你是一位情感表达大师。\n\n" +
      
      "【表达参数】\n" +
      "情感: %s (强度: %d/10)\n" +
      "人物性格: %s\n" +
      "表达风格: %s\n\n" +
      
      "【表达规则】\n" +
      "1. 开始: 短句密集，制造节奏感\n" +
      "   示例: \"不可能。绝不可能。怎么会...\"\n" +
      "2. 过渡: 句式逐渐加长，深化表达\n" +
      "   示例: \"他的心开始下沉，仿佛有什么东西在崩塌。\"\n" +
      "3. 高潮: 长句表现情感的深度和复杂性\n" +
      "   示例: \"那一刻，他明白了，所有的挣扎和不甘都归于一个字：后悔。\"\n" +
      "4. 结尾: 短句收尾，形成有力的句号\n" +
      "   示例: \"一切都晚了。\"\n\n" +
      
      "请生成符合上述节奏的情感表达序列:",
      emotion, intensity, personality, expressionStyle
    );
  }
}

// 后处理增强
@Component
public class DescriptionEnhancer {
  
  public String enhanceWithSensoryDetails(String description) {
    // 检查各感官维度是否都有涉及
    // 若缺失则补充
    
    String[] senses = {"视觉", "听觉", "嗅觉", "触觉", "味觉"};
    List<String> missingSenses = new ArrayList<>();
    
    for (String sense : senses) {
      if (!description.contains(sense)) {
        missingSenses.add(sense);
      }
    }
    
    if (!missingSenses.isEmpty()) {
      String enhancePrompt = String.format(
        "在以下描写的基础上，增加%s的细节描写:\n%s\n\n" +
        "要求: 自然融合，不生硬。长度增加30-50字。",
        String.join("和", missingSenses),
        description
      );
      
      return aiService.chat(enhancePrompt);
    }
    
    return description;
  }
  
  public String addPsychologicalLayers(String description) {
    // 增加心理描写的层次
    // 1. 表面: 显而易见的想法
    // 2. 中层: 隐含的动机
    // 3. 深层: 潜意识的冲动
    
    String layerPrompt = String.format(
      "以下是人物的心理描写。请在保留原内容的基础上，" +
      "添加更深层的心理活动 (潜意识/原始欲望/真实恐惧):\n%s\n\n" +
      "要求: 用括号标注新增内容的心理层级。",
      description
    );
    
    return aiService.chat(layerPrompt);
  }
  
  public String modulateSentenceLength(String text) {
    // 分析当前的句式长度分布
    // 调整为: 短→长→短 的有韵律的节奏
    
    String[] sentences = text.split("[。!！?？]");
    
    // 计算理想的句长调整
    String modulatePrompt = String.format(
      "调整以下文本的句式长度，实现节奏感:\n" +
      "1. 开始: 短句 (5-15字)\n" +
      "2. 中部: 长句 (30-50字)\n" +
      "3. 结尾: 短句 (5-15字)\n\n" +
      "原文本:\n%s\n\n" +
      "请重新组织句式，保持原意不变。",
      text
    );
    
    return aiService.chat(modulatePrompt);
  }
}
```

**6.2 描写优化工具链**

前端优化面板：
```vue
<!-- EnhancedDescriptionPanel.vue -->
<template>
  <div class="description-panel">
    <!-- 选项卡 -->
    <div class="tabs">
      <button v-for="tab in tabs" :key="tab.id"
              @click="activeTab = tab.id"
              :class="{ active: activeTab === tab.id }">
        {{ tab.label }}
      </button>
    </div>

    <!-- 环境描写优化 -->
    <div v-if="activeTab === 'atmosphere'" class="tab-content">
      <div class="form-group">
        <label>地点:</label>
        <input v-model="atmosphereContext.location" />
      </div>
      <div class="form-group">
        <label>时间:</label>
        <select v-model="atmosphereContext.timeOfDay">
          <option>清晨</option>
          <option>上午</option>
          <option>正午</option>
          <option>下午</option>
          <option>傍晚</option>
          <option>夜晚</option>
        </select>
      </div>
      <div class="form-group">
        <label>氛围:</label>
        <select v-model="atmosphereContext.mood">
          <option>紧张</option>
          <option>温馨</option>
          <option>诡异</option>
          <option>荒凉</option>
          <option>热闹</option>
        </select>
      </div>
      <button @click="generateAtmosphere">生成环境描写</button>
      
      <div v-if="atmosphereResult" class="result">
        <p>{{ atmosphereResult }}</p>
        <button @click="insertAtmosphere">插入</button>
        <button @click="regenerateAtmosphere">重新生成</button>
      </div>
    </div>

    <!-- 心理描写优化 -->
    <div v-if="activeTab === 'psychology'" class="tab-content">
      <div class="form-group">
        <label>人物:</label>
        <select v-model="psychologyContext.characterId">
          <option v-for="char in characters" :key="char.id" 
                  :value="char.id">
            {{ char.name }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label>当前情绪:</label>
        <input v-model="psychologyContext.emotion" />
      </div>
      <div class="form-group">
        <label>内心矛盾:</label>
        <textarea v-model="psychologyContext.conflict" 
                  placeholder="描述人物的内心矛盾" />
      </div>
      <button @click="generatePsychology">生成心理描写</button>

      <div v-if="psychologyResult" class="result">
        <p>{{ psychologyResult }}</p>
        <button @click="insertPsychology">插入</button>
        <button @click="regeneratePsychology">重新生成</button>
      </div>
    </div>

    <!-- 情感表达优化 -->
    <div v-if="activeTab === 'emotion'" class="tab-content">
      <div class="form-group">
        <label>情感:</label>
        <select v-model="emotionContext.emotion">
          <option>惊喜</option>
          <option>愤怒</option>
          <option>悲伤</option>
          <option>恐惧</option>
          <option>失望</option>
        </select>
      </div>
      <div class="form-group">
        <label>强度: <span>{{ emotionContext.intensity }}/10</span></label>
        <input type="range" v-model.number="emotionContext.intensity"
               min="1" max="10" />
      </div>
      <button @click="generateEmotion">生成情感表达</button>

      <div v-if="emotionResult" class="result">
        <div class="emotion-stages">
          <div class="stage">
            <h4>短句开始</h4>
            <p>{{ emotionResult.shortOpening }}</p>
          </div>
          <div class="stage">
            <h4>长句深化</h4>
            <p>{{ emotionResult.longMiddle }}</p>
          </div>
          <div class="stage">
            <h4>短句收尾</h4>
            <p>{{ emotionResult.shortEnding }}</p>
          </div>
        </div>
        <button @click="insertEmotion">全部插入</button>
      </div>
    </div>
  </div>
</template>

<script setup>
// 实现逻辑...
</script>
```

---

## 技术架构设计

### 后端服务扩展图

```
┌─────────────────────────────────────────────────────┐
│                 SmartWritingController               │
├─────────────────────────────────────────────────────┤
│ 提供统一的AI创作API入口                              │
├─────────────────────────────────────────────────────┤

┌────────────────────────┐  ┌──────────────────────┐
│  WorldViewService      │  │ TextImportService    │
├────────────────────────┤  ├──────────────────────┤
│- upsert(worldView)     │  │- uploadFile()        │
│- validate()            │  │- autoChapterize()    │
│- getConstraints()      │  │- extractElements()   │
│- checkConflicts()      │  │- detectConflicts()   │
│                        │  │- finalizeImport()    │
└────────────────────────┘  └──────────────────────┘

┌────────────────────────────────────────┐
│     ConstraintEngineService            │
├────────────────────────────────────────┤
│- validateWorldViewConstraints()        │
│- validateSceneRhythm()                 │
│- validateCharacterConsistency()        │
│- validateOutlineAlignment()            │
│- validateContinuationQuality()         │
└────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│       EnhancedPromptEngine / PromptTemplates        │
├─────────────────────────────────────────────────────┤
│ 30+优化的Prompt模板                                 │
│ - 基于世界观的续写Prompt                            │
│ - 基于场景节奏的续写Prompt                          │
│ - 环境描写专化Prompt                                │
│ - 心理描写专化Prompt                                │
│ - 情感表达Prompt                                    │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│       EnhancedDescriptionService                    │
├─────────────────────────────────────────────────────┤
│- generateAtmosphereDescription()                    │
│- generatePsychologicalDescription()                 │
│- generateEmotionalExpression()                      │
│- enhanceDescriptionQuality()                        │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│       SmartContinuationService (增强)               │
├─────────────────────────────────────────────────────┤
│ 已有功能 + 新增:                                    │
│ - buildConstrainedContext()                         │
│ - validateAgainstConstraints()                      │
│ - streamWithRealTimeMetrics()                       │
│ - calibrateByWordCount()                            │
└─────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────┐
│       AiService (增强)                              │
├────────────────────────────────────────────────────┤
│ - chat() / chatJson() (已有)                        │
│ - stream() (新增, 流式响应)                        │
│ - validateConstraints() (新增)                      │
│ - extractElementsFromText() (新增)                  │
└────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│         Repository Layer (扩展)                      │
├─────────────────────────────────────────────────────┤
│- NovelWorldviewRepository                           │
│- WorldViewBackgroundRepository                      │
│- WorldViewRulesRepository                           │
│- WorldViewElementsRepository                        │
│- TextImportLogRepository                            │
│- SceneRhythmConfigRepository                        │
│- RhythmValidationRecordRepository                   │
│- ConstraintCheckRecordRepository                    │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│         Database Layer (MySQL 8.0)                  │
├─────────────────────────────────────────────────────┤
│ 新增7个表:                                          │
│ - novel_worldview (主表)                            │
│ - worldview_background (子表)                       │
│ - worldview_rules (子表)                            │
│ - worldview_elements (子表)                         │
│ - text_import_log                                   │
│ - scene_rhythm_config                               │
│ - worldview_constraint_checks                       │
└─────────────────────────────────────────────────────┘
```

---

## 数据模型扩展

### 新增表详细设计

**1. novel_worldview (世界观主表)**

```sql
CREATE TABLE novel_worldview (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  novel_id BIGINT NOT NULL UNIQUE,
  
  -- 基本信息
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE,
  
  -- 完整度指标
  background_completeness DECIMAL(3,2) DEFAULT 0,    -- 背景设定完整度
  rules_completeness DECIMAL(3,2) DEFAULT 0,          -- 规则完整度
  elements_completeness DECIMAL(3,2) DEFAULT 0,       -- 要素完整度
  overall_completeness DECIMAL(3,2) DEFAULT 0,        -- 总体完整度
  
  -- 约束强度配置
  constraint_level VARCHAR(20) DEFAULT 'medium', -- 'strict', 'medium', 'loose'
  
  FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
  KEY idx_novel_id (novel_id)
);

-- 2. worldview_background (背景设定子表)
CREATE TABLE worldview_background (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  worldview_id BIGINT NOT NULL,
  
  -- 分类
  category VARCHAR(50) NOT NULL, -- '时代背景', '地理环境', '社会结构', '历史设定', '技术水平'
  name VARCHAR(255) NOT NULL,
  
  -- 内容
  description TEXT NOT NULL,
  key_points TEXT, -- JSON: 关键要点列表
  visual_elements TEXT, -- JSON: 视觉要素用于AI生成
  
  -- 元数据
  importance INT DEFAULT 3, -- 1-5等级，影响约束强度
  constraint_level VARCHAR(20) DEFAULT 'reference', -- 'strict', 'flexible', 'reference'
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (worldview_id) REFERENCES novel_worldview(id) ON DELETE CASCADE,
  KEY idx_worldview_id (worldview_id),
  KEY idx_category (category)
);

-- 3. worldview_rules (规则体系子表)
CREATE TABLE worldview_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  worldview_id BIGINT NOT NULL,
  
  -- 分类
  rule_type VARCHAR(50) NOT NULL, -- '魔法系统', '科技规则', '社会法则', '禁忌事项', '能力限制'
  rule_name VARCHAR(255) NOT NULL,
  
  -- 规则定义
  description TEXT NOT NULL,
  application_scope TEXT, -- '全文', '特定角色', '特定地点'
  
  -- 约束条件 (结构化)
  constraints JSON, -- {"actor": "xx角色", "action": "xx行为", "consequence": "xx结果", "exception": "例外情况"}
  violation_consequence VARCHAR(200), -- 违反后果
  
  -- 元数据
  enforcement_level VARCHAR(20) DEFAULT 'strict', -- 'strict', 'flexible', 'suggestion'
  importance INT DEFAULT 5, -- 1-5, 影响续写生成
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (worldview_id) REFERENCES novel_worldview(id) ON DELETE CASCADE,
  KEY idx_worldview_id (worldview_id),
  KEY idx_rule_type (rule_type)
);

-- 4. worldview_elements (特殊要素子表)
CREATE TABLE worldview_elements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  worldview_id BIGINT NOT NULL,
  
  -- 分类
  element_type VARCHAR(50) NOT NULL, -- '种族', '文明', '物质', '能量体', '神秘力量'
  name VARCHAR(255) NOT NULL,
  
  -- 要素定义
  description TEXT NOT NULL,
  characteristics JSON, -- {属性列表}
  interaction_rules TEXT, -- 与其他要素的互动规则
  
  -- 影响评估
  plot_influence INT DEFAULT 3, -- 1-5, 对情节的影响程度
  world_building_importance INT DEFAULT 3, -- 1-5, 对世界设定的重要性
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (worldview_id) REFERENCES novel_worldview(id) ON DELETE CASCADE,
  KEY idx_worldview_id (worldview_id),
  KEY idx_element_type (element_type)
);

-- 5. worldview_constraint_checks (约束检验记录表)
CREATE TABLE worldview_constraint_checks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  novel_id BIGINT NOT NULL,
  
  -- 检验目标
  check_target VARCHAR(50) NOT NULL, -- 'outline', 'scene', 'character', 'continuation', 'overall'
  target_id BIGINT,
  target_description VARCHAR(500), -- 用于参考
  
  -- 检验结果
  violations JSON, -- [{rule: "规则名", violation: "冲突描述", severity: "critical/warning/info"}]
  violation_count INT DEFAULT 0,
  
  severity VARCHAR(20) DEFAULT 'info', -- 'critical', 'warning', 'info'
  
  -- 审计信息
  last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  checked_by VARCHAR(100) DEFAULT 'system',
  
  FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
  KEY idx_novel_id (novel_id),
  KEY idx_check_target (check_target),
  KEY idx_last_checked (last_checked)
);

-- 6. text_import_log (文本导入日志)
CREATE TABLE text_import_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  novel_id BIGINT,
  
  -- 文件信息
  original_filename VARCHAR(255) NOT NULL,
  file_size INT NOT NULL,
  encoding VARCHAR(20) DEFAULT 'UTF-8',
  
  -- 处理统计
  total_chapters INT,
  extracted_outlines INT,
  extracted_scenes INT,
  extracted_characters INT,
  extracted_hooks INT,
  conflicts_detected INT,
  
  -- 状态
  status VARCHAR(20) DEFAULT 'processing', -- 'processing', 'completed', 'failed'
  error_message TEXT,
  
  -- 时间戳
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP,
  
  FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE SET NULL,
  KEY idx_novel_id (novel_id),
  KEY idx_status (status)
);

-- 7. scene_rhythm_config (场景节奏配置)
CREATE TABLE scene_rhythm_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  scene_id BIGINT NOT NULL UNIQUE,
  
  -- 字数配置
  target_word_count INT NOT NULL DEFAULT 2000,
  word_count_tolerance INT DEFAULT 200, -- 允许容差
  
  -- 节奏类型
  rhythm_type VARCHAR(50) NOT NULL DEFAULT 'balanced', -- 'action', 'description', 'introspection', 'dialogue', 'balanced'
  rhythm_description TEXT,
  
  -- 句式分布 (百分比)
  short_sentence_ratio INT DEFAULT 30, -- 短句占比
  long_sentence_ratio INT DEFAULT 40,  -- 长句占比
  medium_sentence_ratio INT DEFAULT 30, -- 中等句占比
  
  -- 内容分布 (百分比)
  action_ratio INT DEFAULT 25,
  dialogue_ratio INT DEFAULT 25,
  description_ratio INT DEFAULT 25,
  introspection_ratio INT DEFAULT 25,
  
  -- 情感曲线
  emotional_curve TEXT, -- 如: '平静 → 紧张 → 高潮 → 释然'
  emotional_keywords TEXT, -- JSON: 关键情感词
  
  -- 节奏转变规则
  pacing_transitions TEXT,
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE,
  KEY idx_scene_id (scene_id)
);
```

---

## 实现路线图

### Phase 1: 基础设施 (第1-2周)

- ✅ 数据库扩展 (7个新表)
- ✅ Entity和Repository层代码生成
- ✅ 基础Controller框架
- ✅ 前端基础页面组件

### Phase 2: 世界观模块 (第3周)

- ✅ WorldViewService完整实现
- ✅ ConstraintEngine初步实现
- ✅ 世界观编辑页面UI
- ✅ 约束检验功能

### Phase 3: 文本导入模块 (第4周)

- ✅ TextImportService核心逻辑
- ✅ 文本预处理和分章算法
- ✅ 关键要素提取AI集成
- ✅ 导入UI和进度反馈

### Phase 4: 小说创建强化 (第5周)

- ✅ 表单验证和必填项
- ✅ AI推荐集成
- ✅ 前端交互优化

### Phase 5: AI续写增强 (第5-6周)

- ✅ 多维约束融合
- ✅ Prompt优化
- ✅ 流式响应实现
- ✅ 实时字数反馈

### Phase 6: 场景节奏管理 (第6-7周)

- ✅ 节奏可视化UI
- ✅ 节奏验证算法
- ✅ 冲突检测

### Phase 7: 描写能力强化 (第7-8周)

- ✅ 描写增强Service
- ✅ Prompt模板库完善
- ✅ 后处理优化

### Phase 8: 测试和优化 (第8周)

- ✅ 集成测试
- ✅ 性能优化
- ✅ 文档完善

---

## 核心模块详设

### API设计示例

```java
// ============ 世界观相关API ============

// GET /api/worldview/{novelId}
// 获取小说的世界观设定

// POST /api/worldview/{novelId}/backgrounds
// 添加背景设定

// PUT /api/worldview/{novelId}/rules/{ruleId}
// 更新规则

// POST /api/worldview/{novelId}/validate
// 验证世界观与其他模块的一致性

// ============ 文本导入API ============

// POST /api/text-import/upload
// 上传文本文件

// GET /api/text-import/{importId}/preview
// 预览导入结果

// POST /api/text-import/{importId}/finalize
// 完成导入

// ============ 续写约束API ============

// POST /api/continuations/stream-with-constraints
// 基于约束的流式续写

// POST /api/continuations/validate
// 验证续写是否符合约束

// ============ 场景节奏API ============

// POST /api/scene-rhythm/{sceneId}/config
// 配置场景节奏

// GET /api/chapter/{chapterId}/rhythm-validation
// 获取整章节奏验证结果

// ============ 描写增强API ============

// POST /api/description/atmosphere
// 生成环境描写

// POST /api/description/psychology
// 生成心理描写

// POST /api/description/emotion
// 生成情感表达
```

---

这份规划文档为期5-8周的优化项目提供了完整的技术方案和实现路径。

**建议下一步**:
1. 确认各需求的优先级
2. 组织技术评审，确认方案可行性
3. 分配开发资源和时间表
4. 启动Phase 1基础设施建设
