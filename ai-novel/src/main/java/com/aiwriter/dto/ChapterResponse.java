package com.aiwriter.dto;

import lombok.Data; /**
 * 章节响应DTO
 */
@Data
public class ChapterResponse {
    private Long id;
    private Long novelId;
    private Long sceneId;
    private Long outlineNodeId;
    private String title;
    private String content;
    private Integer chapterNumber;
    private Integer wordCount;
    private String summary;
    private String continuationDirection;
    private Boolean isAiGenerated;
}
