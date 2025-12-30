package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 冲突追踪响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictTrackingResponse {
    
    private Long id;
    private Long novelId;
    private String conflictType;
    private String title;
    private String description;
    private List<String> involvedCharacters;
    private Integer intensityLevel;
    private Long introducedChapterId;
    private List<EscalationPoint> escalationPoints;
    private Long resolutionChapterId;
    private String status;
    private String resolutionType;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscalationPoint {
        private Integer chapterNumber;
        private String description;
        private Integer intensityChange;
    }
}
