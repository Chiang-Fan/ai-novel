package com.aiwriter.dto;

import com.aiwriter.entity.PlotHook;
import lombok.Data;

import java.util.List;

/**
 * 更新伏笔请求
 */
@Data
public class UpdatePlotHookRequest {
    
    private String title;
    private String description;
    private Integer expectedChapter;
    private PlotHook.ForeshadowingType type;
    private Integer priority;
    private List<String> relatedCharacters;
    private String notes;
}
