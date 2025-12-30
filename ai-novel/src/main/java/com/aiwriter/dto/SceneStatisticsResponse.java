package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneStatisticsResponse {
    private Long sceneId;
    private String sceneName;
    private Long totalUsages;
    private String firstUsedChapter;
    private String lastUsedChapter;
    private Map<String, Long> usageByTimeOfDay;
    private Map<String, Long> usageByWeather;
    private Long changeCount;
}
