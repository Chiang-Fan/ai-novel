package com.aiwriter.repository;

import com.aiwriter.entity.RelationshipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 关系历史Repository
 */
@Repository
public interface RelationshipHistoryRepository extends JpaRepository<RelationshipHistory, Long> {
    
    /**
     * 根据关系ID查询历史记录
     */
    List<RelationshipHistory> findByRelationshipIdOrderByChangeTimeDesc(Long relationshipId);
    
    /**
     * 根据变化类型查询
     */
    List<RelationshipHistory> findByChangeType(String changeType);
    
    /**
     * 根据章节ID查询关联的关系变化
     */
    List<RelationshipHistory> findByRelatedChapterId(Long chapterId);
}
