# Phase 2: 世界观设定模块 - 开发计划

## 阶段概述

**目标**: 构建完整的世界观设定系统，让用户能够为小说定义完整的世界观体系，并通过AI辅助和约束联动机制，确保创作内容与世界观设定保持一致。

**时间**: 3周  
**优先级**: ⭐⭐⭐ 高优先级  
**依赖**: Phase 1 完成✅

---

## 核心功能目标

### 2.1 世界观设定管理系统
- [ ] 创建新的世界观设定
- [ ] 支持多维度设定（背景、规则、特殊要素等）
- [ ] AI辅助生成世界观建议
- [ ] 世界观与小说关联约束

### 2.2 多维体系设定
- [ ] 地理/空间设定 (Geography & Locations)
- [ ] 社会体系 (Social System)
- [ ] 魔法/科技体系 (Magic/Tech System)
- [ ] 文化习俗 (Culture & Customs)
- [ ] 历史背景 (Historical Background)
- [ ] 特殊规则 (Special Rules)

### 2.3 AI辅助功能
- [ ] 基于小说类型的世界观建议
- [ ] AI自动补充世界观内容
- [ ] 约束条件的智能验证
- [ ] 世界观与场景/角色的一致性检查

### 2.4 约束联动系统
- [ ] 世界观对续写内容的约束
- [ ] 世界观对场景描写的影响
- [ ] 世界观对角色行为的限制
- [ ] 约束违规提示和建议

---

## 数据库设计

### 新增表结构

#### 1. WorldView 表 (世界观表)
```sql
CREATE TABLE world_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL UNIQUE COMMENT '关联小说ID',
    name VARCHAR(200) NOT NULL COMMENT '世界观名称',
    description TEXT COMMENT '简要描述',
    setting_type VARCHAR(50) COMMENT '世界观类型: fantasy, sci-fi, modern, historical等',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft, active, archived',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='世界观表';
```

#### 2. WorldViewDimension 表 (世界观维度表)
```sql
CREATE TABLE world_view_dimensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    world_view_id BIGINT NOT NULL COMMENT '世界观ID',
    dimension_type VARCHAR(50) NOT NULL COMMENT '维度类型: geography, society, magic, culture, history, rules',
    content TEXT COMMENT '维度内容 (JSON格式)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (world_view_id) REFERENCES world_views(id) ON DELETE CASCADE,
    UNIQUE KEY uk_world_dimension (world_view_id, dimension_type),
    INDEX idx_world_view_id (world_view_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='世界观维度表';
```

#### 3. WorldViewConstraint 表 (约束规则表)
```sql
CREATE TABLE world_view_constraints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    world_view_id BIGINT NOT NULL COMMENT '世界观ID',
    constraint_type VARCHAR(50) NOT NULL COMMENT '约束类型: character_behavior, scene_setting, magic_system等',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    description TEXT COMMENT '规则描述',
    violation_severity VARCHAR(20) DEFAULT 'warning' COMMENT '违规严重程度: warning, error, info',
    examples TEXT COMMENT '示例 (JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (world_view_id) REFERENCES world_views(id) ON DELETE CASCADE,
    INDEX idx_world_view_id (world_view_id),
    INDEX idx_constraint_type (constraint_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='世界观约束规则表';
```

#### 4. ConstraintViolationLog 表 (违规日志表)
```sql
CREATE TABLE constraint_violation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    constraint_id BIGINT NOT NULL COMMENT '约束ID',
    content_type VARCHAR(50) COMMENT '内容类型: scene, chapter, character_action等',
    content_id BIGINT COMMENT '内容ID',
    violation_text TEXT COMMENT '违规的内容片段',
    suggestion TEXT COMMENT 'AI建议修改',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, resolved, ignored',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (constraint_id) REFERENCES world_view_constraints(id),
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='约束违规日志表';
```

---

## 后端架构设计

### Entity 类
```java
// WorldView.java
@Entity
@Table(name = "world_views")
public class WorldView extends BaseEntity {
    @Column(nullable = false)
    private Long novelId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "setting_type")
    private String settingType;  // fantasy, sci-fi, modern等
    
    private String status;
    
    @OneToMany(mappedBy = "worldView", cascade = CascadeType.ALL)
    private List<WorldViewDimension> dimensions;
    
    @OneToMany(mappedBy = "worldView", cascade = CascadeType.ALL)
    private List<WorldViewConstraint> constraints;
}

// WorldViewDimension.java
@Entity
@Table(name = "world_view_dimensions")
public class WorldViewDimension extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "world_view_id")
    private WorldView worldView;
    
    @Column(name = "dimension_type")
    private String dimensionType;
    
    @Column(columnDefinition = "JSON")
    private String content;
}

// WorldViewConstraint.java
@Entity
@Table(name = "world_view_constraints")
public class WorldViewConstraint extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "world_view_id")
    private WorldView worldView;
    
    @Column(name = "constraint_type")
    private String constraintType;
    
    @Column(name = "rule_name")
    private String ruleName;
    
    private String description;
    
    @Column(name = "violation_severity")
    private String violationSeverity;
    
    @Column(columnDefinition = "JSON")
    private String examples;
}
```

### Service 层
```java
// WorldViewService.java
@Service
@RequiredArgsConstructor
public class WorldViewService {
    
    private final WorldViewRepository worldViewRepository;
    private final WorldViewDimensionRepository dimensionRepository;
    private final WorldViewConstraintRepository constraintRepository;
    private final AiService aiService;
    
    /**
     * 创建世界观
     */
    public WorldView createWorldView(Long novelId, CreateWorldViewRequest request) {
        // 验证小说存在
        // 创建世界观
        // 初始化维度
    }
    
    /**
     * 获取世界观详情
     */
    public WorldViewDetailResponse getWorldViewDetail(Long worldViewId) {
        // 返回完整的世界观信息
    }
    
    /**
     * 更新世界观维度
     */
    public void updateDimension(Long worldViewId, String dimensionType, Map<String, Object> content) {
        // 更新特定维度
    }
    
    /**
     * 添加约束规则
     */
    public WorldViewConstraint addConstraint(Long worldViewId, CreateConstraintRequest request) {
        // 添加新的约束规则
    }
    
    /**
     * 验证内容是否违反世界观约束
     */
    public ConstraintValidationResult validateContent(Long worldViewId, String content) {
        // 检查内容是否违反任何约束
        // 返回违规列表和建议
    }
}
```

### AI推荐服务
```java
// WorldViewAiRecommendationService.java
@Service
@RequiredArgsConstructor
public class WorldViewAiRecommendationService {
    
    /**
     * 基于小说类型生成世界观建议
     */
    public WorldViewRecommendation generateWorldViewSuggestion(String genre, String description) {
        // 调用AI生成推荐
        // 返回初始世界观建议
    }
    
    /**
     * 为特定维度生成内容
     */
    public String generateDimensionContent(String dimensionType, String context) {
        // AI生成该维度的详细内容
    }
    
    /**
     * 生成约束规则
     */
    public List<ConstraintRule> generateConstraints(WorldView worldView) {
        // 基于世界观自动生成约束规则
    }
}

// ConstraintEngine.java
@Service
@RequiredArgsConstructor
public class ConstraintEngine {
    
    /**
     * 验证内容是否符合约束
     */
    public ValidationReport validateContent(Long novelId, String content, ContentType type) {
        // 获取小说的世界观和约束
        // 检查内容
        // 返回验证报告
    }
    
    /**
     * 获取违规建议
     */
    public List<SuggestionItem> getSuggestions(ConstraintViolation violation) {
        // 基于违规类型提供修改建议
    }
}
```

### Controller 设计
```java
@RestController
@RequestMapping("/api/world-views")
public class WorldViewController {
    
    @PostMapping
    public ApiResponse<WorldView> createWorldView(@RequestBody CreateWorldViewRequest request) {
        // 创建世界观
    }
    
    @GetMapping("/{id}")
    public ApiResponse<WorldViewDetailResponse> getWorldView(@PathVariable Long id) {
        // 获取世界观详情
    }
    
    @PutMapping("/{id}/dimensions/{dimensionType}")
    public ApiResponse<Void> updateDimension(...) {
        // 更新维度
    }
    
    @PostMapping("/{id}/constraints")
    public ApiResponse<WorldViewConstraint> addConstraint(...) {
        // 添加约束
    }
    
    @PostMapping("/{id}/validate")
    public ApiResponse<ConstraintValidationResult> validateContent(...) {
        // 验证内容
    }
    
    @PostMapping("/{id}/recommendations")
    public ApiResponse<WorldViewRecommendation> getRecommendations(...) {
        // 获取AI推荐
    }
}
```

---

## 前端设计

### 页面结构: WorldViewEditor.vue

#### 左侧导航面板 (20%)
- 维度列表 (地理、社会、魔法等)
- 约束规则列表
- 快速链接

#### 中间编辑区 (50%)
- 维度编辑器 (文本编辑、表单、富文本)
- 实时预览
- AI辅助按钮

#### 右侧预览/约束面板 (30%)
- 世界观概览卡片
- 约束规则列表
- 违规提示 (如有)
- AI建议

### 交互设计
- 拖拽排序维度
- 快速切换维度编辑
- 实时AI建议
- 约束冲突检测

---

## 实现步骤

### Week 1: 数据库和后端核心
- [ ] 创建数据库迁移脚本 (4个新表)
- [ ] 实现Entity和Repository
- [ ] 实现WorldViewService
- [ ] 实现Controller基础端点
- [ ] 测试API

### Week 2: AI推荐和约束引擎
- [ ] 实现WorldViewAiRecommendationService
- [ ] 实现ConstraintEngine
- [ ] 实现约束验证逻辑
- [ ] 集成到续写流程

### Week 3: 前端UI和集成
- [ ] 实现WorldViewEditor.vue
- [ ] 实现维度编辑器
- [ ] 实现约束规则管理UI
- [ ] 集成到NovelDetail页面
- [ ] 测试和优化

---

## 测试计划

### 单元测试
- WorldViewService 的CRUD操作
- ConstraintEngine 的验证逻辑
- AI推荐生成

### 集成测试
- 创建小说 → 创建世界观 → 关联
- 编辑世界观维度 → 自动生成约束
- 续写时约束验证

### 端到端测试
- 完整的世界观创建流程
- UI交互验证
- 约束生效检查

---

## 成功指标

✅ 完成所有4个新数据表的创建  
✅ 5个Service类和2个主要Service  
✅ 10+ API端点  
✅ 3个维度的完整编辑器  
✅ AI推荐生成率 > 90%  
✅ 约束验证准确率 > 95%  

---

## 技术要点

### 数据结构
- JSON字段用于存储灵活的维度内容
- 外键约束确保数据一致性
- 索引优化查询性能

### AI集成
- Prompt模板化设计
- 多轮对话支持
- 默认值降级方案

### 约束验证
- 正则表达式规则
- NLP文本分析
- 逻辑规则引擎

---

## 与后续阶段的关系

### 与 Phase 3 (文本导入)
- 文本导入时应用世界观约束
- 自动提取的要素应符合世界观

### 与 Phase 4 (AI续写)
- 续写时基于世界观约束生成
- 约束作为Prompt的关键输入

### 与 Phase 5 (节奏管理)
- 节奏规则可作为特殊约束
- 世界观影响节奏的选择

---

## 文档输出

- Phase2_Implementation_Report.md (完成时)
- WorldView API文档
- 世界观设计指南
- 约束规则示例库

---

## 下一步

1. 确认Phase 2规划无异议
2. 创建数据库迁移脚本
3. 实现Entity和Repository
4. 开始Service层开发

**预计完成时间**: 2025年1月20日
