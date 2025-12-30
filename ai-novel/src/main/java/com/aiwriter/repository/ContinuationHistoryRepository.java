package com.aiwriter.repository;

import com.aiwriter.entity.ContinuationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 智能续写历史数据访问层
 */
@Repository
public interface ContinuationHistoryRepository extends JpaRepository<ContinuationHistory, Long> {
    
    /**
     * 获取章节的所有续写历史
     */
    List<ContinuationHistory> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
    
    /**
     * 获取章节的已采纳续写历史
     */
    List<ContinuationHistory> findByChapterIdAndAcceptedOrderByCreatedAtDesc(Long chapterId, Boolean accepted);
    
    /**
     * 获取批次内的所有续写方案
     */
    List<ContinuationHistory> findByBatchIdOrderByVariantNumberAsc(String batchId);
    
    /**
     * 统计章节的续写次数
     */
    long countByChapterId(Long chapterId);
    
    /**
     * 统计章节的采纳次数
     */
    long countByChapterIdAndAccepted(Long chapterId, Boolean accepted);
    
    /**
     * 获取章节的平均质量评分
     */
    @Query("SELECT AVG(c.qualityScore) FROM ContinuationHistory c WHERE c.chapterId = :chapterId AND c.qualityScore IS NOT NULL")
    Double getAverageQualityScore(Long chapterId);
    
    /**
     * 获取章节的平均文风一致性
     */
    @Query("SELECT AVG(c.styleConsistency) FROM ContinuationHistory c WHERE c.chapterId = :chapterId AND c.styleConsistency IS NOT NULL")
    Double getAverageStyleConsistency(Long chapterId);
    
    /**
     * 删除章节的所有续写历史
     */
    void deleteByChapterId(Long chapterId);
}
