package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 冲突追踪实体
 */
@Entity
@Table(name = "conflict_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "conflict_type", nullable = false, length = 50)
    private String conflictType;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "involved_characters", columnDefinition = "TEXT")
    private String involvedCharacters;
    
    @Column(name = "intensity_level")
    @Builder.Default
    private Integer intensityLevel = 50;
    
    @Column(name = "introduced_chapter_id")
    private Long introducedChapterId;
    
    @Column(name = "escalation_points", columnDefinition = "TEXT")
    private String escalationPoints;
    
    @Column(name = "resolution_chapter_id")
    private Long resolutionChapterId;
    
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";
    
    @Column(name = "resolution_type", length = 50)
    private String resolutionType;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
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
