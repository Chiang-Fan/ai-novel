package com.aiwriter.repository;

import com.aiwriter.entity.ConflictTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 冲突追踪Repository
 */
@Repository
public interface ConflictTrackingRepository extends JpaRepository<ConflictTracking, Long> {
    
    /**
     * 查找小说的所有冲突
     */
    List<ConflictTracking> findByNovelIdOrderByIntensityLevelDescCreatedAtDesc(Long novelId);
    
    /**
     * 按状态查找
     */
    List<ConflictTracking> findByNovelIdAndStatusOrderByIntensityLevelDesc(Long novelId, String status);
    
    /**
     * 按类型查找
     */
    List<ConflictTracking> findByNovelIdAndConflictTypeOrderByCreatedAtDesc(Long novelId, String conflictType);
    
    /**
     * 查找活跃冲突
     */
    List<ConflictTracking> findByNovelIdAndStatusInOrderByIntensityLevelDesc(Long novelId, List<String> statuses);
}
