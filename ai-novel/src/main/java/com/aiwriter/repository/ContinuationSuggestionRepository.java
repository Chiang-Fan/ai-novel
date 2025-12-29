package com.aiwriter.repository;

import com.aiwriter.entity.ContinuationSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 续写建议Repository
 */
@Repository
public interface ContinuationSuggestionRepository extends JpaRepository<ContinuationSuggestion, Long> {
    
    /**
     * 根据小说ID查找所有续写建议（按优先级排序）
     */
    List<ContinuationSuggestion> findByNovelIdOrderByPriorityDesc(Long novelId);
    
    /**
     * 根据分析ID查找续写建议
     */
    List<ContinuationSuggestion> findByAnalysisIdOrderByPriorityDesc(Long analysisId);
    
    /**
     * 查找未采用的续写建议
     */
    List<ContinuationSuggestion> findByNovelIdAndIsAdoptedFalseOrderByPriorityDesc(Long novelId);
    
    /**
     * 查找已采用的续写建议
     */
    List<ContinuationSuggestion> findByNovelIdAndIsAdoptedTrueOrderByCreatedAtDesc(Long novelId);
    
    /**
     * 查找最新的N个未采用建议
     */
    @Query("SELECT cs FROM ContinuationSuggestion cs WHERE cs.novelId = ?1 AND cs.isAdopted = false ORDER BY cs.createdAt DESC, cs.priority DESC LIMIT ?2")
    List<ContinuationSuggestion> findTopNUnadopted(Long novelId, int limit);
    
    /**
     * 统计小说的续写建议数
     */
    long countByNovelId(Long novelId);
    
    /**
     * 统计未采用的建议数
     */
    long countByNovelIdAndIsAdoptedFalse(Long novelId);
}
