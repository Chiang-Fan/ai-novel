package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

/**
 * 大纲节点实体
 * 对应Python的OutlineNode模型
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "outline_nodes", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
public class OutlineNode extends BaseEntity {
    
    /**
     * 所属小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    /**
     * 父节点ID（用于树形结构）
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 节点标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 节点内容/描述
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 节点类型（volume, scene, chapter）
     */
    @Column(name = "node_type", length = 20)
    private String nodeType;
    
    /**
     * 节点层级（0=根，1=一级，2=二级...）
     */
    @Column(name = "level")
    private Integer level = 0;
    
    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    /**
     * 是否完成
     */
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    /**
     * 预计字数
     */
    @Column(name = "estimated_words")
    private Integer estimatedWords;
    
    /**
     * 实际字数
     */
    @Column(name = "actual_words")
    private Integer actualWords = 0;
}
