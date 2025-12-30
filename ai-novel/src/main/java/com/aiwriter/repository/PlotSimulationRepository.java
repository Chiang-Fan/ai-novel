package com.aiwriter.repository;

import com.aiwriter.entity.PlotSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlotSimulationRepository extends JpaRepository<PlotSimulation, Long> {
    
    // 查找小说的所有模拟
    List<PlotSimulation> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    
    // 查找指定状态的模拟
    List<PlotSimulation> findByNovelIdAndStatusOrderByCreatedAtDesc(Long novelId, String status);
}
