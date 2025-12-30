package com.aiwriter.repository;

import com.aiwriter.entity.SceneAtmosphere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 场景氛围Repository
 */
@Repository
public interface SceneAtmosphereRepository extends JpaRepository<SceneAtmosphere, Long> {
    
    /**
     * 查询场景的所有氛围
     */
    List<SceneAtmosphere> findBySceneIdOrderByCreatedAtDesc(Long sceneId);
    
    /**
     * 查询场景的特定氛围类型
     */
    List<SceneAtmosphere> findBySceneIdAndAtmosphereTypeOrderByVersionDesc(Long sceneId, String atmosphereType);
    
    /**
     * 查询场景已应用的氛围
     */
    Optional<SceneAtmosphere> findBySceneIdAndIsAppliedTrue(Long sceneId);
    
    /**
     * 查询使用特定模板的氛围
     */
    List<SceneAtmosphere> findByTemplateId(Long templateId);
    
    /**
     * 统计场景氛围数量
     */
    long countBySceneId(Long sceneId);
    
    /**
     * 查询场景最新版本的氛围
     */
    @Query("SELECT sa FROM SceneAtmosphere sa WHERE sa.sceneId = :sceneId " +
           "AND sa.version = (SELECT MAX(sa2.version) FROM SceneAtmosphere sa2 WHERE sa2.sceneId = :sceneId AND sa2.atmosphereType = sa.atmosphereType)")
    List<SceneAtmosphere> findLatestVersionsBySceneId(Long sceneId);
}
