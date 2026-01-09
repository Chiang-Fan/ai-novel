package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "plot_foreshadowing")
public class PlotForeshadowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "planted_in_chapter", nullable = false)
    private Integer plantedInChapter;
    
    @Column(name = "expected_chapter")
    private Integer expectedChapter;
    
    @Column(name = "triggered_in_chapter")
    private Integer triggeredInChapter;
    
    @Column(name = "resolved_in_chapter")
    private Integer resolvedInChapter;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type; // EXPLICIT, IMPLICIT, CHEKHOV_GUN
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 5; // 1-10，10为最高优先级
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, HINTED, TRIGGERED, RESOLVED
    
    @Column(name = "is_auto_detected", nullable = false)
    private Boolean isAutoDetected = false;
    
    @Column(name = "content_reference", columnDefinition = "TEXT")
    private String contentReference;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;
    
    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
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