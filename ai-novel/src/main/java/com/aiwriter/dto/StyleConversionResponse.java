package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风格转换响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleConversionResponse {
    
    private Long id;
    private Long chapterId;
    private String sourceText;
    private String convertedText;
    private StyleInfo sourceStyle;
    private StyleInfo targetStyle;
    private String conversionNotes;
    private Integer styleMatchScore;
    private Integer version;
    private Integer rating;
    private Boolean isApplied;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StyleInfo {
        private Long id;
        private String name;
        private String author;
        private String category;
        private String tone;
    }
}
