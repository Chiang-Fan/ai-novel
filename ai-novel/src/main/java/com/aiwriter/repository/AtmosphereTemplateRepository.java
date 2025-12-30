package com.aiwriter.repository;

import com.aiwriter.entity.AtmosphereTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 氛围模板Repository
 */
@Repository
public interface AtmosphereTemplateRepository extends JpaRepository<AtmosphereTemplate, Long> {
    
    /**
     * 按分类查询
     */
    List<AtmosphereTemplate> findByCategoryOrderByUsageCountDesc(String category);
    
    /**
     * 按氛围类型查询
     */
    List<AtmosphereTemplate> findByAtmosphereTypeOrderByUsageCountDesc(String atmosphereType);
    
    /**
     * 查询系统模板
     */
    List<AtmosphereTemplate> findByIsSystemTrueOrderByUsageCountDesc();
    
    /**
     * 查询用户创建的模板
     */
    List<AtmosphereTemplate> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    /**
     * 按使用次数排序
     */
    List<AtmosphereTemplate> findTop10ByOrderByUsageCountDesc();
}
