package com.aiwriter.repository;

import com.aiwriter.entity.StyleConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 风格转换记录Repository
 */
@Repository
public interface StyleConversionRepository extends JpaRepository<StyleConversion, Long> {
    
    /**
     * 查找章节的转换记录
     */
    List<StyleConversion> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
    
    /**
     * 查找指定目标风格的转换记录
     */
    List<StyleConversion> findByTargetStyleIdOrderByCreatedAtDesc(Long targetStyleId);
    
    /**
     * 查找已应用的转换记录
     */
    List<StyleConversion> findByChapterIdAndIsAppliedTrueOrderByAppliedAtDesc(Long chapterId);
    
    /**
     * 统计章节的转换次数
     */
    long countByChapterId(Long chapterId);
}
