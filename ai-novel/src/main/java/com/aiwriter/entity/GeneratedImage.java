package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI生成图片实体
 */
@Data
@Entity
@Table(name = "generated_images")
public class GeneratedImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long novelId;
    
    private Long chapterId;
    
    private Long characterId;
    
    private Long sceneId;
    
    @Column(nullable = false, length = 50)
    private String imageType; // CHARACTER, SCENE, COVER, ILLUSTRATION
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;
    
    @Column(columnDefinition = "TEXT")
    private String negativePrompt;
    
    @Column(length = 100)
    private String style; // REALISTIC, ANIME, COMIC, WATERCOLOR, OIL_PAINTING, SKETCH
    
    @Column(length = 1000)
    private String imageUrl;
    
    @Column(length = 500)
    private String localPath;
    
    private Integer width;
    
    private Integer height;
    
    private Long fileSize;
    
    @Column(length = 100)
    private String aiModel;
    
    @Column(columnDefinition = "TEXT")
    private String generationParams; // JSON格式
    
    private Integer qualityScore;
    
    @Column(nullable = false)
    private Boolean isAdopted = false;
    
    private LocalDateTime adoptedAt;
    
    @Column(columnDefinition = "TEXT")
    private String tags; // JSON数组
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
