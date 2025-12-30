package com.aiwriter.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成图片响应DTO
 */
@Data
@Builder
public class GeneratedImageResponse {
    
    private Long id;
    
    private Long novelId;
    
    private Long chapterId;
    
    private Long characterId;
    
    private Long sceneId;
    
    private String imageType;
    
    private String prompt;
    
    private String negativePrompt;
    
    private String style;
    
    private String imageUrl;
    
    private String localPath;
    
    private Integer width;
    
    private Integer height;
    
    private Long fileSize;
    
    private String aiModel;
    
    private Integer qualityScore;
    
    private Boolean isAdopted;
    
    private LocalDateTime adoptedAt;
    
    private List<String> tags;
    
    private LocalDateTime createdAt;
}
