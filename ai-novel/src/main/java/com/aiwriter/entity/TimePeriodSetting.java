package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 时代背景设定
 * 存储小说世界的时间框架、历史背景、时代特征等
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "time_period_settings")
public class TimePeriodSetting extends BaseEntity {

    /**
     * 关联的世界观ID
     */
    @Column(name = "world_setting_id", nullable = false)
    private Long worldSettingId;

    /**
     * 时代名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 时代类型（古代/中世纪/现代/未来等）
     */
    @Column(name = "period_type", length = 50)
    private String periodType;

    /**
     * 时间跨度描述（如"第三纪元第五百年"）
     */
    @Column(length = 200)
    private String timeDescription;

    /**
     * 历史背景
     */
    @Column(columnDefinition = "TEXT")
    private String historicalBackground;

    /**
     * 主要历史事件
     */
    @Column(name = "major_events", columnDefinition = "TEXT")
    private String majorEvents;

    /**
     * 时代特征（政治制度、科技水平、文化特点等）
     */
    @Column(name = "era_characteristics", columnDefinition = "TEXT")
    private String eraCharacteristics;

    /**
     * 社会结构描述
     */
    @Column(name = "social_structure", columnDefinition = "TEXT")
    private String socialStructure;

    /**
     * 开始时间戳（可选，用于时间轴管理）
     */
    @Column(name = "start_year")
    private Integer startYear;

    /**
     * 结束时间戳（可选）
     */
    @Column(name = "end_year")
    private Integer endYear;
}
