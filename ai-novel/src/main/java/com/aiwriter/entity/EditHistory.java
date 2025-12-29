package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "edit_history")
public class EditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "operation_type", nullable = false, length = 20)
    private String operationType; // CREATE, UPDATE, DELETE, REGENERATE
    
    @Column(name = "content_before", columnDefinition = "TEXT")
    private String contentBefore;
    
    @Column(name = "content_after", columnDefinition = "TEXT")
    private String contentAfter;
    
    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;
    
    @Column(name = "word_count_diff")
    private Integer wordCountDiff;
    
    @Column(name = "edit_reason", length = 200)
    private String editReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
