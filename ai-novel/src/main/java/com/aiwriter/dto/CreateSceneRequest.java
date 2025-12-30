package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateSceneRequest {
    private Long novelId;
    private String sceneName;
    private String sceneType; // INDOOR, OUTDOOR, SPECIAL
    private String locationDesc;
    private String description;
    private String atmosphere;
    private List<String> tags;
    private Integer importanceScore; // 1-10
}
