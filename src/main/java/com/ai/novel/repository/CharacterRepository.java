package com.ai.novel.repository;

import com.ai.novel.entity.Character;
import com.ai.novel.entity.enums.CharacterImportance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色Repository
 */
@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {

    /**
     * 查询小说的所有角色
     */
    List<Character> findByNovelId(Long novelId);
    
    /**
     * 分页查询小说的角色
     */
    Page<Character> findByNovelId(Long novelId, Pageable pageable);

    /**
     * 根据重要性查询角色
     */
    List<Character> findByNovelIdAndImportanceLevel(Long novelId, CharacterImportance importance);

    /**
     * 根据姓名查询角色
     */
    List<Character> findByNovelIdAndName(Long novelId, String name);

    /**
     * 查询存活的角色
     */
    List<Character> findByNovelIdAndAlive(Long novelId, Integer alive);

    /**
     * 查询主要角色(重要性为MAIN或SECONDARY)
     */
    List<Character> findByNovelIdAndImportanceLevelIn(Long novelId, List<CharacterImportance> importanceLevels);
}
