package com.aiwriter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 续写响应
 */
@Data
@Builder
public class ContinuationResponse {
    private Long id;
    private Long chapterId;
    private String sourceText;
    private String continuationText;
    private String style;
    private String length;
    private String promptUsed;
    private String aiModel;
    private Integer version;
    private Integer rating;
    private Boolean isApplied;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private OutlineCheck outlineCheck;  // 大纲检查结果
    
    @Data
    @Builder
    public static class OutlineCheck {
        private Boolean isConsistent;
        private String issue;
        private String suggestion;
        private Long relatedOutlineId;
    }
}
