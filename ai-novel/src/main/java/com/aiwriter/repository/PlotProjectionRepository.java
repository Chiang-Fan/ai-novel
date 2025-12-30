package com.aiwriter.repository;

import com.aiwriter.entity.PlotProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 情节推演Repository
 */
@Repository
public interface PlotProjectionRepository extends JpaRepository<PlotProjection, Long> {
    
    /**
     * 查找小说的推演记录
     */
    List<PlotProjection> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    
    /**
     * 按类型查找
     */
    List<PlotProjection> findByNovelIdAndProjectionTypeOrderByCreatedAtDesc(Long novelId, String projectionType);
    
    /**
     * 查找已采纳的推演
     */
    List<PlotProjection> findByNovelIdAndIsAdoptedTrueOrderByAdoptedAtDesc(Long novelId);
}
