package com.aiwriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 种族系统设定请求DTO
 */
@Data
public class RaceSettingRequest {
    /**
     * 世界观ID
     */
    @NotNull(message = "世界观ID不能为空")
    private Long worldSettingId;

    /**
     * 种族名称
     */
    @NotBlank(message = "种族名称不能为空")
    private String name;

    /**
     * 种族分类
     */
    private String raceCategory;

    /**
     * 物理特征
     */
    private String physicalCharacteristics;

    /**
     * 性格特点
     */
    private String personalityTraits;

    /**
     * 文化习俗
     */
    private String culturalCustoms;

    /**
     * 能力特性
     */
    private String abilities;

    /**
     * 寿命范围
     */
    private String lifespanYears;

    /**
     * 社会地位
     */
    private String socialStatus;

    /**
     * 与其他种族的关系
     */
    private String raceRelations;

    /**
     * 主要分布地区
     */
    private String mainDistribution;

    /**
     * 重要性等级
     */
    private Integer importanceLevel = 3;
}
