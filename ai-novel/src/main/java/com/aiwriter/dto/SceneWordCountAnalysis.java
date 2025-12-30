package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 场景字数分析DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneWordCountAnalysis {
    
    /** 场景ID */
    private Long sceneId;
    
    /** 场景名称 */
    private String sceneName;
    
    /** 该场景使用的总字数 */
    private Long totalWordCount;
    
    /** 平均每次使用字数 */
    private Long averageWordCount;
    
    /** 场景出现次数（使用次数） */
    private Long usageCount;
    
    /** 最少字数 */
    private Integer minWordCount;
    
    /** 最多字数 */
    private Integer maxWordCount;
    
    /** 字数分布 (如 "0-300": 2, "300-500": 3, ...) */
    private Map<String, Integer> wordCountDistribution;
    
    /** 字数趋势 (INCREASING/DECREASING/STABLE) */
    private String trend;
    
    /** 最后使用时间 */
    private String lastUsageTime;
    
    /** 统计日期 */
    private String statisticsDate;
}
