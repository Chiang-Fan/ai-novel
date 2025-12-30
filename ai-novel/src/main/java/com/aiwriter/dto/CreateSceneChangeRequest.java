package com.aiwriter.dto;

import lombok.Data;

@Data
public class CreateSceneChangeRequest {
    private Long sceneId;
    private String changeType; // REBUILD, DAMAGE, SEASONAL, DECORATION
    private String changeDesc;
    private String beforeState;
    private String afterState;
    private Long relatedChapterId;
}
