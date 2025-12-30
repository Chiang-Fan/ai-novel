package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 情节推演记录实体
 */
@Entity
@Table(name = "plot_projections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotProjection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "current_chapter_id", nullable = false)
    private Long currentChapterId;
    
    @Column(name = "projection_text", nullable = false, columnDefinition = "TEXT")
    private String projectionText;
    
    @Column(name = "projection_type", length = 50)
    private String projectionType;
    
    @Column(name = "chapters_ahead")
    private Integer chaptersAhead;
    
    @Column(name = "confidence_score")
    private Integer confidenceScore;
    
    @Column(name = "key_events", columnDefinition = "TEXT")
    private String keyEvents;
    
    @Column(name = "character_changes", columnDefinition = "TEXT")
    private String characterChanges;
    
    @Column(name = "plot_threads", columnDefinition = "TEXT")
    private String plotThreads;
    
    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;
    
    @Column(name = "ai_model", length = 100)
    private String aiModel;
    
    @Column(name = "is_adopted")
    @Builder.Default
    private Boolean isAdopted = false;
    
    @Column(name = "adopted_at")
    private LocalDateTime adoptedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
