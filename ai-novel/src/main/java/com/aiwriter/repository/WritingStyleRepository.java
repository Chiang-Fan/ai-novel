package com.aiwriter.repository;

import com.aiwriter.entity.WritingStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 写作风格Repository
 */
@Repository
public interface WritingStyleRepository extends JpaRepository<WritingStyle, Long> {
    
    /**
     * 查找指定分类的风格
     */
    List<WritingStyle> findByCategoryOrderByUsageCountDesc(String category);
    
    /**
     * 查找系统风格
     */
    List<WritingStyle> findByIsSystemTrueOrderByUsageCountDesc();
    
    /**
     * 按使用次数排序
     */
    List<WritingStyle> findAllByOrderByUsageCountDesc();
    
    /**
     * 按评分排序
     */
    List<WritingStyle> findAllByOrderByRatingDesc();
    
    /**
     * 根据小说ID查找文风
     */
    WritingStyle findByNovelId(Long novelId);
    
    /**
     * 检查是否存在指定小说的文风记录
     */
    boolean existsByNovelId(Long novelId);
}
