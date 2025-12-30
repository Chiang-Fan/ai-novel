package com.aiwriter.repository;

import com.aiwriter.entity.CharacterRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色关系Repository
 */
@Repository
public interface CharacterRelationshipRepository extends JpaRepository<CharacterRelationship, Long> {
    
    /**
     * 根据角色ID查询所有关系
     */
    List<CharacterRelationship> findByCharacterId(Long characterId);
    
    /**
     * 根据相关角色ID查询所有关系
     */
    List<CharacterRelationship> findByRelatedCharacterId(Long relatedCharacterId);
    
    /**
     * 查询两个角色之间的关系
     */
    Optional<CharacterRelationship> findByCharacterIdAndRelatedCharacterId(Long characterId, Long relatedCharacterId);
    
    /**
     * 根据小说ID查询所有角色关系（通过角色表关联）
     */
    @Query("SELECT r FROM CharacterRelationship r " +
           "WHERE r.characterId IN (SELECT c.id FROM Character c WHERE c.novelId = :novelId)")
    List<CharacterRelationship> findByNovelId(@Param("novelId") Long novelId);
    
    /**
     * 根据关系类型查询
     */
    List<CharacterRelationship> findByRelationshipType(String relationshipType);
    
    /**
     * 查询角色的所有关系（包括正向和反向）
     */
    @Query("SELECT r FROM CharacterRelationship r " +
           "WHERE r.characterId = :characterId OR r.relatedCharacterId = :characterId")
    List<CharacterRelationship> findAllRelationshipsByCharacterId(@Param("characterId") Long characterId);
}
