package com.aiwriter.repository;

import com.aiwriter.entity.ConversationSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI对话会话仓库
 */
@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {
    
    /**
     * 按小说ID查询会话列表（按创建时间倒序）
     */
    List<ConversationSession> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    
    /**
     * 按小说ID和状态查询会话列表（按创建时间倒序）
     */
    List<ConversationSession> findByNovelIdAndStatusOrderByCreatedAtDesc(Long novelId, String status);
    
    /**
     * 按小说ID分页查询
     */
    Page<ConversationSession> findByNovelIdOrderByUpdatedAtDesc(Long novelId, Pageable pageable);
    
    /**
     * 按章节ID查询会话列表
     */
    List<ConversationSession> findByChapterId(Long chapterId);
    
    /**
     * 统计小说的会话数量
     */
    Long countByNovelId(Long novelId);
    
    /**
     * 统计小说的活跃会话数量
     */
    Long countByNovelIdAndStatus(Long novelId, String status);
}
