package com.aiwriter.repository;

import com.aiwriter.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByNovelIdOrderByRoleTypeAsc(Long novelId);
    List<Character> findByNovelIdAndRoleType(Long novelId, String roleType);
    long countByNovelId(Long novelId);
    
    // 查找主要角色（主角和反派）
    @Query("SELECT c FROM Character c WHERE c.novelId = ?1 AND c.roleType IN ('PROTAGONIST', 'ANTAGONIST') ORDER BY c.roleType ASC")
    List<Character> findMainCharacters(Long novelId);
}
