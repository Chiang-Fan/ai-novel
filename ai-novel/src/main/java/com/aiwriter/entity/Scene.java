package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 场景实体
 */
@Entity
@Table(name = "scenes", indexes = {
    @Index(name = "idx_novel_id_scenes", columnList = "novel_id"),
    @Index(name = "idx_scene_type", columnList = "scene_type"),
    @Index(name = "idx_is_recurring", columnList = "is_recurring")
})
@Data
public class Scene {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(length = 200, nullable = false)
    private String name;
    
    /**
     * 场景类型: INDOOR(室内), OUTDOOR(室外), SPECIAL(特殊), LOCATION(地点), EVENT(事件), TIME_PERIOD(时期)
     */
    @Column(name = "scene_type", length = 50)
    private String sceneType;
    
    @Column(length = 200)
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String atmosphere;
    
    @Column(name = "time_period", length = 100)
    private String timePeriod;
    
    @Column(length = 50)
    private String weather;
    
    /**
     * 道具JSON数组
     */
    @Column(columnDefinition = "TEXT")
    private String props;
    
    /**
     * 相关角色ID JSON数组
     */
    @Column(name = "involved_characters", columnDefinition = "TEXT")
    private String involvedCharacters;
    
    /**
     * 出现章节JSON数组
     */
    @Column(name = "chapter_references", columnDefinition = "TEXT")
    private String chapterReferences;
    
    /**
     * 标签（逗号分隔）
     */
    @Column(length = 500)
    private String tags;
    
    /**
     * 重要性评分 (1-10)
     */
    @Column(name = "importance_score")
    private Integer importanceScore;
    
    /**
     * 是否重复出现的场景
     */
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;
    
    /**
     * 备注
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
