package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

/**
 * 角色关系实体
 * 对应Python的CharacterRelationship模型
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "character_relationships", indexes = {
    @Index(name = "idx_character1", columnList = "character1_id"),
    @Index(name = "idx_character2", columnList = "character2_id")
})
public class CharacterRelationship extends BaseEntity {
    
    /**
     * 角色1的ID
     */
    @Column(name = "character1_id", nullable = false)
    private Long character1Id;
    
    /**
     * 角色2的ID
     */
    @Column(name = "character2_id", nullable = false)
    private Long character2Id;
    
    /**
     * 关系类型（family, friend, lover, enemy, mentor, rival等）
     */
    @Column(name = "relationship_type", length = 50, nullable = false)
    private String relationshipType;
    
    /**
     * 关系描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 亲密度（0-100）
     */
    private Integer intimacy = 50;
    
    /**
     * 关系建立章节ID
     */
    @Column(name = "established_in_chapter_id")
    private Long establishedInChapterId;
    
    /**
     * 关系状态（active, broken, evolving）
     */
    @Column(length = 20)
    private String status;
}
