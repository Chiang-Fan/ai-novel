package com.aiwriter.repository;

import com.aiwriter.entity.SceneChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneChangeRepository extends JpaRepository<SceneChange, Long> {
    
    /**
     * 查询场景的所有变化记录
     */
    List<SceneChange> findBySceneIdOrderByCreatedAtDesc(Long sceneId);
    
    /**
     * 按变化类型查询
     */
    List<SceneChange> findBySceneIdAndChangeTypeOrderByCreatedAtDesc(Long sceneId, String changeType);
    
    /**
     * 统计场景变化次数
     */
    long countBySceneId(Long sceneId);
}
