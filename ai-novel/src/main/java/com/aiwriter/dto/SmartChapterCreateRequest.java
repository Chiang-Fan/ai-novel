package com.aiwriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 智能章节创建请求
 * 支持自动提取章节元数据
 */
@Data
public class SmartChapterCreateRequest {
    
    /**
     * 所属小说ID
     */
    @NotNull(message = "小说ID不能为空")
    private Long novelId;
    
    /**
     * 章节正文内容（必填）
     */
    @NotBlank(message = "章节内容不能为空")
    private String content;
    
    /**
     * 章节标题（可选，如为空则自动提取）
     */
    private String title;
    
    /**
     * 关联场景ID（可选）
     */
    private Long sceneId;
    
    /**
     * 关联大纲节点ID（可选）
     */
    private Long outlineNodeId;
    
    /**
     * 是否启用深度分析（默认true）
     * true: 深度分析，提取角色关系、伏笔等
     * false: 快速分析，仅提取基础信息
     */
    private Boolean deepAnalysis = true;
    
    /**
     * 是否自动生成续写方向（默认true）
     */
    private Boolean generateContinuationDirection = true;
}
