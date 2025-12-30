package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 续写历史记录DTO
 */
@Data
public class ContinuationHistoryDto {
    
    private Long id;
    private Long chapterId;
    private Integer position;
    private String contextBefore;
    private Integer contextLength;
    private String generatedContent;
    private Integer wordCount;
    private String settings;
    private Double qualityScore;
    private Double styleConsistency;
    private Double plotCoherence;
    private Boolean accepted;
    private LocalDateTime acceptedAt;
    private Integer variantNumber;
    private String batchId;
    private String feedback;
    private LocalDateTime createdAt;
}
