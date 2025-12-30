package com.aiwriter.repository;

import com.aiwriter.entity.PlotSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 情节发展建议Repository
 */
@Repository
public interface PlotSuggestionRepository extends JpaRepository<PlotSuggestion, Long> {
    
    /**
     * 查找小说的所有建议
     */
    List<PlotSuggestion> findByNovelIdOrderByPriorityDescCreatedAtDesc(Long novelId);
    
    /**
     * 按状态查找
     */
    List<PlotSuggestion> findByNovelIdAndStatusOrderByPriorityDescCreatedAtDesc(Long novelId, String status);
    
    /**
     * 按类型查找
     */
    List<PlotSuggestion> findByNovelIdAndSuggestionTypeOrderByCreatedAtDesc(Long novelId, String suggestionType);
    
    /**
     * 查找高优先级建议
     */
    List<PlotSuggestion> findByNovelIdAndPriorityInOrderByCreatedAtDesc(Long novelId, List<String> priorities);
}
