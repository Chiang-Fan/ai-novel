package com.aiwriter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场景氛围响应
 */
@Data
@Builder
public class SceneAtmosphereResponse {
    private Long id;
    private Long sceneId;
    private Long templateId;
    private String templateName;
    private String atmosphereType;
    private String generatedText;
    private String promptUsed;
    private String aiModel;
    private Integer version;
    private Integer rating;
    private Boolean isApplied;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
}
