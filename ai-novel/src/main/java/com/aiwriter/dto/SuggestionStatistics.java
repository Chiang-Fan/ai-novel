package com.aiwriter.dto;

import lombok.Data;

import java.util.Map;

/**
 * 推荐统计 DTO
 */
@Data
public class SuggestionStatistics {

    private Long novelId;

    // 总览
    private Integer totalCount;
    private Integer activeCount;
    private Integer acceptedCount;
    private Integer rejectedCount;
    private Integer expiredCount;

    // 分类统计
    private Map<String, Integer> countByType;

    // 优先级分布
    private Integer highPriorityCount;    // priority >= 8
    private Integer mediumPriorityCount;  // 5 <= priority < 8
    private Integer lowPriorityCount;     // priority < 5

    // 平均指标
    private Double avgPriority;
    private Double avgRelevanceScore;

    // 采纳率
    private Double acceptanceRate;  // accepted / (accepted + rejected)

    // 推荐质量
    private Double qualityScore;    // 综合质量评分（基于相关性和采纳率）
}
