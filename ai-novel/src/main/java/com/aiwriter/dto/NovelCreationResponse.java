package com.aiwriter.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 智能新建小说响应
 */
@Data
public class NovelCreationResponse {
    
    private String question;  // AI提出的问题
    
    private List<String> suggestions; // AI提供的建议
    
    private String currentStage; // 当前阶段
    
    private Map<String, Object> context; // 当前上下文信息
    
    private Boolean isComplete; // 是否完成
    
    private Long novelId; // 小说ID（完成时返回）
}