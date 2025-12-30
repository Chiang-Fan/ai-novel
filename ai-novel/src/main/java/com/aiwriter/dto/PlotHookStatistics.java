package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 伏笔统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotHookStatistics {
    
    private Integer totalCount;          // 总数
    private Integer pendingCount;        // 待触发
    private Integer hintedCount;         // 铺垫中
    private Integer triggeredCount;      // 已触发
    private Integer resolvedCount;       // 已解决
    private Integer overdueCount;        // 超期未触发
    private Integer highPriorityCount;   // 高优先级（≥8）
    private Map<String, Integer> typeDistribution;  // 类型分布
}
