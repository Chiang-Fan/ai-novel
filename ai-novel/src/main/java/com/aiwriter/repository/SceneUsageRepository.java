package com.aiwriter.repository;

import com.aiwriter.entity.SceneUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneUsageRepository extends JpaRepository<SceneUsage, Long> {
    
    /**
     * 查询场景的所有使用记录
     */
    List<SceneUsage> findBySceneIdOrderByUsageTimeDesc(Long sceneId);
    
    /**
     * 查询章节使用的场景
     */
    List<SceneUsage> findByChapterIdOrderByUsageTimeAsc(Long chapterId);
    
    /**
     * 统计场景使用次数
     */
    long countBySceneId(Long sceneId);
    
    /**
     * 查询场景首次使用
     */
    SceneUsage findFirstBySceneIdOrderByUsageTimeAsc(Long sceneId);
    
    /**
     * 查询场景最后使用
     */
    SceneUsage findFirstBySceneIdOrderByUsageTimeDesc(Long sceneId);
    
    /**
     * 按时段统计
     */
    @Query("SELECT su.timeOfDay, COUNT(su) FROM SceneUsage su WHERE su.sceneId = :sceneId GROUP BY su.timeOfDay")
    List<Object[]> countByTimeOfDay(Long sceneId);
}
