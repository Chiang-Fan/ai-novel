package com.aiwriter.dto;

import lombok.Data;

/**
 * 章节分析统计 DTO
 */
@Data
public class ChapterAnalysisStatistics {
    
    // 基础统计
    private Long novelId;
    private Integer totalChapters;
    private Integer analyzedChapters;
    private Double analysisProgress; // 0-1
    
    // 平均质量指标
    private Double avgQualityScore;
    private Double avgReadabilityScore;
    private Double avgCreativityScore;
    private Double avgCoherenceScore;
    
    // 节奏分布
    private Integer slowPacedCount;
    private Integer mediumPacedCount;
    private Integer fastPacedCount;
    private Integer variablePacedCount;
    
    // 冲突类型分布
    private Integer internalConflictCount;
    private Integer externalConflictCount;
    private Integer mixedConflictCount;
    
    // 质量分布
    private Integer excellentChapters; // >= 8
    private Integer goodChapters; // 6-7
    private Integer averageChapters; // 4-5
    private Integer poorChapters; // < 4
    
    // 主题统计
    private Integer uniqueThemesCount;
    private String mostCommonTheme;
    
    // 叙事技巧
    private Integer avgTurningPointsPerChapter;
    private Double avgSuspenseLevel;
    
    // 角色发展
    private Integer chaptersWithCharacterGrowth;
    private Double characterGrowthRatio;
    
    // 改进建议
    private Integer totalImprovementSuggestions;
    private Integer highPriorityIssues; // priority >= 8
    
    // 趋势
    private String qualityTrend; // IMPROVING, DECLINING, STABLE
    private String conflictTrend; // ESCALATING, DE_ESCALATING, FLUCTUATING
}
