package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 地理位置设定
 * 存储世界观中的地理信息、地名、地形等
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "geography_settings")
public class GeographySetting extends BaseEntity {

    /**
     * 关联的世界观ID
     */
    @Column(name = "world_setting_id", nullable = false)
    private Long worldSettingId;

    /**
     * 地名
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 地理类型（大陆/国家/城市/地区）
     */
    @Column(name = "geography_type", length = 50)
    private String geographyType;

    /**
     * 地形描述（山脉/平原/沙漠等）
     */
    @Column(name = "terrain_type", length = 50)
    private String terrainType;

    /**
     * 详细描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 重要性等级（1-5）
     */
    @Column(name = "importance_level")
    private Integer importanceLevel = 3;

    /**
     * 气候特征
     */
    @Column(length = 200)
    private String climate;

    /**
     * 主要资源或特色
     */
    @Column(name = "special_features", columnDefinition = "TEXT")
    private String specialFeatures;

    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
