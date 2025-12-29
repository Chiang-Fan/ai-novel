package com.aiwriter.repository;

import com.aiwriter.entity.OutlineNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 大纲节点Repository
 */
@Repository
public interface OutlineNodeRepository extends JpaRepository<OutlineNode, Long> {
    
    /**
     * 查询小说的所有大纲节点
     */
    List<OutlineNode> findByNovelIdOrderBySortOrderAsc(Long novelId);
    
    /**
     * 查询根节点（没有父节点）
     */
    List<OutlineNode> findByNovelIdAndParentIdIsNullOrderBySortOrderAsc(Long novelId);
    
    /**
     * 查询子节点
     */
    List<OutlineNode> findByParentIdOrderBySortOrderAsc(Long parentId);
    
    /**
     * 根据节点类型查询
     */
    List<OutlineNode> findByNovelIdAndNodeType(Long novelId, String nodeType);
    
    /**
     * 查询未完成的节点
     */
    List<OutlineNode> findByNovelIdAndIsCompleted(Long novelId, Boolean isCompleted);
    
    /**
     * 查询某层级的节点
     */
    List<OutlineNode> findByNovelIdAndLevel(Long novelId, Integer level);
    
    /**
     * 统计节点数
     */
    long countByNovelId(Long novelId);
}
