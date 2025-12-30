package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 场景变化记录实体
 */
@Entity
@Table(name = "scene_changes", indexes = {
    @Index(name = "idx_scene_id_changes", columnList = "scene_id"),
    @Index(name = "idx_change_type", columnList = "change_type"),
    @Index(name = "idx_created_at_changes", columnList = "created_at")
})
@Data
public class SceneChange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scene_id", nullable = false)
    private Long sceneId;
    
    /**
     * 变化类型: REBUILD(重建), DAMAGE(破坏), SEASONAL(季节性), DECORATION(装饰)等
     */
    @Column(name = "change_type", length = 50)
    private String changeType;
    
    @Column(name = "change_desc", columnDefinition = "TEXT", nullable = false)
    private String changeDesc;
    
    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;
    
    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;
    
    @Column(name = "related_chapter_id")
    private Long relatedChapterId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
