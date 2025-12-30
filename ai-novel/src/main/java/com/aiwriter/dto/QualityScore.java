package com.aiwriter.dto;

import lombok.Data;

/**
 * 描写质量评分
 */
@Data
public class QualityScore {
    
    /**
     * 总体评分（0-100）
     */
    private Integer overallScore;
    
    /**
     * 对话评分
     */
    private ScoreDetail dialogueScore;
    
    /**
     * 心理描写评分
     */
    private ScoreDetail psychologicalScore;
    
    /**
     * 环境描写评分
     */
    private ScoreDetail environmentScore;
    
    /**
     * 节奏评分
     */
    private ScoreDetail rhythmScore;
    
    /**
     * 情感渲染评分
     */
    private ScoreDetail emotionScore;
    
    /**
     * 详细评价
     */
    private String detailedEvaluation;
    
    /**
     * 评分时间
     */
    private String scoredAt;
    
    @Data
    public static class ScoreDetail {
        private Integer score;
        private String comment;
    }
}