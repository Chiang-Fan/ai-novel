package com.aiwriter.repository;

import com.aiwriter.entity.NovelCreationRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 小说创建推荐 Repository
 */
@Repository
public interface NovelCreationRecommendationRepository 
    extends JpaRepository<NovelCreationRecommendation, Long> {
    
    /**
     * 根据小说ID查询推荐
     */
    Optional<NovelCreationRecommendation> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID删除推荐
     */
    void deleteByNovelId(Long novelId);
}
