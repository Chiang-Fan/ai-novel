package com.aiwriter.repository;

import com.aiwriter.entity.WorldSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 世界观设定Repository
 */
@Repository
public interface WorldSettingRepository extends JpaRepository<WorldSetting, Long> {
    
    /**
     * 查询小说的所有设定
     */
    List<WorldSetting> findByNovelId(Long novelId);
    
    /**
     * 根据分类查询设定
     */
    List<WorldSetting> findByNovelIdAndCategory(Long novelId, String category);
    
    /**
     * 根据重要程度查询
     */
    List<WorldSetting> findByNovelIdAndImportance(Long novelId, String importance);
    
    /**
     * 模糊查询设定名称
     */
    List<WorldSetting> findByNovelIdAndNameContaining(Long novelId, String keyword);
    
    /**
     * 统计设定数
     */
    long countByNovelId(Long novelId);
}
