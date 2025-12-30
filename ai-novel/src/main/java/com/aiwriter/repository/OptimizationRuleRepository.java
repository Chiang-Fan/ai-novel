package com.aiwriter.repository;

import com.aiwriter.entity.OptimizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 优化规则Repository
 */
@Repository
public interface OptimizationRuleRepository extends JpaRepository<OptimizationRule, Long> {
    
    /**
     * 查找启用的规则
     */
    List<OptimizationRule> findByIsEnabledTrueOrderByUsageCountDesc();
    
    /**
     * 查找指定类型的规则
     */
    List<OptimizationRule> findByRuleTypeAndIsEnabledTrueOrderByUsageCountDesc(String ruleType);
    
    /**
     * 查找指定严重程度的规则
     */
    List<OptimizationRule> findBySeverityAndIsEnabledTrueOrderByUsageCountDesc(String severity);
}
