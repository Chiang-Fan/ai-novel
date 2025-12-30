package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateSceneUsageRequest {
    private Long sceneId;
    private Long chapterId;
    private LocalDateTime usageTime;
    private String sceneState;
    private String weather; // SUNNY, RAINY, CLOUDY, SNOWY
    private String timeOfDay; // MORNING, NOON, EVENING, NIGHT
    private String notes;
}
