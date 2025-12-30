package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容优化响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationResponse {
    
    private Long id;
    private Long chapterId;
    private String originalText;
    private String optimizedText;
    private String optimizationType;
    private List<Change> changes;
    private Integer version;
    private Integer rating;
    private Boolean isApplied;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Change {
        private String type; // ADD, DELETE, MODIFY
        private String original;
        private String modified;
        private String reason;
        private Integer position;
    }
}
