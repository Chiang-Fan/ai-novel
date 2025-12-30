package com.aiwriter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 写作建议响应
 */
@Data
@Builder
public class SuggestionResponse {
    private Long id;
    private Long chapterId;
    private String suggestionType;
    private String suggestion;
    private String reasoning;
    private String priority;
    private String status;
    private String contextText;
    private Long relatedOutlineId;
    private String relatedOutlineTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
