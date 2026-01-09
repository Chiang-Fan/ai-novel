package com.aiwriter.repository;

import com.aiwriter.entity.CharacterLorebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterLorebookRepository extends JpaRepository<CharacterLorebook, Long> {
    List<CharacterLorebook> findByNovelIdAndCharacterId(Long novelId, Long characterId);
    List<CharacterLorebook> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    List<CharacterLorebook> findByCharacterId(Long characterId);
}