package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 伏笔管理系统数据传输对象
 */
@Data
public class PlotForeshadowingDto {
    
    private Long id;
    
    private Long novelId;
    
    private Long chapterId;
    
    private Integer plantedInChapter;
    
    private Integer expectedChapter;
    
    private Integer triggeredInChapter;
    
    private Integer resolvedInChapter;
    
    private String title;
    
    private String description;
    
    private String type; // EXPLICIT, IMPLICIT, CHEKHOV_GUN
    
    private Integer priority = 5; // 1-10，10为最高优先级
    
    private String status; // PENDING, HINTED, TRIGGERED, RESOLVED
    
    private Boolean isAutoDetected = false;
    
    private String contentReference;
    
    private String resolutionNote;
    
    private String notes;
    
    private LocalDateTime triggeredAt;
    
    private LocalDateTime resolvedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}