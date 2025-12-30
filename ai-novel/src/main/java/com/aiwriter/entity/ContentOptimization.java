package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 内容优化记录实体
 */
@Entity
@Table(name = "content_optimizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentOptimization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "original_text", nullable = false, columnDefinition = "TEXT")
    private String originalText;
    
    @Column(name = "optimized_text", nullable = false, columnDefinition = "TEXT")
    private String optimizedText;
    
    @Column(name = "optimization_type", nullable = false, length = 50)
    private String optimizationType;
    
    @Column(name = "changes_summary", columnDefinition = "TEXT")
    private String changesSummary;
    
    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;
    
    @Column(name = "ai_model", length = 100)
    private String aiModel;
    
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;
    
    @Column(name = "rating")
    private Integer rating;
    
    @Column(name = "is_applied")
    @Builder.Default
    private Boolean isApplied = false;
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
