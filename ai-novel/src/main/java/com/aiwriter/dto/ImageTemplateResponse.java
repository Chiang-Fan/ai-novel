package com.aiwriter.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图片模板响应DTO
 */
@Data
@Builder
public class ImageTemplateResponse {
    
    private Long id;
    
    private String name;
    
    private String category;
    
    private String description;
    
    private String promptTemplate;
    
    private String negativePromptTemplate;
    
    private String defaultStyle;
    
    private String recommendedSize;
    
    private String previewUrl;
    
    private Integer usageCount;
    
    private Boolean isSystem;
    
    private LocalDateTime createdAt;
}
