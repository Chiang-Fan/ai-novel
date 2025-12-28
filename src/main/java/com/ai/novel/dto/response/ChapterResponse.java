package com.ai.novel.dto.response;

import com.ai.novel.entity.enums.ChapterStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节响应
 */
@Data
public class ChapterResponse {
    
    private Long id;
    private Long novelId;
    private Integer chapterNumber;
    private String title;
    private String content;
    private String summary;
    private Integer wordCount;
    private ChapterStatus status;
    private Long sceneId;
    private String sceneName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
