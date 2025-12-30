package com.aiwriter.repository;

import com.aiwriter.entity.ImageGenerationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 图片生成历史Repository
 */
@Repository
public interface ImageGenerationHistoryRepository extends JpaRepository<ImageGenerationHistory, Long> {
    
    List<ImageGenerationHistory> findByNovelIdOrderByCreatedAtDesc(Long novelId);
}
