package com.aiwriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 地理位置设定请求DTO
 */
@Data
public class GeographySettingRequest {
    /**
     * 世界观ID
     */
    @NotNull(message = "世界观ID不能为空")
    private Long worldSettingId;

    /**
     * 地名
     */
    @NotBlank(message = "地名不能为空")
    private String name;

    /**
     * 地理类型
     */
    private String geographyType;

    /**
     * 地形描述
     */
    private String terrainType;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 重要性等级
     */
    private Integer importanceLevel = 3;

    /**
     * 气候特征
     */
    private String climate;

    /**
     * 主要资源或特色
     */
    private String specialFeatures;

    /**
     * 排序序号
     */
    private Integer sortOrder = 0;
}
