package com.aiwriter.repository;

import com.aiwriter.entity.PlotThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 伏笔Repository
 */
@Repository
public interface PlotThreadRepository extends JpaRepository<PlotThread, Long> {
    
    /**
     * 查询小说的所有伏笔
     */
    List<PlotThread> findByNovelId(Long novelId);
    
    /**
     * 根据状态查询伏笔
     */
    List<PlotThread> findByNovelIdAndStatus(Long novelId, String status);
    
    /**
     * 查询待展开的伏笔
     */
    List<PlotThread> findByNovelIdAndStatusIn(Long novelId, List<String> statuses);
    
    /**
     * 根据重要程度查询
     */
    List<PlotThread> findByNovelIdAndImportance(Long novelId, String importance);
    
    /**
     * 查询在某章节埋下的伏笔
     */
    List<PlotThread> findByPlantedInChapterId(Long chapterId);
    
    /**
     * 统计伏笔数
     */
    long countByNovelIdAndStatus(Long novelId, String status);
}
