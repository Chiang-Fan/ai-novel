package com.aiwriter.repository;

import com.aiwriter.entity.WritingSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 写作建议Repository
 */
@Repository
public interface WritingSuggestionRepository extends JpaRepository<WritingSuggestion, Long> {
    
    /**
     * 查询章节的所有建议
     */
    List<WritingSuggestion> findByChapterIdOrderByPriorityDescCreatedAtDesc(Long chapterId);
    
    /**
     * 按状态查询建议
     */
    List<WritingSuggestion> findByChapterIdAndStatus(Long chapterId, String status);
    
    /**
     * 按类型查询建议
     */
    List<WritingSuggestion> findByChapterIdAndSuggestionType(Long chapterId, String suggestionType);
    
    /**
     * 按优先级查询建议
     */
    List<WritingSuggestion> findByChapterIdAndPriorityOrderByCreatedAtDesc(
            Long chapterId, String priority);
    
    /**
     * 统计待处理建议数量
     */
    long countByChapterIdAndStatus(Long chapterId, String status);
    
    /**
     * 查询关联大纲的建议
     */
    List<WritingSuggestion> findByRelatedOutlineId(Long outlineId);
}
