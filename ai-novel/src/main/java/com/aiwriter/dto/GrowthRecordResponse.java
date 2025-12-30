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
public class GrowthRecordResponse {
    private Long id;
    private Long characterId;
    private String characterName;
    private Long chapterId;
    private String chapterTitle;
    private LocalDateTime recordTime;
    private Map<String, Object> attributes;
    private String notes;
    private LocalDateTime createdAt;
}
