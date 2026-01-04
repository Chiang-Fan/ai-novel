package com.ainovel.novelcraft.repository;

import com.ainovel.novelcraft.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByNovelIdOrderByChapterNumber(Long novelId);
    Optional<Chapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);
}