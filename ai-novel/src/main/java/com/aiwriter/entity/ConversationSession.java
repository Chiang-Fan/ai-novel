package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI对话会话实体
 */
@Data
@Entity
@Table(name = "conversation_sessions")
public class ConversationSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "session_title", length = 200)
    private String sessionTitle;
    
    /**
     * 会话类型：
     * WRITING_ADVICE - 创作咨询
     * PLOT_CONSULTATION - 情节讨论
     * STYLE_GUIDANCE - 文风建议
     * GENERAL - 一般交流
     */
    @Column(name = "session_type", length = 50)
    private String sessionType;
    
    /**
     * 上下文快照JSON
     */
    @Column(name = "context_snapshot", columnDefinition = "TEXT")
    private String contextSnapshot;
    
    @Column(name = "total_messages")
    private Integer totalMessages = 0;
    
    /**
     * 状态：ACTIVE, ARCHIVED
     */
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (totalMessages == null) {
            totalMessages = 0;
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
