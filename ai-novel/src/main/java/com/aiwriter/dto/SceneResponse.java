package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneResponse {
    private Long id;
    private Long novelId;
    private String sceneName;
    private String sceneType;
    private String locationDesc;
    private String description;
    private String atmosphere;
    private List<String> tags;
    private Integer importanceScore;
    private Boolean isRecurring;
    private Long usageCount;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
