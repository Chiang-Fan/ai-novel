package com.aiwriter.repository;

import com.aiwriter.entity.ImportedChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 导入章节仓储
 */
@Repository
public interface ImportedChapterRepository extends JpaRepository<ImportedChapter, Long> {
    /**
     * 查询导入记录的所有章节
     */
    List<ImportedChapter> findByTextImportIdOrderByChapterNumber(Long textImportId);

    /**
     * 查询小说的所有导入章节
     */
    List<ImportedChapter> findByNovelIdOrderByCreatedAtDesc(Long novelId);

    /**
     * 统计未转换的章节
     */
    long countByTextImportIdAndConvertedToChapterFalse(Long textImportId);
}
