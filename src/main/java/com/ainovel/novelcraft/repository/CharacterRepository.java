package com.ainovel.novelcraft.repository;

import com.ainovel.novelcraft.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByNovelId(Long novelId);
}