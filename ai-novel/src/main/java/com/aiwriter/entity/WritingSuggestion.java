package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 写作建议实体
 */
@Entity
@Table(name = "writing_suggestions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingSuggestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "suggestion_type", nullable = false, length = 50)
    private String suggestionType;  // PLOT, CONFLICT, CHARACTER, DIALOGUE, PACING
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String suggestion;
    
    @Column(columnDefinition = "TEXT")
    private String reasoning;
    
    @Column(length = 20)
    private String priority = "MEDIUM";  // LOW, MEDIUM, HIGH
    
    @Column(length = 20)
    private String status = "PENDING";  // PENDING, ACCEPTED, REJECTED
    
    @Column(name = "context_text", columnDefinition = "TEXT")
    private String contextText;
    
    @Column(name = "related_outline_id")
    private Long relatedOutlineId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (priority == null) {
            priority = "MEDIUM";
        }
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
