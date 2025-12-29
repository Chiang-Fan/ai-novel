package com.aiwriter.repository;

import com.aiwriter.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    List<Scene> findByNovelIdAndSceneType(Long novelId, String sceneType);
    long countByNovelId(Long novelId);
}
