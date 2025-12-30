package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 写作风格响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingStyleResponse {
    
    private Long id;
    private String name;
    private String author;
    private String description;
    private String sampleText;
    private String category;
    private String languageComplexity;
    private String sentenceLength;
    private String tone;
    private Integer usageCount;
    private BigDecimal rating;
    private Boolean isSystem;
    private LocalDateTime createdAt;
}
