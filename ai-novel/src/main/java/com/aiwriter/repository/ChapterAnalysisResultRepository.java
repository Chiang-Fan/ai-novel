package com.aiwriter.repository;

import com.aiwriter.entity.ChapterAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节深度分析结果仓库
 */
@Repository
public interface ChapterAnalysisResultRepository extends JpaRepository<ChapterAnalysisResult, Long> {
    
    /**
     * 按章节ID查询
     */
    Optional<ChapterAnalysisResult> findByChapterId(Long chapterId);
    
    /**
     * 按小说ID查询所有分析结果
     */
    List<ChapterAnalysisResult> findByNovelIdOrderByChapterNumberAsc(Long novelId);
    
    /**
     * 按小说ID查询，按章节号降序
     */
    List<ChapterAnalysisResult> findByNovelIdOrderByChapterNumberDesc(Long novelId);
    
    /**
     * 查询小说的最新分析
     */
    @Query("SELECT c FROM ChapterAnalysisResult c WHERE c.novelId = :novelId ORDER BY c.chapterNumber DESC LIMIT 1")
    Optional<ChapterAnalysisResult> findLatestByNovelId(Long novelId);
    
    /**
     * 查询章节范围内的分析结果
     */
    @Query("SELECT c FROM ChapterAnalysisResult c WHERE c.novelId = :novelId AND c.chapterNumber BETWEEN :startChapter AND :endChapter ORDER BY c.chapterNumber ASC")
    List<ChapterAnalysisResult> findByNovelIdAndChapterRange(Long novelId, Integer startChapter, Integer endChapter);
    
    /**
     * 查询高质量章节（质量评分 >= threshold）
     */
    @Query("SELECT c FROM ChapterAnalysisResult c WHERE c.novelId = :novelId AND c.qualityScore >= :threshold ORDER BY c.qualityScore DESC")
    List<ChapterAnalysisResult> findHighQualityChapters(Long novelId, Integer threshold);
    
    /**
     * 查询需要改进的章节（质量评分 < threshold）
     */
    @Query("SELECT c FROM ChapterAnalysisResult c WHERE c.novelId = :novelId AND c.qualityScore < :threshold ORDER BY c.qualityScore ASC")
    List<ChapterAnalysisResult> findLowQualityChapters(Long novelId, Integer threshold);
    
    /**
     * 统计小说的平均质量评分
     */
    @Query("SELECT AVG(c.qualityScore) FROM ChapterAnalysisResult c WHERE c.novelId = :novelId")
    Double getAverageQualityScore(Long novelId);
}
