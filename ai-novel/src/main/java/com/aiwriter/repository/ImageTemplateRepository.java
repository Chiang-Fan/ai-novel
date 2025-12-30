package com.aiwriter.repository;

import com.aiwriter.entity.ImageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 图片模板Repository
 */
@Repository
public interface ImageTemplateRepository extends JpaRepository<ImageTemplate, Long> {
    
    List<ImageTemplate> findByCategoryOrderByUsageCountDesc(String category);
    
    List<ImageTemplate> findByIsSystemTrueOrderByUsageCountDesc();
}
