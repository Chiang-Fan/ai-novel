package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息响应
 */
@Data
public class MessageResponse {
    
    private Long id;
    
    /**
     * 角色：USER, ASSISTANT, SYSTEM
     */
    private String role;
    
    private String content;
    
    /**
     * 消息类型：TEXT, SUGGESTION, CODE_EXAMPLE
     */
    private String messageType;
    
    private Integer tokenCount;
    
    private Integer responseTime;
    
    private LocalDateTime createdAt;
}
