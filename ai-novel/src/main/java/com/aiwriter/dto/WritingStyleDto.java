package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 写作风格 DTO
 */
@Data
public class WritingStyleDto {
    private Long id;
    private Long novelId;
    private String novelTitle;
    private String perspective;
    private String tone;
    private String sentenceStyle;
    private List<String> keywords;
    private String rawSample;
    private Integer avgSentenceLength;
    private Double dialogueRatio;
    private String descriptionDensity;
    private LocalDateTime extractedAt;
    private LocalDateTime lastValidatedAt;
    private Double confidenceScore;
    private String styleDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
