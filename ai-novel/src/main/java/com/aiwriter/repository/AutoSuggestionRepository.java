package com.aiwriter.repository;

import com.aiwriter.entity.AutoSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AutoSuggestionRepository extends JpaRepository<AutoSuggestion, Long> {

    /**
     * 获取小说的所有推荐
     */
    List<AutoSuggestion> findByNovelIdOrderByPriorityDescRelevanceScoreDesc(Long novelId);

    /**
     * 获取小说的活跃推荐
     */
    List<AutoSuggestion> findByNovelIdAndStatusOrderByPriorityDescRelevanceScoreDesc(
            Long novelId, AutoSuggestion.SuggestionStatus status);

    /**
     * 获取指定类型的推荐
     */
    List<AutoSuggestion> findByNovelIdAndTypeAndStatusOrderByPriorityDescRelevanceScoreDesc(
            Long novelId, AutoSuggestion.SuggestionType type, AutoSuggestion.SuggestionStatus status);

    /**
     * 获取章节的推荐
     */
    List<AutoSuggestion> findByChapterIdOrderByPriorityDescRelevanceScoreDesc(Long chapterId);

    /**
     * 获取高优先级推荐（priority >= threshold）
     */
    @Query("SELECT s FROM AutoSuggestion s WHERE s.novelId = :novelId " +
           "AND s.status = 'ACTIVE' AND s.priority >= :threshold " +
           "ORDER BY s.priority DESC, s.relevanceScore DESC")
    List<AutoSuggestion> findHighPrioritySuggestions(
            @Param("novelId") Long novelId, @Param("threshold") Integer threshold);

    /**
     * 获取已过期的推荐
     */
    @Query("SELECT s FROM AutoSuggestion s WHERE s.novelId = :novelId " +
           "AND s.status = 'ACTIVE' AND s.expiresAt < :now")
    List<AutoSuggestion> findExpiredSuggestions(
            @Param("novelId") Long novelId, @Param("now") LocalDateTime now);

    /**
     * 统计各状态数量
     */
    @Query("SELECT s.status, COUNT(s) FROM AutoSuggestion s " +
           "WHERE s.novelId = :novelId GROUP BY s.status")
    List<Object[]> countByStatus(@Param("novelId") Long novelId);

    /**
     * 统计各类型数量
     */
    @Query("SELECT s.type, COUNT(s) FROM AutoSuggestion s " +
           "WHERE s.novelId = :novelId AND s.status = 'ACTIVE' GROUP BY s.type")
    List<Object[]> countActiveByType(@Param("novelId") Long novelId);

    /**
     * 获取参考对象的推荐
     */
    List<AutoSuggestion> findByNovelIdAndReferenceTypeAndReferenceId(
            Long novelId, String referenceType, Long referenceId);

    /**
     * 删除小说的所有推荐
     */
    void deleteByNovelId(Long novelId);
}
