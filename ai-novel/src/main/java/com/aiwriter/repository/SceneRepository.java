package com.aiwriter.repository;

import com.aiwriter.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    
    /**
     * 查询小说的所有场景（按重要性排序）
     */
    List<Scene> findByNovelIdOrderByImportanceScoreDesc(Long novelId);
    
    /**
     * 查询小说的所有场景（按创建时间排序）- 兼容旧代码
     */
    List<Scene> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    
    /**
     * 按类型查询场景
     */
    List<Scene> findByNovelIdAndSceneTypeOrderByNameAsc(Long novelId, String sceneType);
    
    /**
     * 查询重复出现的场景
     */
    List<Scene> findByNovelIdAndIsRecurringTrueOrderByImportanceScoreDesc(Long novelId);
    
    /**
     * 模糊搜索场景名称
     */
    List<Scene> findByNovelIdAndNameContainingOrderByNameAsc(Long novelId, String keyword);
    
    /**
     * 统计小说的场景数量
     */
    long countByNovelId(Long novelId);
}
