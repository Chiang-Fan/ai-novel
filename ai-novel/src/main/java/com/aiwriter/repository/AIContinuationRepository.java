package com.aiwriter.repository;

import com.aiwriter.entity.AIContinuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI续写Repository
 */
@Repository
public interface AIContinuationRepository extends JpaRepository<AIContinuation, Long> {
    
    /**
     * 查询章节的所有续写
     */
    List<AIContinuation> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
    
    /**
     * 查询章节的特定风格续写
     */
    List<AIContinuation> findByChapterIdAndStyleOrderByVersionDesc(Long chapterId, String style);
    
    /**
     * 查询章节已应用的续写
     */
    Optional<AIContinuation> findByChapterIdAndIsAppliedTrue(Long chapterId);
    
    /**
     * 统计章节续写数量
     */
    long countByChapterId(Long chapterId);
    
    /**
     * 查询最新版本的续写
     */
    @Query("SELECT ac FROM AIContinuation ac WHERE ac.chapterId = :chapterId " +
           "AND ac.version = (SELECT MAX(ac2.version) FROM AIContinuation ac2 " +
           "WHERE ac2.chapterId = :chapterId AND ac2.style = ac.style)")
    List<AIContinuation> findLatestVersionsByChapterId(Long chapterId);
    
    /**
     * 查询高评分续写
     */
    List<AIContinuation> findByChapterIdAndRatingGreaterThanEqualOrderByRatingDesc(
            Long chapterId, Integer minRating);
}
