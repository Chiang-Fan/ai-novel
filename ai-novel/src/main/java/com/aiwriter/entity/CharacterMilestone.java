package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色里程碑事件实体
 */
@Entity
@Table(name = "character_milestones", indexes = {
    @Index(name = "idx_character_id_milestones", columnList = "character_id"),
    @Index(name = "idx_chapter_id_milestones", columnList = "chapter_id"),
    @Index(name = "idx_milestone_type", columnList = "milestone_type")
})
@Data
public class CharacterMilestone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "character_id", nullable = false)
    private Long characterId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    /**
     * 里程碑类型: POSITIVE(正面), NEGATIVE(负面), NEUTRAL(中性)
     */
    @Column(name = "milestone_type", length = 50)
    private String milestoneType;
    
    @Column(name = "event_name", length = 200, nullable = false)
    private String eventName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 影响程度 (1-10)
     */
    @Column(name = "impact_level")
    private Integer impactLevel;
    
    /**
     * 影响的属性（JSON格式）
     * 示例: {"swordPlay": 3, "confidence": 2}
     */
    @Column(name = "affected_attributes", columnDefinition = "JSON")
    private String affectedAttributes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
