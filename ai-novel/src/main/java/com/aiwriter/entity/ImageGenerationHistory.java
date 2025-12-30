package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图片生成历史实体
 */
@Data
@Entity
@Table(name = "image_generation_history")
public class ImageGenerationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long novelId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;
    
    @Column(length = 100)
    private String style;
    
    @Column(nullable = false)
    private Integer batchSize = 1;
    
    @Column(nullable = false)
    private Integer generatedCount = 0;
    
    @Column(nullable = false)
    private Integer successCount = 0;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal cost;
    
    private Integer durationSeconds;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
