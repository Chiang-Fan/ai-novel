package com.ainovel.novelcraft.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "plot_hooks")
@Data
public class PlotHook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Column(name = "context_snippet", columnDefinition = "TEXT")
    private String contextSnippet;
    
    @Column(name = "min_chapter", nullable = false)
    private Integer minChapter;
    
    @Column(name = "max_chapter", nullable = false)
    private Integer maxChapter;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum Status {
        PENDING, TRIGGERED, RESOLVED
    }
}