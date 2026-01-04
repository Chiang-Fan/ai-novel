package com.ainovel.novelcraft.repository;

import com.ainovel.novelcraft.entity.WritingStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WritingStyleRepository extends JpaRepository<WritingStyle, Long> {
    WritingStyle findByNovelId(Long novelId);
}