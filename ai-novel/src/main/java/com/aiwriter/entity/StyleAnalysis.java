package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风格分析结果实体
 */
@Entity
@Table(name = "style_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "text_sample", nullable = false, columnDefinition = "TEXT")
    private String textSample;
    
    @Column(name = "detected_style_id")
    private Long detectedStyleId;
    
    @Column(name = "analysis_result", nullable = false, columnDefinition = "TEXT")
    private String analysisResult;
    
    @Column(name = "confidence_score")
    private Integer confidenceScore;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
