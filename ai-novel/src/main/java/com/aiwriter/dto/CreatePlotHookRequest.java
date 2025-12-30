package com.aiwriter.dto;

import com.aiwriter.entity.PlotHook;
import lombok.Data;

import java.util.List;

/**
 * 创建伏笔请求
 */
@Data
public class CreatePlotHookRequest {
    
    private Long novelId;
    private String title;
    private String description;
    private Integer plantedInChapter;
    private Integer expectedChapter;
    private PlotHook.ForeshadowingType type;
    private Integer priority;
    private List<String> relatedCharacters;
    private String contentReference;
    private String notes;
}
