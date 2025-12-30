package com.aiwriter.repository;

import com.aiwriter.entity.StyleAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 风格分析Repository
 */
@Repository
public interface StyleAnalysisRepository extends JpaRepository<StyleAnalysis, Long> {
    
    /**
     * 查找章节的分析记录
     */
    List<StyleAnalysis> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
    
    /**
     * 查找章节的最新分析
     */
    Optional<StyleAnalysis> findFirstByChapterIdOrderByCreatedAtDesc(Long chapterId);
}
