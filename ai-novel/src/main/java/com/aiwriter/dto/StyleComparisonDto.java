package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 风格对比结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleComparisonDto {
    /**
     * 小说原始风格
     */
    private WritingStyleDto originalStyle;
    
    /**
     * 章节风格分析
     */
    private WritingStyleDto chapterStyle;
    
    /**
     * 相似度评分（0-100）
     */
    private Double similarityScore;
    
    /**
     * 差异项
     */
    private Map<String, String> differences;
    
    /**
     * 对比总结
     */
    private String summary;
}
