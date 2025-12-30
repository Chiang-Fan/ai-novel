package com.aiwriter.repository;

import com.aiwriter.entity.GeneratedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 生成图片Repository
 */
@Repository
public interface GeneratedImageRepository extends JpaRepository<GeneratedImage, Long> {
    
    List<GeneratedImage> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    
    List<GeneratedImage> findByNovelIdAndImageTypeOrderByCreatedAtDesc(Long novelId, String imageType);
    
    List<GeneratedImage> findByChapterId(Long chapterId);
    
    List<GeneratedImage> findByCharacterId(Long characterId);
    
    List<GeneratedImage> findBySceneId(Long sceneId);
    
    List<GeneratedImage> findByNovelIdAndIsAdoptedTrue(Long novelId);
}
