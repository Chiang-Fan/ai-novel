package com.aiwriter.repository;

import com.aiwriter.entity.ContentOptimization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 内容优化记录Repository
 */
@Repository
public interface ContentOptimizationRepository extends JpaRepository<ContentOptimization, Long> {
    
    /**
     * 查找章节的所有优化记录
     */
    List<ContentOptimization> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
    
    /**
     * 查找指定类型的优化记录
     */
    List<ContentOptimization> findByChapterIdAndOptimizationTypeOrderByCreatedAtDesc(
            Long chapterId, String optimizationType);
    
    /**
     * 查找已应用的优化记录
     */
    List<ContentOptimization> findByChapterIdAndIsAppliedTrueOrderByAppliedAtDesc(Long chapterId);
    
    /**
     * 统计章节的优化次数
     */
    long countByChapterId(Long chapterId);
}
