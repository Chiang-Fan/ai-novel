package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小说创建推荐实体
 * 存储为新小说生成的AI推荐数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "novel_creation_recommendations")
public class NovelCreationRecommendation extends BaseEntity {
    
    /**
     * 关联的小说ID
     */
    @Column(unique = true, nullable = false)
    private Long novelId;
    
    /**
     * 故事框架
     */
    @Column(columnDefinition = "TEXT")
    private String storyFramework;
    
    /**
     * 三幕结构 (JSON格式)
     */
    @Column(columnDefinition = "JSON")
    private String threeActStructure;
    
    /**
     * 主要情节点 (JSON数组)
     */
    @Column(columnDefinition = "JSON")
    private String mainPlotPoints;
    
    /**
     * 初始场景推荐 (JSON格式)
     */
    @Column(columnDefinition = "JSON")
    private String initialSceneRecommendation;
    
    /**
     * 角色推荐 (JSON数组)
     */
    @Column(columnDefinition = "JSON")
    private String characterRecommendations;
    
    /**
     * 主题 (JSON数组)
     */
    @Column(columnDefinition = "JSON")
    private String themes;
    
    /**
     * 写作风格要素 (JSON数组)
     */
    @Column(columnDefinition = "JSON")
    private String styleElements;
    
    /**
     * 字数范围 (JSON格式)
     */
    @Column(columnDefinition = "JSON")
    private String wordCountRange;
    
    /**
     * 预估阅读时间 (分钟)
     */
    private Integer estimatedReadingTime;
}
