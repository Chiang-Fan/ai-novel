package com.aiwriter.dto;

import lombok.Data;

/**
 * 图片生成请求DTO
 */
@Data
public class ImageGenerationRequest {
    
    private Long novelId;
    
    private Long chapterId;
    
    private Long characterId;
    
    private Long sceneId;
    
    private String imageType; // CHARACTER, SCENE, COVER, ILLUSTRATION
    
    private String prompt; // 自定义提示词
    
    private String negativePrompt;
    
    private String style; // REALISTIC, ANIME, COMIC, WATERCOLOR, OIL_PAINTING, SKETCH
    
    private Integer width;
    
    private Integer height;
    
    private Integer batchSize = 1; // 批次生成数量
    
    private Long templateId; // 使用的模板ID
}
