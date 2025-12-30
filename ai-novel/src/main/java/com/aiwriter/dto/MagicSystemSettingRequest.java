package com.aiwriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 魔法系统设定请求DTO
 */
@Data
public class MagicSystemSettingRequest {
    /**
     * 世界观ID
     */
    @NotNull(message = "世界观ID不能为空")
    private Long worldSettingId;

    /**
     * 系统名称
     */
    @NotBlank(message = "系统名称不能为空")
    private String name;

    /**
     * 系统类型
     */
    private String systemType;

    /**
     * 核心规则
     */
    private String coreRules;

    /**
     * 能力体系分级
     */
    private String powerLevels;

    /**
     * 修炼/学习方式
     */
    private String cultivationMethods;

    /**
     * 主要类型或分支
     */
    private String mainCategories;

    /**
     * 限制条件
     */
    private String limitations;

    /**
     * 能量来源或修炼资源
     */
    private String energySources;

    /**
     * 使用者人群
     */
    private String practitioners;

    /**
     * 历史背景
     */
    private String historicalBackground;

    /**
     * 重要性等级
     */
    private Integer importanceLevel = 4;
}
