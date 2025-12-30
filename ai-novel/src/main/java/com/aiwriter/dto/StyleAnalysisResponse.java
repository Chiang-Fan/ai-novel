package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 风格分析响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleAnalysisResponse {
    
    private Long id;
    private Long chapterId;
    private String textSample;
    private DetectedStyle detectedStyle;
    private StyleFeatures features;
    private Integer confidenceScore;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectedStyle {
        private Long id;
        private String name;
        private String author;
        private String category;
        private Integer matchScore; // 匹配度
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StyleFeatures {
        private String languageComplexity; // 语言复杂度
        private String sentenceLength; // 句子长度
        private String tone; // 语调
        private Integer avgSentenceLength; // 平均句长
        private Integer avgParagraphLength; // 平均段长
        private Map<String, Integer> wordFrequency; // 词频
        private String description; // 特征描述
    }
}
