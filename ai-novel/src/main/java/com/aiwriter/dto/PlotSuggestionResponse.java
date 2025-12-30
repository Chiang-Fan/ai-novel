package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 情节建议响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotSuggestionResponse {
    
    private Long id;
    private Long novelId;
    private Long chapterId;
    private String suggestionType;
    private String title;
    private String description;
    private String reasoning;
    private String priority;
    private Integer impactScore;
    private Long relatedPlotHookId;
    private Long relatedCharacterId;
    private String status;
    private Long appliedChapterId;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
}
