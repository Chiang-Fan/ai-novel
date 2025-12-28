package com.ai.novel.repository;

import com.ai.novel.entity.OutlineNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<OutlineNode> findByNovelId(Long novelId);

    /**
     * 查询根节点(没有父节点的节点)
     */
    List<OutlineNode> findByNovelIdAndParentIsNull(Long novelId);

    /**
     * 查询子节点
     */
    List<OutlineNode> findByParentIdOrderByOrderIndexAsc(Long parentId);

    /**
     * 查询树形结构
     */
    @Query("SELECT n FROM OutlineNode n WHERE n.novel.id = :novelId ORDER BY n.level, n.orderIndex")
    List<OutlineNode> findTreeByNovelId(@Param("novelId") Long novelId);
}
