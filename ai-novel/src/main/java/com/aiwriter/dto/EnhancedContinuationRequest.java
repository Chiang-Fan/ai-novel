package com.aiwriter.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * 增强型续写请求 - 支持多维约束
 */
@Data
public class EnhancedContinuationRequest {
    
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;
    
    @NotBlank(message = "源文本不能为空")
    private String sourceText;
    
    // 续写风格
    private String style = "NEUTRAL"; // SERIOUS, LIGHT, SUSPENSE, ROMANTIC, ACTION, NEUTRAL
    
    // 续写长度
    private String length = "PARAGRAPH"; // SENTENCE, PARAGRAPH, SECTION, LONG
    
    // 额外上下文
    private String additionalContext;
    
    /**
     * Phase 4 新增参数
     */
    
    // 是否启用多维约束
    private Boolean enableConstraints = true;
    
    // 约束优先级
    private String constraintPriority = "HIGH"; // LOW, MEDIUM, HIGH
    
    // 是否检查情节一致性
    private Boolean checkPlotConsistency = true;
    
    // 是否检查角色行为一致性
    private Boolean checkCharacterConsistency = true;
    
    // 是否进行描写质量评分
    private Boolean enableQualityScore = true;
    
    // 最低质量分数要求 (0-100)
    private Integer minimumQualityScore = 60;
    
    // 是否需要多次迭代优化
    private Boolean iterativeRefinement = false;
    
    // 最多迭代次数
    private Integer maxIterations = 3;
    
    // 是否返回约束信息（用于调试）
    private Boolean debugMode = false;
}
