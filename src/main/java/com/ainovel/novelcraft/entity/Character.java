package com.ainovel.novelcraft.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
@Data
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "core_belief", length = 1000)
    private String coreBelief;
    
    @Column(name = "evolving_belief", length = 1000)
    private String evolvingBelief;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_arc_stage")
    private ArcStage currentArcStage;
    
    @Column(name = "last_updated_chapter")
    private Integer lastUpdatedChapter;
    
    @Column(name = "created_at")
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
    
    public enum ArcStage {
        INITIAL, CHALLENGED, CRISIS, TRANSFORMED
    }
}