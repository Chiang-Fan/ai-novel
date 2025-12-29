package com.aiwriter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说创建请求DTO
 */
@Data
public class NovelCreateRequest {
    
    @NotBlank(message = "小说标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;
    
    @Size(max = 5000, message = "简介长度不能超过5000")
    private String description;
    
    @Size(max = 50, message = "类型长度不能超过50")
    private String genre;
    
    @Size(max = 50, message = "目标读者长度不能超过50")
    private String targetAudience;
    
    private String writingStyle;
}

