package com.aiwriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 世界观设定请求DTO
 */
@Data
public class WorldSettingRequest {
    /**
     * 小说ID
     */
    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /**
     * 世界观名称
     */
    @NotBlank(message = "世界观名称不能为空")
    private String name;

    /**
     * 世界观描述
     */
    private String description;

    /**
     * 宇宙背景
     */
    private String cosmicBackground;
}
