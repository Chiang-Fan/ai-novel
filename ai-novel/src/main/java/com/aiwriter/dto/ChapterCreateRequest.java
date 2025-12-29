package com.aiwriter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 章节创建请求DTO
 */
@Data
public class ChapterCreateRequest {
    
    private Long novelId;
    
    @NotBlank(message = "章节标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;
    
    @NotBlank(message = "章节内容不能为空")
    private String content;
    
    private Long sceneId;
    private Long outlineNodeId;
    private String continuationDirection;
}

