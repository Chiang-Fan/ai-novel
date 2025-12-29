package com.aiwriter.repository;

import com.aiwriter.entity.ContentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 内容分析Repository
 */
@Repository
public interface ContentAnalysisRepository extends JpaRepository<ContentAnalysis, Long> {
    
    /**
     * 根据小说ID查找所有分析记录
     */
    List<ContentAnalysis> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    
    /**
     * 根据章节ID查找分析记录
     */
    Optional<ContentAnalysis> findByChapterId(Long chapterId);
    
    /**
     * 查找小说的最新分析记录
     */
    @Query("SELECT ca FROM ContentAnalysis ca WHERE ca.novelId = ?1 ORDER BY ca.createdAt DESC LIMIT 1")
    Optional<ContentAnalysis> findLatestByNovelId(Long novelId);
    
    /**
     * 查找小说的最近N条分析记录
     */
    @Query("SELECT ca FROM ContentAnalysis ca WHERE ca.novelId = ?1 ORDER BY ca.createdAt DESC LIMIT ?2")
    List<ContentAnalysis> findRecentByNovelId(Long novelId, int limit);
    
    /**
     * 统计小说的分析记录数
     */
    long countByNovelId(Long novelId);
    
    /**
     * 删除章节的分析记录
     */
    void deleteByChapterId(Long chapterId);
}
