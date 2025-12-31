package com.aiwriter.repository;

import com.aiwriter.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节Repository
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    /**
     * 查询小说的所有章节（按章节号排序）
     */
    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);
    
    /**
     * 根据小说ID和章节号查询章节
     */
    Optional<Chapter> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);
    
    /**
     * 查询小说最近N章
     */
    @Query("SELECT c FROM Chapter c WHERE c.novelId = ?1 ORDER BY c.chapterNumber DESC LIMIT ?2")
    List<Chapter> findTopNByNovelId(Long novelId, int limit);
    
    /**
     * 查询场景相关章节
     */
    List<Chapter> findBySceneIdOrderByChapterNumberAsc(Long sceneId);
    
    /**
     * 查询大纲节点关联章节
     */
    List<Chapter> findByOutlineNodeIdOrderByChapterNumberAsc(Long outlineNodeId);
    
    /**
     * 查询小说最大章节号
     */
    @Query("SELECT MAX(c.chapterNumber) FROM Chapter c WHERE c.novelId = ?1")
    Optional<Integer> findMaxChapterNumber(Long novelId);
    
    /**
     * 统计小说章节数
     */
    long countByNovelId(Long novelId);
    
    /**
     * 统计小说总字数
     */
    @Query("SELECT SUM(c.wordCount) FROM Chapter c WHERE c.novelId = ?1")
    Optional<Integer> sumWordCountByNovelId(Long novelId);
    
    /**
     * 查询AI生成的章节
     */
    List<Chapter> findByNovelIdAndIsAiGenerated(Long novelId, Boolean isAiGenerated);
    
    /**
     * 查询最新章节（Qwen-Project.md 新增）
     */
    @Query("SELECT c FROM Chapter c WHERE c.novelId = ?1 ORDER BY c.chapterNumber DESC LIMIT 1")
    Optional<Chapter> findTopByNovelIdOrderByChapterNumberDesc(Long novelId);
    
    /**
     * 统计从某章节开始的高重要场景数量（Qwen-Project.md 新增）
     * 用于节奏控制，避免高潮场景连续出现过多
     */
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.novelId = ?1 AND c.chapterNumber >= ?2 AND c.isHighStakes = true")
    int countHighStakesChapters(Long novelId, Integer fromChapter);
}
