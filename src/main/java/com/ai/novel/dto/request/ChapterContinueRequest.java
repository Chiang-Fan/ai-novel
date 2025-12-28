package com.ai.novel.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 章节续写请求
 */
@Data
public class ChapterContinueRequest {
    
    @NotNull(message = "小说ID不能为空")
    private Long novelId;
    
    @NotNull(message = "章节号不能为空")
    @Min(value = 1, message = "章节号必须大于0")
    private Integer chapterNumber;
    
    /**
     * 关联场景ID（可选）
     */
    private Long sceneId;
    
    /**
     * 续写方向提示（可选）
     */
    private String direction;
    
    /**
     * 生成温度 (0.0-2.0)
     */
    @Min(value = 0, message = "温度值不能小于0")
    @Max(value = 2, message = "温度值不能大于2")
    private Double temperature = 0.7;
    
    /**
     * 最大生成长度
     */
    @Min(value = 100, message = "最小生成长度为100")
    @Max(value = 10000, message = "最大生成长度为10000")
    private Integer maxLength = 2000;
}
