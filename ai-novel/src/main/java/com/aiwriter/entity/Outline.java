package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "outlines")
public class Outline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "node_type", nullable = false, length = 20)
    private String nodeType; // ARC, VOLUME, CHAPTER, SECTION
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "target_word_count")
    private Integer targetWordCount;
    
    @Column(name = "key_events", columnDefinition = "TEXT")
    private String keyEvents; // JSON数组
    
    @Column(name = "character_focus", columnDefinition = "TEXT")
    private String characterFocus; // JSON数组
    
    @Column(name = "plot_points", columnDefinition = "TEXT")
    private String plotPoints; // JSON数组
    
    @Column(name = "themes", columnDefinition = "TEXT")
    private String themes; // JSON数组
    
    @Column(name = "status", length = 20)
    private String status = "PLANNED"; // PLANNED, IN_PROGRESS, COMPLETED
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
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