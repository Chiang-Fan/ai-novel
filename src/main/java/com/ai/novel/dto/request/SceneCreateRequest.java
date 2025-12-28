package com.ai.novel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 场景创建请求
 */
@Data
public class SceneCreateRequest {
    
    @NotNull(message = "小说ID不能为空")
    private Long novelId;
    
    @NotBlank(message = "场景名称不能为空")
    @Size(max = 200, message = "名称长度不能超过200字符")
    private String name;
    
    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;
    
    @NotNull(message = "起始章节不能为空")
    @Min(value = 1, message = "起始章节必须大于0")
    private Integer startChapter;
    
    @Min(value = 1, message = "结束章节必须大于0")
    private Integer endChapter;
    
    @Size(max = 1000, message = "氛围描述不能超过1000字符")
    private String atmosphere;
    
    @Size(max = 2000, message = "关键事件不能超过2000字符")
    private String keyEvents;
    
    @Size(max = 1000, message = "场景目标不能超过1000字符")
    private String sceneGoals;
}
