package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节奏优化建议DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RhythmRecommendation {
    
    /** 场景ID */
    private Long sceneId;
    
    /** 场景名称 */
    private String sceneName;
    
    /** 当前节奏模式 */
    private String currentPattern;
    
    /** 当前节奏评分 */
    private Double currentScore;
    
    /** 问题描述 */
    private String issue;
    
    /** 优化建议 */
    private String recommendation;
    
    /** 优先级 (HIGH/MEDIUM/LOW) */
    private String priority;
    
    /** 额外说明 */
    private String note;
    
    /** 建议目标评分 */
    private Double targetScore;
    
    /** 建议的改进方向 */
    private String suggestedImprovement;
}
