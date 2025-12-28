package com.ai.novel.repository;

import com.ai.novel.entity.Scene;
import com.ai.novel.entity.enums.SceneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 场景Repository
 */
@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {

    /**
     * 查询小说的所有场景,按场景号排序
     */
    List<Scene> findByNovelIdOrderBySceneNumberAsc(Long novelId);
    
    /**
     * 查询小说的所有场景
     */
    List<Scene> findByNovelId(Long novelId);

    /**
     * 根据状态查询场景
     */
    List<Scene> findByNovelIdAndStatus(Long novelId, SceneStatus status);

    /**
     * 统计小说的场景数
     */
    long countByNovelId(Long novelId);
}
