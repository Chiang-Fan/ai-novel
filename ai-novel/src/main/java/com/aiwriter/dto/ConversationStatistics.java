package com.aiwriter.dto;

import lombok.Data;
import java.util.Map;

/**
 * 对话统计信息
 */
@Data
public class ConversationStatistics {
    
    /**
     * 总会话数
     */
    private Integer totalSessions;
    
    /**
     * 活跃会话数
     */
    private Integer activeSessions;
    
    /**
     * 总消息数
     */
    private Integer totalMessages;
    
    /**
     * 平均每会话消息数
     */
    private Double avgMessagesPerSession;
    
    /**
     * 会话类型分布
     * key: 会话类型, value: 数量
     */
    private Map<String, Integer> sessionTypeDistribution;
}
