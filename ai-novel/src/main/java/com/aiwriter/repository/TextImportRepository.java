package com.aiwriter.repository;

import com.aiwriter.entity.TextImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文本导入仓储
 */
@Repository
public interface TextImportRepository extends JpaRepository<TextImport, Long> {
    /**
     * 查询小说的所有导入记录
     */
    List<TextImport> findByNovelIdOrderByCreatedAtDesc(Long novelId);

    /**
     * 查询特定状态的导入记录
     */
    List<TextImport> findByNovelIdAndStatusOrderByCreatedAtDesc(Long novelId, String status);
}
