package com.aiwriter.repository;

import com.aiwriter.entity.ChapterVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterVersionRepository extends JpaRepository<ChapterVersion, Long> {
    
    // 查找章节的所有版本（未删除）
    Page<ChapterVersion> findByChapterIdAndIsDeletedFalseOrderByVersionNumberDesc(
        Long chapterId, Pageable pageable);
    
    // 查找章节的所有版本（包括已删除）
    List<ChapterVersion> findByChapterIdOrderByVersionNumberDesc(Long chapterId);
    
    // 查找当前版本
    Optional<ChapterVersion> findByChapterIdAndIsCurrentTrueAndIsDeletedFalse(Long chapterId);
    
    // 查找最大版本号
    @Query("SELECT MAX(v.versionNumber) FROM ChapterVersion v WHERE v.chapterId = ?1")
    Integer findMaxVersionNumberByChapterId(Long chapterId);
    
    // 统计版本数量
    long countByChapterIdAndIsDeletedFalse(Long chapterId);
}
