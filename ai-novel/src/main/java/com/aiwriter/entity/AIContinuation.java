package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI续写记录实体
 */
@Entity
@Table(name = "ai_continuations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIContinuation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "source_text", nullable = false, columnDefinition = "TEXT")
    private String sourceText;
    
    @Column(name = "continuation_text", nullable = false, columnDefinition = "TEXT")
    private String continuationText;
    
    @Column(length = 50)
    private String style;  // SERIOUS, LIGHT, SUSPENSE, ROMANTIC, ACTION
    
    @Column(length = 50)
    private String length;  // SENTENCE, PARAGRAPH, SECTION
    
    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;
    
    @Column(name = "ai_model", length = 100)
    private String aiModel;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Column
    private Integer rating;  // 1-5
    
    @Column(name = "is_applied")
    private Boolean isApplied = false;
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
        if (isApplied == null) {
            isApplied = false;
        }
    }
}
