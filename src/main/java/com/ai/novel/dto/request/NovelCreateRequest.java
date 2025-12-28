package com.ai.novel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 小说创建请求
 */
@Data
public class NovelCreateRequest {
    
    @NotBlank(message = "小说标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;
    
    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;
    
    @Size(max = 100, message = "作者名长度不能超过100字符")
    private String author;
    
    @Size(max = 50, message = "类型长度不能超过50字符")
    private String type;
    
    @Size(max = 1000, message = "写作风格描述不能超过1000字符")
    private String writingStyle;
    
    /**
     * 目标字数
     */
    private Integer targetWordCount;
}
