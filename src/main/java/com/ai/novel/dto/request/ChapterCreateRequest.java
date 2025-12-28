package com.ai.novel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 章节创建请求
 */
@Data
public class ChapterCreateRequest {
    
    @NotNull(message = "小说ID不能为空")
    private Long novelId;
    
    @NotNull(message = "章节号不能为空")
    @Min(value = 1, message = "章节号必须大于0")
    private Integer chapterNumber;
    
    @NotBlank(message = "章节标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;
    
    @NotBlank(message = "章节内容不能为空")
    private String content;
    
    @Size(max = 1000, message = "摘要长度不能超过1000字符")
    private String summary;
    
    /**
     * 关联场景ID
     */
    private Long sceneId;
}
