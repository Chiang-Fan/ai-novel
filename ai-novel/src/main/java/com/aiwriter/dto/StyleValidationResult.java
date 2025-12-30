package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风格验证结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleValidationResult {
    /**
     * 是否符合风格
     */
    private Boolean isValid;
    
    /**
     * 相似度评分（0-100）
     */
    private Double similarityScore;
    
    /**
     * 视角一致性
     */
    private Boolean perspectiveMatch;
    
    /**
     * 语气一致性
     */
    private Boolean toneMatch;
    
    /**
     * 句式一致性
     */
    private Boolean sentenceStyleMatch;
    
    /**
     * 验证详情
     */
    private String details;
    
    /**
     * 建议修改
     */
    private String suggestions;
}
