package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 写作风格实体
 */
@Entity
@Table(name = "writing_styles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingStyle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "author", length = 200)
    private String author;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "sample_text", nullable = false, columnDefinition = "TEXT")
    private String sampleText;
    
    @Column(name = "style_features", columnDefinition = "TEXT")
    private String styleFeatures;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "language_complexity", length = 20)
    private String languageComplexity;
    
    @Column(name = "sentence_length", length = 20)
    private String sentenceLength;
    
    @Column(name = "tone", length = 50)
    private String tone;
    
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;
    
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Column(name = "is_system")
    @Builder.Default
    private Boolean isSystem = false;
    
    @Column(name = "created_by")
    private Long createdBy;
    
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
