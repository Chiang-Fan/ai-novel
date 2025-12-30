package com.aiwriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 时代背景设定请求DTO
 */
@Data
public class TimePeriodSettingRequest {
    /**
     * 世界观ID
     */
    @NotNull(message = "世界观ID不能为空")
    private Long worldSettingId;

    /**
     * 时代名称
     */
    @NotBlank(message = "时代名称不能为空")
    private String name;

    /**
     * 时代类型
     */
    private String periodType;

    /**
     * 时间跨度描述
     */
    private String timeDescription;

    /**
     * 历史背景
     */
    private String historicalBackground;

    /**
     * 主要历史事件
     */
    private String majorEvents;

    /**
     * 时代特征
     */
    private String eraCharacteristics;

    /**
     * 社会结构描述
     */
    private String socialStructure;

    /**
     * 开始年份
     */
    private Integer startYear;

    /**
     * 结束年份
     */
    private Integer endYear;
}
