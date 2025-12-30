package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风格转换记录实体
 */
@Entity
@Table(name = "style_conversions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleConversion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "source_text", nullable = false, columnDefinition = "TEXT")
    private String sourceText;
    
    @Column(name = "converted_text", nullable = false, columnDefinition = "TEXT")
    private String convertedText;
    
    @Column(name = "source_style_id")
    private Long sourceStyleId;
    
    @Column(name = "target_style_id", nullable = false)
    private Long targetStyleId;
    
    @Column(name = "conversion_notes", columnDefinition = "TEXT")
    private String conversionNotes;
    
    @Column(name = "style_match_score")
    private Integer styleMatchScore;
    
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
