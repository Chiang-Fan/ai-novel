package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {
    private Long id;
    private Long characterId;
    private String characterName;
    private Long chapterId;
    private String chapterTitle;
    private String milestoneType;
    private String eventName;
    private String description;
    private Integer impactLevel;
    private Map<String, Integer> affectedAttributes;
    private LocalDateTime createdAt;
}
