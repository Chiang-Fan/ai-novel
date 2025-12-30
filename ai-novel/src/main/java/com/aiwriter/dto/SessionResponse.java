package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话响应
 */
@Data
public class SessionResponse {
    
    private Long id;
    
    private Long novelId;
    
    private Long chapterId;
    
    private String sessionTitle;
    
    private String sessionType;
    
    private Integer totalMessages;
    
    /**
     * 状态：ACTIVE, ARCHIVED
     */
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    /**
     * 最近的3条消息
     */
    private List<MessageResponse> recentMessages;
}
