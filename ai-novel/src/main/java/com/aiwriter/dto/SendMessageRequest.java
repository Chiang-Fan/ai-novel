package com.aiwriter.dto;

import lombok.Data;

/**
 * 发送消息请求
 */
@Data
public class SendMessageRequest {
    
    private Long sessionId;
    
    private String content;
    
    /**
     * 是否包含完整上下文
     * true: 包含小说、章节、角色等完整信息
     * false: 仅包含对话历史
     */
    private Boolean includeContext = true;
}
