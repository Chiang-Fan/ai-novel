package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 场景使用记录实体
 */
@Entity
@Table(name = "scene_usages", indexes = {
    @Index(name = "idx_scene_id_usages", columnList = "scene_id"),
    @Index(name = "idx_chapter_id_usages", columnList = "chapter_id"),
    @Index(name = "idx_usage_time", columnList = "usage_time")
})
@Data
public class SceneUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scene_id", nullable = false)
    private Long sceneId;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "usage_time", nullable = false)
    private LocalDateTime usageTime;
    
    @Column(name = "scene_state", columnDefinition = "TEXT")
    private String sceneState;
    
    /**
     * 天气: SUNNY(晴), RAINY(雨), CLOUDY(阴), SNOWY(雪)等
     */
    @Column(length = 50)
    private String weather;
    
    /**
     * 时段: MORNING(早晨), NOON(中午), EVENING(傍晚), NIGHT(夜晚)
     */
    @Column(name = "time_of_day", length = 50)
    private String timeOfDay;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (usageTime == null) {
            usageTime = LocalDateTime.now();
        }
    }
}
