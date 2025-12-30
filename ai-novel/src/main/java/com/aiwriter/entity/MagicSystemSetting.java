package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 魔法/超自然规则系统设定
 * 存储世界观中的魔法体系、超能力、科技水平等规则
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "magic_system_settings")
public class MagicSystemSetting extends BaseEntity {

    /**
     * 关联的世界观ID
     */
    @Column(name = "world_setting_id", nullable = false)
    private Long worldSettingId;

    /**
     * 系统名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 系统类型（魔法/超能力/科技/修仙等）
     */
    @Column(name = "system_type", length = 50)
    private String systemType;

    /**
     * 核心规则
     */
    @Column(name = "core_rules", columnDefinition = "TEXT")
    private String coreRules;

    /**
     * 能力体系分级（如等级划分）
     */
    @Column(name = "power_levels", columnDefinition = "TEXT")
    private String powerLevels;

    /**
     * 修炼/学习方式
     */
    @Column(name = "cultivation_methods", columnDefinition = "TEXT")
    private String cultivationMethods;

    /**
     * 主要类型或分支
     */
    @Column(name = "main_categories", columnDefinition = "TEXT")
    private String mainCategories;

    /**
     * 限制条件（如魔法消耗、后遗症等）
     */
    @Column(name = "limitations", columnDefinition = "TEXT")
    private String limitations;

    /**
     * 能量来源或修炼资源
     */
    @Column(name = "energy_sources", columnDefinition = "TEXT")
    private String energySources;

    /**
     * 使用者人群
     */
    @Column(name = "practitioners", length = 200)
    private String practitioners;

    /**
     * 历史背景（如何产生的）
     */
    @Column(name = "historical_background", columnDefinition = "TEXT")
    private String historicalBackground;

    /**
     * 重要性等级（1-5）
     */
    @Column(name = "importance_level")
    private Integer importanceLevel = 4;
}
