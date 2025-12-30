package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 世界观设定主体
 * 用于存储小说的世界观信息，包括宇宙背景、地理、时代等多维设定
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "world_settings")
public class WorldSetting extends BaseEntity {

    /**
     * 关联的小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    /**
     * 世界观名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 世界观类别（universe/geography/race/magic/technology/culture）
     */
    @Column(length = 50)
    private String category;

    /**
     * 世界观描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 世界观规则
     */
    @Column(columnDefinition = "TEXT")
    private String rules;

    /**
     * 宇宙背景/世界起源
     */
    @Column(name = "cosmic_background", columnDefinition = "TEXT")
    private String cosmicBackground;

    /**
     * 是否已生成AI辅助设定
     */
    @Column(name = "ai_assisted")
    private Boolean aiAssisted = false;

    /**
     * 版本号，用于跟踪设定更新
     */
    @Column(name = "version")
    private Integer version = 1;

    /**
     * 设定状态（draft/published）
     */
    @Column(length = 20)
    private String status = "draft";

    /**
     * 重要性级别（high/medium/low）
     */
    @Column(length = 10)
    private String importance = "medium";

    /**
     * 宇宙规则
     */
    @Column(name = "universe_rules", columnDefinition = "TEXT")
    private String universeRules;

    /**
     * 时代背景上下文
     */
    @Column(name = "time_period_context", columnDefinition = "TEXT")
    private String timePeriodContext;

    /**
     * 魔法系统规则
     */
    @Column(name = "magic_system_rules", columnDefinition = "TEXT")
    private String magicSystemRules;
}
