package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateSceneRequest {
    private String sceneName;
    private String sceneType;
    private String locationDesc;
    private String description;
    private String atmosphere;
    private List<String> tags;
    private Integer importanceScore;
    private Boolean isRecurring;
}
