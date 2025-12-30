package com.aiwriter.dto;

import com.aiwriter.service.MultiDimensionalConstraintEngine;
import com.aiwriter.service.PlotConsistencyCheckService;
import com.aiwriter.service.DescriptionQualityScoreService;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 增强型续写响应 - 包含多维分析和评分
 */
@Data
public class EnhancedContinuationResponse {
    
    // 基础信息
    private Long id;
    private Long chapterId;
    private String sourceText;
    private String continuationText;
    private String style;
    private String length;
    private String aiModel;
    private Integer version;
    
    /**
     * Phase 4 新增响应字段
     */
    
    // 多维约束信息
    private MultiDimensionalConstraintEngine.CompositeConstraints constraints;
    
    // 情节一致性检查结果
    private PlotConsistencyCheckService.PlotConsistencyCheckResult consistencyCheck;
    
    // 描写质量评分
    private DescriptionQualityScoreService.DescriptionQualityScore qualityScore;
    
    // 综合建议
    private ContinuationRecommendation recommendation;
    
    // 应用状态
    private Boolean isApplied;
    private LocalDateTime appliedAt;
    private Integer rating;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    /**
     * 续写综合建议
     */
    @Data
    public static class ContinuationRecommendation {
        // 是否建议应用此续写
        private Boolean recommendToApply;
        
        // 建议理由
        private String reason;
        
        // 整体质量评级 (EXCELLENT, GOOD, FAIR, POOR)
        private String qualityRating;
        
        // 需要改进的地方
        private List<String> improvementPoints;
        
        // 修改建议
        private List<String> modificationSuggestions;
        
        // 综合评分 (0-100)
        private Integer compositeScore;
    }
}
