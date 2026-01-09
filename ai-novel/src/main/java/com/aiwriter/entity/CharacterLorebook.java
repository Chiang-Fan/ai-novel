package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "character_lorebook")
public class CharacterLorebook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "character_id", nullable = false)
    private Long characterId;
    
    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;
    
    @Column(columnDefinition = "TEXT")
    private String lore;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 5; // 1-10，10为最高优先级
    
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