package com.ainovel.novelcraft.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapters")
@Data
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;
    
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(length = 1000)
    private String summary;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "scene_type")
    private SceneType sceneType;
    
    @Column(name = "is_high_stakes")
    private Boolean isHighStakes = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
        
    @Column(name = "generation_direction", columnDefinition = "TEXT")
    private String generationDirection;
        
    @Column(name = "banned_elements")
    private String bannedElements;
        
    @Column(name = "mood")
    private String mood;
        
    @Column(name = "generation_prompt", columnDefinition = "TEXT")
    private String generationPrompt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum SceneType {
        SETUP, CONFLICT, TURNING_POINT, REFLECTION
    }
}