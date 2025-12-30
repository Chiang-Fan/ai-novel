package com.aiwriter.repository;

import com.aiwriter.entity.CharacterMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterMilestoneRepository extends JpaRepository<CharacterMilestone, Long> {
    
    /**
     * 查询角色的所有里程碑
     */
    List<CharacterMilestone> findByCharacterIdOrderByCreatedAtDesc(Long characterId);
    
    /**
     * 查询指定章节的里程碑
     */
    List<CharacterMilestone> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
    
    /**
     * 查询指定类型的里程碑
     */
    List<CharacterMilestone> findByCharacterIdAndMilestoneTypeOrderByCreatedAtDesc(
        Long characterId, String milestoneType);
    
    /**
     * 统计角色的里程碑数量
     */
    long countByCharacterId(Long characterId);
}
