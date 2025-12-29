package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 世界观设定实体
 * 对应Python的WorldSetting模型
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "world_settings", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_category", columnList = "category")
})
public class WorldSetting extends BaseEntity {
    
    /**
     * 所属小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    /**
     * 设定分类（地理、历史、魔法、科技等）
     */
    @Column(nullable = false, length = 50)
    private String category;
    
    /**
     * 设定名称
     */
    @Column(nullable = false, length = 200)
    private String name;
    
    /**
     * 设定描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 设定规则
     */
    @Column(columnDefinition = "TEXT")
    private String rules;
    
    /**
     * 首次提及章节ID
     */
    @Column(name = "first_mentioned_chapter_id")
    private Long firstMentionedChapterId;
    
    /**
     * 重要程度（high, medium, low）
     */
    @Column(length = 20)
    private String importance;
}
