package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 种族系统设定
 * 存储世界观中的各个种族信息
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "race_settings")
public class RaceSetting extends BaseEntity {

    /**
     * 关联的世界观ID
     */
    @Column(name = "world_setting_id", nullable = false)
    private Long worldSettingId;

    /**
     * 种族名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 种族分类（人类/兽族/精灵等）
     */
    @Column(name = "race_category", length = 50)
    private String raceCategory;

    /**
     * 物理特征
     */
    @Column(name = "physical_characteristics", columnDefinition = "TEXT")
    private String physicalCharacteristics;

    /**
     * 性格特点
     */
    @Column(name = "personality_traits", columnDefinition = "TEXT")
    private String personalityTraits;

    /**
     * 文化习俗
     */
    @Column(name = "cultural_customs", columnDefinition = "TEXT")
    private String culturalCustoms;

    /**
     * 能力特性
     */
    @Column(name = "abilities", columnDefinition = "TEXT")
    private String abilities;

    /**
     * 寿命范围
     */
    @Column(name = "lifespan_years")
    private String lifespanYears;

    /**
     * 社会地位（统治/从属/独立等）
     */
    @Column(name = "social_status", length = 50)
    private String socialStatus;

    /**
     * 与其他种族的关系
     */
    @Column(name = "race_relations", columnDefinition = "TEXT")
    private String raceRelations;

    /**
     * 主要分布地区
     */
    @Column(name = "main_distribution", length = 200)
    private String mainDistribution;

    /**
     * 重要性等级（1-5）
     */
    @Column(name = "importance_level")
    private Integer importanceLevel = 3;
}
