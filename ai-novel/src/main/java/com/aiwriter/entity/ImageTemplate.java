package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图片模板实体
 */
@Data
@Entity
@Table(name = "image_templates")
public class ImageTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String category; // CHARACTER, SCENE, COVER, ITEM
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptTemplate;
    
    @Column(columnDefinition = "TEXT")
    private String negativePromptTemplate;
    
    @Column(length = 100)
    private String defaultStyle;
    
    @Column(length = 50)
    private String recommendedSize;
    
    @Column(length = 1000)
    private String previewUrl;
    
    @Column(nullable = false)
    private Integer usageCount = 0;
    
    @Column(nullable = false)
    private Boolean isSystem = false;
    
    private Long createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
