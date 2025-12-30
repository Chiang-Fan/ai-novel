package com.aiwriter.dto;

import lombok.Data;

/**
 * 创建会话请求
 */
@Data
public class CreateSessionRequest {
    
    private Long novelId;
    
    private Long chapterId; // 可选
    
    private String sessionTitle; // 可选
    
    /**
     * 会话类型：
     * WRITING_ADVICE - 创作咨询
     * PLOT_CONSULTATION - 情节讨论
     * STYLE_GUIDANCE - 文风建议
     * GENERAL - 一般交流
     */
    private String sessionType;
    
    private String initialMessage; // 可选：初始消息
}
