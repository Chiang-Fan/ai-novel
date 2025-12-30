package com.aiwriter.dto;

import com.aiwriter.entity.PlotHook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 伏笔数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotHookDto {
    
    private Long id;
    private Long novelId;
    private String title;
    private String description;
    private Integer plantedInChapter;
    private Integer expectedChapter;
    private Integer triggeredInChapter;
    private Integer resolvedInChapter;
    private PlotHook.Status status;
    private PlotHook.ForeshadowingType type;
    private Integer priority;
    private String resolutionNote;
    private List<String> relatedCharacters;
    private Boolean isAutoDetected;
    private String contentReference;
    private String notes;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 计算属性
    private Integer duration;  // 持续章节数
    private Boolean isOverdue; // 是否超期
}
