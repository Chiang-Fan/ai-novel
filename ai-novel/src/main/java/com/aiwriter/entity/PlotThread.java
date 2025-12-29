package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 伏笔实体
 * 对应Python的PlotThread模型
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "plot_threads", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class PlotThread extends BaseEntity {
    
    /**
     * 所属小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    /**
     * 伏笔标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 伏笔描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 埋笔章节ID
     */
    @Column(name = "planted_in_chapter_id")
    private Long plantedInChapterId;
    
    /**
     * 揭示章节ID
     */
    @Column(name = "revealed_in_chapter_id")
    private Long revealedInChapterId;
    
    /**
     * 伏笔状态（planted, developing, revealed）
     */
    @Column(length = 20)
    private String status;
    
    /**
     * 重要程度（high, medium, low）
     */
    @Column(length = 20)
    private String importance;
    
    /**
     * 相关角色IDs（JSON数组）
     */
    @Column(name = "related_character_ids", columnDefinition = "TEXT")
    private String relatedCharacterIds;
    
    /**
     * 伏笔笔记
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
}
