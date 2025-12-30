package com.aiwriter.dto;

import lombok.Data;

/**
 * 续写统计信息
 */
@Data
public class ContinuationStatistics {
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 总续写次数
     */
    private Long totalCount;
    
    /**
     * 采纳次数
     */
    private Long acceptedCount;
    
    /**
     * 采纳率
     */
    private Double acceptanceRate;
    
    /**
     * 平均质量评分
     */
    private Double avgQualityScore;
    
    /**
     * 平均文风一致性
     */
    private Double avgStyleConsistency;
    
    /**
     * 平均情节连贯性
     */
    private Double avgPlotCoherence;
    
    /**
     * 总生成字数
     */
    private Integer totalWordsGenerated;
    
    /**
     * 最常用长度类型
     */
    private String mostUsedLengthType;
    
    /**
     * 最常用文风类型
     */
    private String mostUsedStyleType;
}
