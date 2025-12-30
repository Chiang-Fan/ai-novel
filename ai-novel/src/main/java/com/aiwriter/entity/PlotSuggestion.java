package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 情节发展建议实体
 */
@Entity
@Table(name = "plot_suggestions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotSuggestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "suggestion_type", nullable = false, length = 50)
    private String suggestionType;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;
    
    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "MEDIUM";
    
    @Column(name = "impact_score")
    private Integer impactScore;
    
    @Column(name = "related_plot_hook_id")
    private Long relatedPlotHookId;
    
    @Column(name = "related_character_id")
    private Long relatedCharacterId;
    
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";
    
    @Column(name = "applied_chapter_id")
    private Long appliedChapterId;
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    
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
