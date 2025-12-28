package com.ai.novel.repository;

import com.ai.novel.entity.PlotThread;
import com.ai.novel.entity.enums.PlotThreadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 情节线Repository
 */
@Repository
public interface PlotThreadRepository extends JpaRepository<PlotThread, Long> {

    /**
     * 查询小说的所有情节线
     */
    List<PlotThread> findByNovelId(Long novelId);

    /**
     * 根据状态查询情节线
     */
    List<PlotThread> findByNovelIdAndStatus(Long novelId, PlotThreadStatus status);

    /**
     * 查询待处理的伏笔(PLANTED状态)
     */
    List<PlotThread> findByNovelIdAndStatusIn(Long novelId, List<PlotThreadStatus> statuses);
}
