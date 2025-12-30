package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 氛围模板实体
 */
@Entity
@Table(name = "atmosphere_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtmosphereTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String category;  // TIME, WEATHER, EMOTION, ACTION
    
    @Column(name = "atmosphere_type", nullable = false, length = 50)
    private String atmosphereType;  // PEACEFUL, TENSE, ROMANTIC, MYSTERIOUS等
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String keywords;  // JSON数组
    
    @Column(name = "sensory_details", columnDefinition = "TEXT")
    private String sensoryDetails;  // JSON对象
    
    @Column(name = "example_text", columnDefinition = "TEXT")
    private String exampleText;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "is_system")
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
        if (usageCount == null) {
            usageCount = 0;
        }
        if (isSystem == null) {
            isSystem = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
