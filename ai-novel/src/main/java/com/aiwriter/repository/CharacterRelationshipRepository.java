package com.aiwriter.repository;

import com.aiwriter.entity.CharacterRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色关系Repository
 */
@Repository
public interface CharacterRelationshipRepository extends JpaRepository<CharacterRelationship, Long> {
    
    /**
     * 查询角色的所有关系
     */
    @Query("SELECT r FROM CharacterRelationship r WHERE r.character1Id = ?1 OR r.character2Id = ?1")
    List<CharacterRelationship> findByCharacterId(Long characterId);
    
    /**
     * 查询两个角色之间的关系
     */
    @Query("SELECT r FROM CharacterRelationship r WHERE " +
           "(r.character1Id = ?1 AND r.character2Id = ?2) OR " +
           "(r.character1Id = ?2 AND r.character2Id = ?1)")
    Optional<CharacterRelationship> findByCharacterPair(Long characterId1, Long characterId2);
    
    /**
     * 根据关系类型查询
     */
    List<CharacterRelationship> findByRelationshipType(String relationshipType);
    
    /**
     * 根据关系状态查询
     */
    List<CharacterRelationship> findByStatus(String status);
}
