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
public class SceneChangeResponse {
    private Long id;
    private Long sceneId;
    private String sceneName;
    private String changeType;
    private String changeDesc;
    private String beforeState;
    private String afterState;
    private Long relatedChapterId;
    private String chapterTitle;
    private LocalDateTime createdAt;
}
