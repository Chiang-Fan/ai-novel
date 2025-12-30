package com.aiwriter.repository;

import com.aiwriter.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI对话消息仓库
 */
@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    
    /**
     * 按会话ID查询消息列表（按创建时间排序）
     */
    List<ConversationMessage> findBySessionIdOrderByCreatedAt(Long sessionId);
    
    /**
     * 统计会话的消息数量
     */
    Long countBySessionId(Long sessionId);
    
    /**
     * 查询会话的最近N条消息
     */
    @Query(value = "SELECT * FROM conversation_messages WHERE session_id = ?1 ORDER BY created_at DESC LIMIT ?2", 
           nativeQuery = true)
    List<ConversationMessage> findRecentMessagesBySessionId(Long sessionId, int limit);
    
    /**
     * 删除会话的所有消息
     */
    void deleteBySessionId(Long sessionId);
}
