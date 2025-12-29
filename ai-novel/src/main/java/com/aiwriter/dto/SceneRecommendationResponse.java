package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

@Data
public class SceneRecommendationResponse {
    private String name;
    private String sceneType;
    private String location;
    private String timePeriod;
    private String weather;
    private String description;
    private String atmosphere;
    private List<String> props;
}
