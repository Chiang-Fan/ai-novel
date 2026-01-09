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
    List<Character> findByNovelIdOrderByUpdatedAtDesc(Long novelId);
    long countByNovelId(Long novelId);
    
    // 查找主要角色（主角和反派）
    @Query("SELECT c FROM Character c WHERE c.novelId = ?1 AND c.roleType IN ('PROTAGONIST', 'ANTAGONIST') ORDER BY c.roleType ASC")
    List<Character> findMainCharacters(Long novelId);
    
    /**
     * 查找最近更新的角色（Qwen-Project.md 新增）
     * 用于获取当前主要活跃的角色
     */
    @Query("SELECT c FROM Character c WHERE c.novelId = ?1 ORDER BY c.lastUpdatedChapter DESC NULLS LAST")
    List<Character> findByNovelIdOrderByLastUpdatedChapterDesc(Long novelId);
    
    /**
     * 获取最后出场的角色（主角候选）
     */
    default Character findLastUpdatedCharacter(Long novelId) {
        List<Character> characters = findByNovelIdOrderByLastUpdatedChapterDesc(novelId);
        return characters.isEmpty() ? null : characters.get(0);
    }
}
