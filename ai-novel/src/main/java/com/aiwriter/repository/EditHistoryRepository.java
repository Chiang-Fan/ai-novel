package com.aiwriter.repository;

import com.aiwriter.entity.EditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EditHistoryRepository extends JpaRepository<EditHistory, Long> {
    List<EditHistory> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
    List<EditHistory> findByChapterIdAndOperationType(Long chapterId, String operationType);
    long countByChapterId(Long chapterId);
}
