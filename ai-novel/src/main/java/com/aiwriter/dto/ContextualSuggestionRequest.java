package com.aiwriter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 上下文相关的建议请求
 */
@Data
public class ContextualSuggestionRequest {
    
    @NotNull(message = "小说ID不能为空")
    private Long novelId;
    
    private String currentContent;  // 当前内容
    
    private String currentScene;    // 当前场景
    
    private String currentConflict; // 当前冲突
    
    private String protagonistName; // 主角名称
    
    private String emotionalTone;   // 情感基调
    
    private List<String> activeCharacters; // 活跃角色
    
    private Integer expectedWordCount = 2000; // 期望字数
    
    private Integer count = 3; // 建议数量
}