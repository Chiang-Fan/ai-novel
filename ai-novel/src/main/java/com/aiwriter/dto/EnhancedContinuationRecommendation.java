package com.aiwriter.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 增强续写推荐结果
 */
@Data
public class EnhancedContinuationRecommendation {
    
    /**
     * 续写内容
     */
    private String continuationText;
    
    /**
     * 复合评分（0-100）
     */
    private Integer compositeScore;
    
    /**
     * 是否建议应用
     */
    private Boolean recommendToApply;
    
    /**
     * 修改建议列表
     */
    private List<String> modificationSuggestions = new ArrayList<>();
    
    /**
     * 优化建议列表
     */
    private List<ContinuationSuggestion> suggestions = new ArrayList<>();
    
    /**
     * 续写质量评分
     */
    private QualityScore qualityScore;
    
    /**
     * 情节一致性检查结果
     */
    private PlotConsistencyCheckResult consistencyCheck;
}