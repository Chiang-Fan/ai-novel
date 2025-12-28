package com.ai.novel.repository;

import com.ai.novel.entity.Novel;
import com.ai.novel.entity.enums.NovelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 小说Repository
 */
@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    /**
     * 根据状态查询小说
     */
    List<Novel> findByStatus(NovelStatus status);
    
    /**
     * 根据状态分页查询小说
     */
    Page<Novel> findByStatus(NovelStatus status, Pageable pageable);

    /**
     * 根据标题模糊查询
     */
    List<Novel> findByTitleContaining(String title);

    /**
     * 查询所有小说,按更新时间倒序
     */
    @Query("SELECT n FROM Novel n ORDER BY n.updatedAt DESC")
    List<Novel> findAllOrderByUpdatedAtDesc();
}
