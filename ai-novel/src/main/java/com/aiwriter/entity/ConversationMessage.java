package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI对话消息实体
 */
@Data
@Entity
@Table(name = "conversation_messages")
public class ConversationMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    
    /**
     * 角色：USER, ASSISTANT, SYSTEM
     */
    @Column(name = "role", length = 20, nullable = false)
    private String role;
    
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    /**
     * 消息类型：TEXT, SUGGESTION, CODE_EXAMPLE
     */
    @Column(name = "message_type", length = 50)
    private String messageType;
    
    /**
     * 元数据JSON
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "token_count")
    private Integer tokenCount;
    
    @Column(name = "response_time")
    private Integer responseTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (messageType == null) {
            messageType = "TEXT";
        }
    }
}
