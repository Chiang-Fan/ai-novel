package com.aiwriter.repository;

import com.aiwriter.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 小说Repository
 */
@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {
    
    /**
     * 根据状态查询小说
     */
    List<Novel> findByStatus(String status);
    
    /**
     * 根据类型查询小说
     */
    List<Novel> findByGenre(String genre);
    
    /**
     * 根据标题模糊查询
     */
    List<Novel> findByTitleContaining(String keyword);
    
    /**
     * 查询最近更新的小说
     */
    List<Novel> findTop10ByOrderByUpdatedAtDesc();
    
    /**
     * 统计小说总数
     */
    @Query("SELECT COUNT(n) FROM Novel n WHERE n.status = ?1")
    long countByStatus(String status);
}
