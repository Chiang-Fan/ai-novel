package com.aiwriter.dto;

import com.aiwriter.entity.AutoSuggestion;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能推荐 DTO
 */
@Data
public class AutoSuggestionDto {

    private Long id;
    private Long novelId;
    private Long chapterId;
    private Integer chapterNumber;
    private AutoSuggestion.SuggestionType type;
    private String title;
    private String content;
    private Integer priority;
    private Double relevanceScore;
    private List<String> basis;
    private AutoSuggestion.SuggestionStatus status;
    private ExpectedImpact expectedImpact;
    private Long referenceId;
    private String referenceType;
    private String feedback;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Data
    public static class ExpectedImpact {
        private String dimension;      // 影响维度（质量/节奏/情节等）
        private String description;    // 影响描述
        private Integer scoreChange;   // 预期评分变化
    }
}
