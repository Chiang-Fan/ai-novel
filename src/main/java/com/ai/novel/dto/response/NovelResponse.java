package com.ai.novel.dto.response;

import com.ai.novel.entity.enums.NovelStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 小说响应
 */
@Data
public class NovelResponse {
    
    private Long id;
    private String title;
    private String description;
    private String author;
    private String type;
    private Map<String, Object> writingStyle;
    private Integer targetWordCount;
    private Integer currentWordCount;
    private Integer chapterCount;
    private NovelStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
