package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneUsageResponse {
    private Long id;
    private Long sceneId;
    private String sceneName;
    private Long chapterId;
    private String chapterTitle;
    private LocalDateTime usageTime;
    private String sceneState;
    private String weather;
    private String timeOfDay;
    private String notes;
    private LocalDateTime createdAt;
}
