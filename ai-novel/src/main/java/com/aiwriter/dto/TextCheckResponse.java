package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本检查响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextCheckResponse {
    
    private Integer totalIssues;
    private List<Issue> issues;
    private QualityScore qualityScore;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private String type; // GRAMMAR, STYLE, CONSISTENCY, REDUNDANCY
        private String severity; // LOW, MEDIUM, HIGH
        private String message;
        private String suggestion;
        private Integer startPosition;
        private Integer endPosition;
        private String affectedText;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityScore {
        private Integer overall; // 总分 0-100
        private Integer grammar; // 语法得分
        private Integer style; // 风格得分
        private Integer readability; // 可读性得分
        private Integer consistency; // 一致性得分
    }
}
