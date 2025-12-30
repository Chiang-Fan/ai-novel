package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 场景氛围实体
 */
@Entity
@Table(name = "scene_atmosphere")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneAtmosphere {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scene_id", nullable = false)
    private Long sceneId;
    
    @Column(name = "template_id")
    private Long templateId;
    
    @Column(name = "atmosphere_type", nullable = false, length = 50)
    private String atmosphereType;
    
    @Column(name = "generated_text", nullable = false, columnDefinition = "TEXT")
    private String generatedText;
    
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
