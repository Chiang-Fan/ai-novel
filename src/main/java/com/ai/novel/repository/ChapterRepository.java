package com.ai.novel.repository;

import com.ai.novel.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节Repository
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    /**
     * 查询小说的所有章节,按章节号排序
     */
    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);

    /**
     * 分页查询小说的章节
     */
    Page<Chapter> findByNovelId(Long novelId, Pageable pageable);

    /**
     * 查询小说的最近N个章节
     */
    @Query("SELECT c FROM Chapter c WHERE c.novel.id = :novelId ORDER BY c.chapterNumber DESC")
    List<Chapter> findRecentChapters(@Param("novelId") Long novelId);

    /**
     * 查询小说的最大章节号
     */
    @Query("SELECT MAX(c.chapterNumber) FROM Chapter c WHERE c.novel.id = :novelId")
    Optional<Integer> findMaxChapterNumber(@Param("novelId") Long novelId);

    /**
     * 查询场景的所有章节
     */
    List<Chapter> findBySceneIdOrderByChapterInSceneAsc(Long sceneId);

    /**
     * 统计小说的章节数
     */
    long countByNovelId(Long novelId);

    /**
     * 查询小说的总字数
     */
    @Query("SELECT SUM(c.wordCount) FROM Chapter c WHERE c.novel.id = :novelId")
    Optional<Long> sumWordCountByNovelId(@Param("novelId") Long novelId);
}
