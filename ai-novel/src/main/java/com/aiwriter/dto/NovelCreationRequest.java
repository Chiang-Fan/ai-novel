package com.aiwriter.dto;

import lombok.Data;

/**
 * 智能新建小说请求
 */
@Data
public class NovelCreationRequest {
    
    private String initialIdea;  // 初始创意或方向
    
    private String genre;        // 小说类型
    
    private String targetAudience; // 目标读者
    
    private String writingStyle;   // 写作风格
    
    private Integer expectedWordCount; // 预期字数
    
    private String currentStage; // 当前阶段：IDEA, GENRE, OUTLINE, CHARACTERS, WORLD_SETTING, COMPLETE
}