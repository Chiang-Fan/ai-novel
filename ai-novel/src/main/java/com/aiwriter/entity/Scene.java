package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scenes")
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(name = "scene_type", nullable = false, length = 20)
    private String sceneType; // LOCATION, EVENT, TIME_PERIOD
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String atmosphere; // 氛围描述
    
    @Column(name = "time_period", length = 100)
    private String timePeriod; // 时间段
    
    @Column(length = 200)
    private String location; // 地点
    
    @Column(length = 50)
    private String weather; // 天气
    
    @Column(columnDefinition = "TEXT")
    private String props; // 道具JSON数组
    
    @Column(name = "involved_characters", columnDefinition = "TEXT")
    private String involvedCharacters; // 相关角色ID JSON数组
    
    @Column(name = "chapter_references", columnDefinition = "TEXT")
    private String chapterReferences; // 出现章节JSON数组
    
    @Column(columnDefinition = "TEXT")
    private String notes; // 备注
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
