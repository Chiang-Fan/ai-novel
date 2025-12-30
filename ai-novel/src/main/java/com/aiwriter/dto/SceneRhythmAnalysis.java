package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 场景节奏分析DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneRhythmAnalysis {
    
    /** 场景ID */
    private Long sceneId;
    
    /** 场景名称 */
    private String sceneName;
    
    /** 场景类型 */
    private String sceneType;
    
    /** 总出现次数 */
    private Long totalAppearances;
    
    /** 平均出现间隔（章数） */
    private Integer averageInterval;
    
    /** 最小出现间隔 */
    private Integer minInterval;
    
    /** 最大出现间隔 */
    private Integer maxInterval;
    
    /** 节奏模式 (STABLE/ACCELERATING/DECELERATING/VARIABLE/SPARSE) */
    private String rhythmPattern;
    
    /** 节奏评分 (0-1.0) */
    private Double rhythmScore;
    
    /** 平均章节字数 */
    private Integer averageChapterLength;
    
    /** 首次出现章节号 */
    private Integer firstAppearanceChapter;
    
    /** 最后出现章节号 */
    private Integer lastAppearanceChapter;
    
    /** 最后使用时间 */
    private LocalDateTime lastUsageTime;
    
    /** 距离最后使用多少天 */
    private Integer daysSinceLastUsage;
    
    /** 出现间隔分布 */
    private Map<String, Integer> intervalDistribution;
    
    /** 详细说明 */
    private String description;
}
