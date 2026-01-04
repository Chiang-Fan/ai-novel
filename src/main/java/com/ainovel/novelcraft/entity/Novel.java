package com.ainovel.novelcraft.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "novels")
@Data
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 5000)
    private String outline;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "core_themes", columnDefinition = "JSON")
    private String coreThemes; // JSON string
    
    @Column(name = "recurring_motifs", columnDefinition = "JSON")
    private String recurringMotifs; // JSON string
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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