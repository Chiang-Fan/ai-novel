# 🔧 AI小说创作系统优化 - 技术规范文档

---

## 1. 后端技术规范

### 1.1 Java/Spring Boot编码规范

#### 实体类规范 (Entity)

```java
/**
 * 世界观设定实体类
 */
@Entity
@Table(name = "novel_worldview", uniqueConstraints = {
    @UniqueConstraint(columnNames = "novel_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelWorldview extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long novelId;
    
    @Column(columnDefinition = "DECIMAL(3,2) DEFAULT 0")
    private BigDecimal backgroundCompleteness; // 完整度百分比
    
    @Column(columnDefinition = "DECIMAL(3,2) DEFAULT 0")
    private BigDecimal rulesCompleteness;
    
    @Column(columnDefinition = "DECIMAL(3,2) DEFAULT 0")
    private BigDecimal elementsCompleteness;
    
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'medium'")
    @Enumerated(EnumType.STRING)
    private ConstraintLevel constraintLevel; // 约束强度
    
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;
    
    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    
    // 关联关系
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Novel novel;
    
    @OneToMany(mappedBy = "worldview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorldViewBackground> backgrounds = new ArrayList<>();
    
    @OneToMany(mappedBy = "worldview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorldViewRules> rules = new ArrayList<>();
    
    @OneToMany(mappedBy = "worldview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorldViewElements> elements = new ArrayList<>();
    
    /**
     * 计算总体完整度
     */
    public BigDecimal calculateOverallCompleteness() {
        return backgroundCompleteness
            .add(rulesCompleteness)
            .add(elementsCompleteness)
            .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
    }
}

// 约束强度枚举
public enum ConstraintLevel {
    STRICT("严格约束"),      // 违反会导致内容生成失败
    MEDIUM("中等约束"),      // 违反会生成警告
    LOOSE("宽松约束");       // 违反仅提示建议
    
    private final String description;
    
    ConstraintLevel(String description) {
        this.description = description;
    }
}
```

#### Repository规范

```java
/**
 * 世界观Repository
 * 使用自定义查询和分页
 */
@Repository
public interface NovelWorldviewRepository extends JpaRepository<NovelWorldview, Long> {
    
    Optional<NovelWorldview> findByNovelId(Long novelId);
    
    // 自定义查询：找出完整度不足的世界观
    @Query("SELECT nw FROM NovelWorldview nw " +
           "WHERE nw.overallCompleteness < 0.8 " +
           "AND nw.isActive = TRUE " +
           "ORDER BY nw.updatedAt DESC")
    List<NovelWorldview> findIncompleteWorldviews();
    
    // 统计约束严格程度
    @Query("SELECT COUNT(nw) FROM NovelWorldview nw " +
           "WHERE nw.constraintLevel = :level " +
           "AND nw.isActive = TRUE")
    Long countByConstraintLevel(@Param("level") ConstraintLevel level);
}

/**
 * 世界观背景Repository
 */
@Repository
public interface WorldViewBackgroundRepository extends JpaRepository<WorldViewBackground, Long> {
    
    List<WorldViewBackground> findByWorldviewIdOrderByImportanceDesc(Long worldviewId);
    
    @Query("SELECT wvb FROM WorldViewBackground wvb " +
           "WHERE wvb.worldview.id = :worldviewId " +
           "AND wvb.category = :category")
    List<WorldViewBackground> findByWorldviewAndCategory(
        @Param("worldviewId") Long worldviewId,
        @Param("category") String category
    );
}
```

#### Service规范

```java
/**
 * 世界观业务逻辑层
 * 使用严格的错误处理和事务管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorldViewService {
    
    private final NovelWorldviewRepository worldViewRepo;
    private final WorldViewBackgroundRepository bgRepo;
    private final WorldViewRulesRepository rulesRepo;
    private final WorldViewElementsRepository elemRepo;
    private final ConstraintEngineService constraintEngine;
    private final AiService aiService;
    private final NovelRepository novelRepo;
    
    /**
     * 创建或更新小说的世界观
     * 
     * @param novelId 小说ID
     * @param request 世界观请求
     * @return 更新后的世界观
     * @throws EntityNotFoundException 小说不存在
     */
    @Transactional
    public WorldViewResponse upsertWorldView(
        Long novelId,
        WorldViewRequest request
    ) {
        log.info("开始更新小说[{}]的世界观", novelId);
        
        // 验证小说存在
        Novel novel = novelRepo.findById(novelId)
            .orElseThrow(() -> new EntityNotFoundException(
                String.format("小说不存在: %d", novelId)
            ));
        
        // 查询或创建世界观
        NovelWorldview worldView = worldViewRepo
            .findByNovelId(novelId)
            .orElseGet(() -> {
                NovelWorldview nw = NovelWorldview.builder()
                    .novelId(novelId)
                    .constraintLevel(ConstraintLevel.MEDIUM)
                    .isActive(true)
                    .build();
                return worldViewRepo.save(nw);
            });
        
        // 保存各部分
        saveBackgrounds(worldView, request.getBackgrounds());
        saveRules(worldView, request.getRules());
        saveElements(worldView, request.getElements());
        
        // 计算完整度
        updateCompleteness(worldView);
        
        log.info("成功更新小说[{}]的世界观", novelId);
        
        return toResponse(worldViewRepo.save(worldView));
    }
    
    /**
     * 验证世界观与其他模块的一致性
     * 
     * @param novelId 小说ID
     * @return 验证结果
     */
    @Transactional(readOnly = true)
    public WorldViewConstraintCheckResult validateConstraints(Long novelId) {
        log.info("开始验证小说[{}]的世界观约束", novelId);
        
        NovelWorldview worldView = worldViewRepo.findByNovelId(novelId)
            .orElseThrow(() -> new EntityNotFoundException(
                String.format("世界观不存在: novelId=%d", novelId)
            ));
        
        WorldViewConstraintCheckResult result = new WorldViewConstraintCheckResult();
        
        // 检验各模块
        result.setOutlineViolations(
            constraintEngine.checkOutlineAgainstWorldView(novelId, worldView)
        );
        result.setSceneViolations(
            constraintEngine.checkScenesAgainstWorldView(novelId, worldView)
        );
        result.setCharacterViolations(
            constraintEngine.checkCharactersAgainstWorldView(novelId, worldView)
        );
        result.setContinuationViolations(
            constraintEngine.checkContinuationsAgainstWorldView(novelId, worldView)
        );
        
        // 综合判断
        boolean isValid = result.getTotalViolations() == 0 ||
                         (result.getTotalWarnings() == 0 && 
                          worldView.getConstraintLevel() != ConstraintLevel.STRICT);
        result.setValid(isValid);
        
        log.info("验证完成: novelId={}, valid={}", novelId, isValid);
        
        return result;
    }
    
    private void saveBackgrounds(NovelWorldview worldView, 
                                List<WorldViewBackgroundRequest> requests) {
        if (requests == null || requests.isEmpty()) return;
        
        // 删除已删除的项
        worldView.getBackgrounds().clear();
        
        // 添加新项
        for (WorldViewBackgroundRequest req : requests) {
            WorldViewBackground bg = WorldViewBackground.builder()
                .worldview(worldView)
                .category(req.getCategory())
                .name(req.getName())
                .description(req.getDescription())
                .importance(req.getImportance())
                .constraintLevel(req.getConstraintLevel())
                .build();
            worldView.getBackgrounds().add(bg);
        }
    }
    
    private void updateCompleteness(NovelWorldview worldView) {
        int bgCount = worldView.getBackgrounds().isEmpty() ? 0 : 1;
        int ruleCount = worldView.getRules().isEmpty() ? 0 : 1;
        int elemCount = worldView.getElements().isEmpty() ? 0 : 1;
        
        BigDecimal bgCompleteness = BigDecimal.valueOf(
            Math.min(worldView.getBackgrounds().size() * 25, 100)
        ).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // 类似计算其他完整度...
        
        worldView.setBackgroundCompleteness(bgCompleteness);
    }
    
    private WorldViewResponse toResponse(NovelWorldview worldView) {
        return WorldViewResponse.builder()
            .id(worldView.getId())
            .novelId(worldView.getNovelId())
            .backgroundCompleteness(worldView.getBackgroundCompleteness())
            .rulesCompleteness(worldView.getRulesCompleteness())
            .elementsCompleteness(worldView.getElementsCompleteness())
            .overallCompleteness(worldView.calculateOverallCompleteness())
            .constraintLevel(worldView.getConstraintLevel().name())
            .backgrounds(worldView.getBackgrounds().stream()
                .map(bg -> new WorldViewBackgroundResponse(bg))
                .collect(Collectors.toList()))
            .rules(worldView.getRules().stream()
                .map(rule -> new WorldViewRuleResponse(rule))
                .collect(Collectors.toList()))
            .elements(worldView.getElements().stream()
                .map(elem -> new WorldViewElementResponse(elem))
                .collect(Collectors.toList()))
            .build();
    }
}
```

#### Controller规范

```java
/**
 * 世界观REST API Controller
 * 遵循RESTful设计原则
 */
@RestController
@RequestMapping("/api/worldview")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WorldView API", description = "世界观管理接口")
public class WorldViewController {
    
    private final WorldViewService worldViewService;
    
    @GetMapping("/{novelId}")
    @Operation(summary = "获取小说世界观")
    @ApiResponse(responseCode = "200", description = "成功获取")
    public ApiResponse<WorldViewResponse> getWorldView(
        @PathVariable Long novelId
    ) {
        log.info("GET /api/worldview/{}", novelId);
        
        try {
            WorldViewResponse response = worldViewService.getWorldView(novelId);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            log.warn("世界观不存在: {}", novelId);
            return ApiResponse.notFound(e.getMessage());
        }
    }
    
    @PostMapping("/{novelId}")
    @Operation(summary = "创建或更新世界观")
    public ApiResponse<WorldViewResponse> upsertWorldView(
        @PathVariable Long novelId,
        @Valid @RequestBody WorldViewRequest request
    ) {
        log.info("POST /api/worldview/{} with {}", novelId, request);
        
        try {
            WorldViewResponse response = worldViewService.upsertWorldView(
                novelId, request
            );
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("创建世界观失败", e);
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{novelId}/validate")
    @Operation(summary = "验证世界观约束")
    public ApiResponse<WorldViewConstraintCheckResult> validateConstraints(
        @PathVariable Long novelId
    ) {
        log.info("POST /api/worldview/{}/validate", novelId);
        
        try {
            WorldViewConstraintCheckResult result = 
                worldViewService.validateConstraints(novelId);
            return ApiResponse.success(result);
        } catch (EntityNotFoundException e) {
            return ApiResponse.notFound(e.getMessage());
        }
    }
    
    // ... 其他CRUD操作
}
```

---

### 1.2 AI服务集成规范

#### AiService增强规范

```java
/**
 * AI服务集成层 (DashScope)
 * 负责与通义千问API的交互
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
    
    private final DashscopeClient dashscopeClient;
    private final AiConfigProperties aiConfig;
    private final ModelFactory modelFactory;
    
    /**
     * 流式API调用 (用于续写等长文本生成)
     * 
     * @param prompt 完整的prompt指令
     * @param listener 流式监听器
     * @throws AiServiceException AI服务异常
     */
    public void streamChat(String prompt, StreamChatListener listener) {
        log.debug("开始流式调用AI, prompt长度: {}", prompt.length());
        
        try {
            Generation generation = Generation.builder()
                .model(aiConfig.getModel())
                .messages(Collections.singletonList(
                    Message.builder()
                        .role(Role.USER.getValue())
                        .content(prompt)
                        .build()
                ))
                .topP(0.8)
                .topK(100)
                .repetitionPenalty(1.0)
                .temperature(0.85) // 控制创意度
                .maxTokens(2000)
                .build();
            
            GenerationResult result = dashscopeClient.generateStream(generation,
                new StreamCallable() {
                    @Override
                    public void onEvent(String msg) {
                        listener.onChunkReceived(msg);
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        listener.onError(e);
                    }
                    
                    @Override
                    public void onComplete() {
                        listener.onComplete();
                    }
                }
            );
            
            log.info("流式调用完成, token使用: input={}, output={}",
                result.getUsage().getInputTokens(),
                result.getUsage().getOutputTokens()
            );
            
        } catch (Exception e) {
            log.error("AI流式调用失败", e);
            throw new AiServiceException("AI流式调用失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * JSON格式化API调用
     * 
     * @param prompt 包含JSON格式要求的prompt
     * @param jsonSchema JSON模式名称 (用于验证)
     * @return 解析后的JSON对象
     */
    public JsonNode callWithJsonSchema(String prompt, String jsonSchema) {
        log.debug("开始调用AI获取JSON: schema={}", jsonSchema);
        
        try {
            String response = this.chat(prompt);
            
            // 提取JSON部分
            String json = extractJsonFromResponse(response);
            
            // 验证JSON schema
            JsonNode result = objectMapper.readTree(json);
            validateJsonSchema(result, jsonSchema);
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败", e);
            throw new AiServiceException("JSON解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 约束验证API调用
     * 用于检查生成内容是否违反约束条件
     * 
     * @param content 待验证内容
     * @param constraints 约束条件
     * @return 验证结果
     */
    public ConstraintValidationResult validateConstraints(
        String content,
        List<String> constraints
    ) {
        log.debug("开始验证约束, 约束数量: {}", constraints.size());
        
        String prompt = buildConstraintValidationPrompt(content, constraints);
        
        String response = this.chat(prompt);
        
        // 解析验证结果
        return parseConstraintValidationResult(response);
    }
    
    /**
     * 关键信息提取API
     * 
     * @param text 源文本
     * @param extractType 提取类型 (outline/scene/character/hook)
     * @return 提取的信息
     */
    public <T> T extractElementsFromText(String text, Class<T> extractType) {
        log.debug("开始提取{}, 文本长度: {}", extractType.getSimpleName(), text.length());
        
        String prompt = buildExtractionPrompt(text, extractType);
        
        JsonNode result = this.callWithJsonSchema(prompt, extractType.getSimpleName());
        
        try {
            return objectMapper.treeToValue(result, extractType);
        } catch (JsonProcessingException e) {
            log.error("提取失败", e);
            throw new AiServiceException("提取失败: " + e.getMessage(), e);
        }
    }
    
    // 私有方法...
    
    private String buildConstraintValidationPrompt(String content, 
                                                   List<String> constraints) {
        StringBuilder sb = new StringBuilder();
        sb.append("检查以下内容是否违反约束条件:\n\n");
        sb.append("【内容】\n").append(content).append("\n\n");
        sb.append("【约束条件】\n");
        for (int i = 0; i < constraints.size(); i++) {
            sb.append(i + 1).append(". ").append(constraints.get(i)).append("\n");
        }
        sb.append("\n返回JSON格式的验证结果。");
        return sb.toString();
    }
    
    private String buildExtractionPrompt(String text, Class<?> extractType) {
        if (extractType == OutlineExtractionResult.class) {
            return "从以下文本中提取情节点...\n" + text;
        } else if (extractType == SceneExtractionResult.class) {
            return "从以下文本中提取场景信息...\n" + text;
        } else if (extractType == CharacterExtractionResult.class) {
            return "从以下文本中提取人物信息...\n" + text;
        } else if (extractType == PlotHookExtractionResult.class) {
            return "从以下文本中提取伏笔信息...\n" + text;
        }
        return text;
    }
}

/**
 * 流式聊天监听器接口
 */
public interface StreamChatListener {
    void onChunkReceived(String chunk);
    void onError(Exception e);
    void onComplete();
}

/**
 * 约束验证结果
 */
@Data
@Builder
public class ConstraintValidationResult {
    private boolean valid;
    private List<ConstraintViolation> violations;
    private String summary;
}

@Data
@Builder
public class ConstraintViolation {
    private int constraintIndex;
    private String description;
    private String severity; // critical, warning, info
    private String suggestion;
}
```

---

### 1.3 数据库操作规范

#### 批量操作规范

```java
/**
 * 批量保存世界观背景设定
 */
@Transactional
public void saveBatchBackgrounds(Long worldviewId, 
                               List<WorldViewBackgroundRequest> requests) {
    
    // 步骤1: 删除旧数据
    bgRepository.deleteByWorldviewId(worldviewId);
    
    // 步骤2: 批量插入
    List<WorldViewBackground> entities = requests.stream()
        .map(req -> WorldViewBackground.builder()
            .worldviewId(worldviewId)
            .category(req.getCategory())
            .name(req.getName())
            .description(req.getDescription())
            .importance(req.getImportance())
            .build())
        .collect(Collectors.toList());
    
    // 分批保存 (避免内存溢出)
    final int batchSize = 500;
    for (int i = 0; i < entities.size(); i += batchSize) {
        int end = Math.min(i + batchSize, entities.size());
        bgRepository.saveAll(entities.subList(i, end));
    }
    
    log.info("成功保存{}条背景设定", entities.size());
}
```

#### 查询优化规范

```java
/**
 * 查询优化: 使用分页和投影
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    // ❌ 不推荐: 一次性加载所有关联对象
    // @Query("SELECT c FROM Chapter c LEFT JOIN FETCH c.scenes LEFT JOIN FETCH c.characters")
    
    // ✅ 推荐: 使用分页和延迟加载
    @Query("SELECT new com.aiwriter.dto.ChapterDTO(c.id, c.title, c.wordCount) " +
           "FROM Chapter c WHERE c.novelId = :novelId")
    Page<ChapterDTO> findByNovelIdProjection(
        @Param("novelId") Long novelId,
        Pageable pageable
    );
}
```

---

## 2. 前端技术规范

### 2.1 Vue 3组件规范

#### 单文件组件规范

```vue
<!-- SmartNovelCreator.vue -->
<template>
  <!-- 模板部分 -->
  <div class="smart-novel-creator">
    <!-- 表单区 -->
    <form @submit.prevent="handleSubmit">
      <div class="form-section">
        <label for="novelName">小说名称 *</label>
        <input
          id="novelName"
          v-model="form.novelName"
          type="text"
          required
          aria-label="小说名称"
        />
      </div>

      <!-- 大纲编写 (必填) -->
      <div class="form-section">
        <div class="section-header">
          <label for="outline">创作大纲 *</label>
          <button
            type="button"
            class="btn-ai"
            @click="generateOutlineSuggestions"
            :disabled="loadingOutline"
            aria-label="获取大纲建议"
          >
            💡 获取AI建议
          </button>
        </div>
        <textarea
          id="outline"
          v-model="form.outline"
          placeholder="请输入创作大纲..."
          required
          aria-label="创作大纲"
        />
        <div class="char-count">
          {{ form.outline.length }} / {{ outlineMinLength }}-500字
        </div>
      </div>

      <!-- 初始场景 (必填) -->
      <div class="form-section">
        <div class="section-header">
          <label for="initialScene">初始场景 *</label>
          <button
            type="button"
            class="btn-ai"
            @click="generateSceneSuggestions"
            :disabled="loadingScene"
            aria-label="获取场景建议"
          >
            💡 获取AI建议
          </button>
        </div>
        <textarea
          id="initialScene"
          v-model="form.initialScene"
          placeholder="请描述开篇场景..."
          required
          aria-label="初始场景"
        />
      </div>

      <!-- 验证错误提示 -->
      <div v-if="formErrors.length" class="validation-errors">
        <ul>
          <li v-for="error in formErrors" :key="error">{{ error }}</li>
        </ul>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="isSubmitting">
          {{ isSubmitting ? "创建中..." : "下一步: 设定世界观" }}
        </button>
      </div>
    </form>

    <!-- 推荐面板 (右侧) -->
    <div class="suggestions-panel">
      <!-- 大纲建议 -->
      <div v-if="activeTab === 'outline'" class="suggestion-list">
        <h3>大纲建议</h3>
        <div v-if="loadingOutline" class="skeleton-loader">
          <div v-for="i in 3" :key="i" class="skeleton-item" />
        </div>
        <div v-else-if="outlineSuggestions.length" class="suggestions">
          <div
            v-for="suggestion in outlineSuggestions"
            :key="suggestion.id"
            class="suggestion-item"
            @click="adoptSuggestion('outline', suggestion)"
          >
            <div class="suggestion-title">{{ suggestion.title }}</div>
            <div class="suggestion-desc">{{ suggestion.description }}</div>
            <div class="suggestion-score">得分: {{ suggestion.score }}/10</div>
            <button type="button" class="btn-adopt">采用</button>
          </div>
        </div>
      </div>

      <!-- 场景建议 -->
      <div v-if="activeTab === 'scene'" class="suggestion-list">
        <h3>场景建议</h3>
        <!-- 类似大纲建议 -->
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useNovelStore } from "@/stores/novel";
import * as api from "@/api";

// 类型定义
interface CreateNovelForm {
  novelName: string;
  novelType: string;
  outline: string;
  initialScene: string;
}

interface Suggestion {
  id: string;
  title: string;
  description: string;
  score: number;
}

// Store和API
const novelStore = useNovelStore();

// 本地状态
const form = ref<CreateNovelForm>({
  novelName: "",
  novelType: "网络文学",
  outline: "",
  initialScene: ""
});

const outlineMinLength = 100;
const loadingOutline = ref(false);
const loadingScene = ref(false);
const isSubmitting = ref(false);
const activeTab = ref<"outline" | "scene">("outline");
const outlineSuggestions = ref<Suggestion[]>([]);
const sceneSuggestions = ref<Suggestion[]>([]);
const formErrors = ref<string[]>([]);

// 计算属性
const isFormValid = computed(() => {
  return (
    form.value.novelName.trim().length > 0 &&
    form.value.outline.trim().length >= outlineMinLength &&
    form.value.initialScene.trim().length > 0
  );
});

// 方法
const generateOutlineSuggestions = async () => {
  loadingOutline.value = true;
  try {
    const response = await api.generateOutlineSuggestions({
      genre: form.value.novelType,
      existingContext: form.value.outline
    });
    outlineSuggestions.value = response.data;
    activeTab.value = "outline";
  } catch (error) {
    console.error("生成建议失败", error);
  } finally {
    loadingOutline.value = false;
  }
};

const generateSceneSuggestions = async () => {
  loadingScene.value = true;
  try {
    const response = await api.generateSceneSuggestions({
      genre: form.value.novelType,
      theme: form.value.outline
    });
    sceneSuggestions.value = response.data;
    activeTab.value = "scene";
  } catch (error) {
    console.error("生成建议失败", error);
  } finally {
    loadingScene.value = false;
  }
};

const adoptSuggestion = (type: "outline" | "scene", suggestion: Suggestion) => {
  if (type === "outline") {
    form.value.outline = suggestion.description;
  } else {
    form.value.initialScene = suggestion.description;
  }
};

const handleSubmit = async () => {
  formErrors.value = [];

  // 验证
  if (!form.value.novelName.trim()) {
    formErrors.value.push("请输入小说名称");
  }
  if (form.value.outline.trim().length < outlineMinLength) {
    formErrors.value.push(`大纲至少需要${outlineMinLength}字`);
  }
  if (!form.value.initialScene.trim()) {
    formErrors.value.push("请描述初始场景");
  }

  if (formErrors.value.length > 0) return;

  isSubmitting.value = true;
  try {
    const response = await api.createNovelWithOutlineAndScene({
      name: form.value.novelName,
      type: form.value.novelType,
      outline: form.value.outline,
      initialScene: form.value.initialScene
    });

    novelStore.setCurrentNovel(response.data);
    // 跳转到世界观设定页面
    window.location.href = `/novel/${response.data.id}/worldview`;
  } catch (error) {
    console.error("创建失败", error);
    formErrors.value.push("创建失败，请重试");
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped lang="scss">
.smart-novel-creator {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  max-width: 1400px;
  margin: 0 auto;

  form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .form-section {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;

    label {
      font-weight: 600;
      color: #1f2937;
    }

    textarea {
      min-height: 150px;
      padding: 1rem;
      border: 1px solid #e5e7eb;
      border-radius: 0.5rem;
      font-family: "Fira Code", monospace;
      font-size: 0.875rem;
    }
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .btn-ai {
      padding: 0.5rem 1rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 0.375rem;
      cursor: pointer;
      font-size: 0.875rem;

      &:hover:not(:disabled) {
        opacity: 0.9;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }

  .suggestions-panel {
    border-left: 1px solid #e5e7eb;
    padding-left: 2rem;

    .suggestion-item {
      padding: 1rem;
      margin-bottom: 1rem;
      border: 1px solid #e5e7eb;
      border-radius: 0.5rem;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: #667eea;
        background: #f9fafb;
      }

      .suggestion-title {
        font-weight: 600;
        margin-bottom: 0.5rem;
      }

      .suggestion-desc {
        font-size: 0.875rem;
        color: #6b7280;
        margin-bottom: 0.5rem;
      }

      .btn-adopt {
        padding: 0.5rem 1rem;
        background: #0ea5e9;
        color: white;
        border: none;
        border-radius: 0.375rem;
        cursor: pointer;
        width: 100%;
      }
    }
  }

  .validation-errors {
    padding: 1rem;
    background: #fee2e2;
    border: 1px solid #fca5a5;
    border-radius: 0.5rem;
    color: #991b1b;

    ul {
      margin: 0;
      padding-left: 1.5rem;
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .smart-novel-creator {
    grid-template-columns: 1fr;

    .suggestions-panel {
      border-left: none;
      padding-left: 0;
      border-top: 1px solid #e5e7eb;
      padding-top: 2rem;
    }
  }
}
</style>
```

### 2.2 API调用规范

```typescript
/**
 * API调用层
 * 所有HTTP请求都通过这里统一管理
 */

import axios, { AxiosInstance } from "axios";
import { useAuthStore } from "@/stores/auth";

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json"
  }
});

// 请求拦截器：添加认证token
apiClient.interceptors.request.use(
  config => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 响应拦截器：统一错误处理
apiClient.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      useAuthStore().logout();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// ======== 小说相关API ========

export interface CreateNovelRequest {
  name: string;
  type: string;
  outline: string;
  initialScene: string;
}

export const createNovelWithOutlineAndScene = (data: CreateNovelRequest) =>
  apiClient.post("/novels/create-with-outline", data);

// ======== 世界观相关API ========

export const upsertWorldView = (novelId: number, data: any) =>
  apiClient.post(`/worldview/${novelId}`, data);

export const getWorldView = (novelId: number) =>
  apiClient.get(`/worldview/${novelId}`);

export const validateWorldView = (novelId: number) =>
  apiClient.post(`/worldview/${novelId}/validate`);

// ======== 续写相关API ========

/**
 * 流式续写API
 * 返回EventSource用于服务器推送
 */
export const streamContinuation = (
  novelId: number,
  chapterId: number,
  options: any
): EventSource => {
  const params = new URLSearchParams({
    novelId: String(novelId),
    chapterId: String(chapterId),
    ...options
  });

  return new EventSource(
    `${apiClient.defaults.baseURL}/continuations/stream?${params}`
  );
};

// ======== 文本导入API ========

export const uploadTextFile = (formData: FormData) =>
  apiClient.post("/text-import/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });

export const getImportPreview = (importId: number) =>
  apiClient.get(`/text-import/${importId}/preview`);

export const finalizeTextImport = (
  importId: number,
  config: any
) =>
  apiClient.post(`/text-import/${importId}/finalize`, config);
```

---

## 3. 性能优化规范

### 3.1 后端性能优化

```java
/**
 * 数据库查询优化
 */

// 1. 使用投影避免加载不必要的字段
@Query("SELECT NEW com.aiwriter.dto.ChapterSummaryDTO(c.id, c.title, c.wordCount) " +
       "FROM Chapter c WHERE c.novelId = :novelId " +
       "ORDER BY c.chapterNumber ASC")
Page<ChapterSummaryDTO> findChapterSummaries(Long novelId, Pageable pageable);

// 2. 使用缓存减少数据库访问
@Cacheable(value = "worldview", key = "#novelId")
public NovelWorldview getWorldView(Long novelId) {
  return worldViewRepo.findByNovelId(novelId)
    .orElseThrow();
}

// 3. 批量操作而不是逐条操作
@Modifying
@Query("UPDATE Chapter c SET c.status = :status " +
       "WHERE c.novelId = :novelId")
void updateAllChaptersStatus(Long novelId, String status);

// 4. 异步处理长耗时操作
@Async
public CompletableFuture<Void> extractElementsFromText(
    TextImportLog log,
    List<ImportedChapter> chapters) {
  // 异步处理，不阻塞主线程
  return CompletableFuture.runAsync(() -> {
    // 关键要素提取逻辑
  });
}
```

### 3.2 前端性能优化

```typescript
/**
 * Vue 3 性能优化
 */

// 1. 使用虚拟滚动处理大列表
<template>
  <virtual-scroller
    :items="chapters"
    :item-height="100"
    buffer="5"
  >
    <template #default="{ item }">
      <ChapterItem :chapter="item" />
    </template>
  </virtual-scroller>
</template>

// 2. 使用 v-once 冻结静态内容
<template>
  <div v-once class="static-header">
    {{ staticTitle }}
  </div>
</template>

// 3. 动态导入减小初始包大小
const SmartWritingEditor = defineAsyncComponent(() =>
  import("@/components/SmartWritingEditor.vue")
);

// 4. 防抖和节流处理高频事件
import { debounce } from "lodash-es";

const handleInputChange = debounce((text: string) => {
  analyzeContent(text);
}, 500);

// 5. 使用响应式计算缓存
const computedResult = computed(() => {
  return expensiveComputation(data.value);
}, { lazy: true });
```

---

## 4. 安全规范

### 4.1 输入验证规范

```java
/**
 * Bean Validation规范
 */

@Data
public class WorldViewBackgroundRequest {
    
    @NotBlank(message = "分类不能为空")
    @Size(min = 2, max = 50, message = "分类长度2-50字符")
    private String category;
    
    @NotBlank(message = "名称不能为空")
    @Size(min = 2, max = 255, message = "名称长度2-255字符")
    private String name;
    
    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 5000, message = "描述长度10-5000字符")
    private String description;
    
    @Min(value = 1, message = "重要性1-5")
    @Max(value = 5, message = "重要性1-5")
    private Integer importance;
    
    @Pattern(regexp = "strict|flexible|reference", 
             message = "约束级别值无效")
    private String constraintLevel;
}

/**
 * SQL注入防护 (已通过JPA参数化查询实现)
 */

// ✅ 正确: 参数化查询
@Query("SELECT c FROM Chapter c WHERE c.novelId = :novelId")
List<Chapter> findByNovelId(@Param("novelId") Long novelId);

// ❌ 错误: 字符串拼接 (禁止)
// String query = "SELECT * FROM chapters WHERE novel_id = " + novelId;

/**
 * XSS防护
 */

// 后端: 使用HtmlUtils转义
String escapedContent = HtmlUtils.htmlEscape(userInput);

// 前端: Vue自动转义
<template>
  <!-- 自动转义，防止XSS -->
  {{ userContent }}
</template>
```

---

## 5. 测试规范

### 5.1 单元测试规范

```java
/**
 * 单元测试
 */

@SpringBootTest
@AutoConfigureMockMvc
class WorldViewServiceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private NovelWorldviewRepository worldViewRepo;
    
    @MockBean
    private ConstraintEngineService constraintEngine;
    
    @InjectMocks
    private WorldViewService worldViewService;
    
    @Test
    void testUpsertWorldView_Success() {
        // Arrange
        Long novelId = 1L;
        WorldViewRequest request = WorldViewRequest.builder()
            .background(/* ... */)
            .rules(/* ... */)
            .build();
        
        NovelWorldview expected = NovelWorldview.builder()
            .novelId(novelId)
            .build();
        
        when(worldViewRepo.findByNovelId(novelId))
            .thenReturn(Optional.empty());
        when(worldViewRepo.save(any()))
            .thenReturn(expected);
        
        // Act
        WorldViewResponse result = 
            worldViewService.upsertWorldView(novelId, request);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNovelId()).isEqualTo(novelId);
        verify(worldViewRepo).save(any());
    }
    
    @Test
    void testValidateConstraints_WithViolations() {
        // Arrange
        Long novelId = 1L;
        NovelWorldview worldView = NovelWorldview.builder()
            .novelId(novelId)
            .build();
        
        List<ConstraintViolation> violations = List.of(
            new ConstraintViolation("规则1", "冲突说明")
        );
        
        when(worldViewRepo.findByNovelId(novelId))
            .thenReturn(Optional.of(worldView));
        when(constraintEngine.checkOutlineAgainstWorldView(any(), any()))
            .thenReturn(violations);
        
        // Act & Assert
        WorldViewConstraintCheckResult result = 
            worldViewService.validateConstraints(novelId);
        
        assertThat(result.getOutlineViolations()).hasSize(1);
    }
}
```

### 5.2 集成测试规范

```java
/**
 * API集成测试
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorldViewControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private NovelRepository novelRepo;
    
    @Autowired
    private NovelWorldviewRepository worldViewRepo;
    
    @Test
    void testCreateWorldView_E2E() {
        // 步骤1: 创建小说
        Novel novel = Novel.builder()
            .title("测试小说")
            .build();
        novel = novelRepo.save(novel);
        
        // 步骤2: 创建世界观
        WorldViewRequest request = WorldViewRequest.builder()
            .background(/* ... */)
            .rules(/* ... */)
            .build();
        
        ResponseEntity<ApiResponse<WorldViewResponse>> response = 
            restTemplate.postForEntity(
                "/api/worldview/" + novel.getId(),
                request,
                new ParameterizedTypeReference<ApiResponse<WorldViewResponse>>() {}
            );
        
        // 步骤3: 断言
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getNovelId())
            .isEqualTo(novel.getId());
        
        // 步骤4: 验证数据库
        Optional<NovelWorldview> saved = 
            worldViewRepo.findByNovelId(novel.getId());
        assertThat(saved).isPresent();
    }
}
```

---

这份技术规范文档为开发团队提供了详细的编码指导和最佳实践。

**关键要点总结**:

✅ **后端规范**:
- 严格的Entity/Repository/Service/Controller分层
- 参数化查询防止SQL注入
- 适当的异步处理和缓存
- 完善的异常处理

✅ **前端规范**:
- Vue 3 Composition API规范写法
- 类型安全的TypeScript
- 模块化的API调用封装
- 性能优化实践

✅ **测试规范**:
- 单元测试和集成测试相结合
- 使用Mock和Stub进行隔离测试
- 端到端的验证

✅ **安全规范**:
- 输入验证
- SQL注入防护
- XSS防护
