package com.aiwriter.repository;

import com.aiwriter.entity.PlotHook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 伏笔数据访问层
 */
@Repository
public interface PlotHookRepository extends JpaRepository<PlotHook, Long> {
    
    /**
     * 查询小说的所有伏笔
     */
    List<PlotHook> findByNovelIdOrderByPlantedInChapterAsc(Long novelId);
    
    /**
     * 按状态查询伏笔
     */
    List<PlotHook> findByNovelIdAndStatusOrderByPlantedInChapterAsc(Long novelId, PlotHook.Status status);
    
    /**
     * 查询待触发的伏笔（PENDING 和 HINTED）
     */
    @Query("SELECT h FROM PlotHook h WHERE h.novelId = :novelId AND h.status IN ('PENDING', 'HINTED') ORDER BY h.priority DESC, h.plantedInChapter ASC")
    List<PlotHook> findPendingHooks(@Param("novelId") Long novelId);
    
    /**
     * 查询指定章节埋设的伏笔
     */
    List<PlotHook> findByNovelIdAndPlantedInChapter(Long novelId, Integer chapterNumber);
    
    /**
     * 查询指定章节触发的伏笔
     */
    List<PlotHook> findByNovelIdAndTriggeredInChapter(Long novelId, Integer chapterNumber);
    
    /**
     * 查询高优先级且未触发的伏笔
     */
    @Query("SELECT h FROM PlotHook h WHERE h.novelId = :novelId AND h.status IN ('PENDING', 'HINTED') AND h.priority >= :minPriority ORDER BY h.priority DESC")
    List<PlotHook> findHighPriorityPendingHooks(@Param("novelId") Long novelId, @Param("minPriority") Integer minPriority);
    
    /**
     * 统计各状态伏笔数量
     */
    @Query("SELECT h.status, COUNT(h) FROM PlotHook h WHERE h.novelId = :novelId GROUP BY h.status")
    List<Object[]> countByStatus(@Param("novelId") Long novelId);
    
    /**
     * 查询可能超期的伏笔（预期章节已过但未触发）
     */
    @Query("SELECT h FROM PlotHook h WHERE h.novelId = :novelId AND h.status IN ('PENDING', 'HINTED') AND h.expectedChapter IS NOT NULL AND h.expectedChapter < :currentChapter ORDER BY h.expectedChapter ASC")
    List<PlotHook> findOverdueHooks(@Param("novelId") Long novelId, @Param("currentChapter") Integer currentChapter);
}
