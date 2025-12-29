package com.aiwriter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime; /**
 * 小说响应DTO
 */
@Data
public class NovelResponse {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private String targetAudience;
    private String writingStyle;
    private String status;
    private Integer totalChapters;
    private Integer totalWords;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
