package com.aiwriter.repository;

import com.aiwriter.entity.SimulationBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationBranchRepository extends JpaRepository<SimulationBranch, Long> {
    
    // 查找模拟的所有分支
    List<SimulationBranch> findBySimulationIdOrderByDepthLevelAsc(Long simulationId);
    
    // 查找父分支的子分支
    List<SimulationBranch> findByParentBranchId(Long parentBranchId);
    
    // 查找根分支（无父分支）
    List<SimulationBranch> findBySimulationIdAndParentBranchIdIsNull(Long simulationId);
    
    // 查找结局分支
    List<SimulationBranch> findBySimulationIdAndIsEndingTrue(Long simulationId);
}
