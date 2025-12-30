package com.aiwriter.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 章节深度分析结果 DTO
 */
@Data
public class ChapterAnalysisDto {
    
    private Long id;
    private Long novelId;
    private Long chapterId;
    private Integer chapterNumber;
    
    // 情节点
    private List<PlotPointDto> plotPoints;
    private String conflictType;
    private Integer conflictIntensity;
    private String conflictDirection;
    
    // 主题
    private List<String> themes;
    private Integer themeIntensity;
    private String themeDescription;
    
    // 角色弧光
    private List<CharacterArcDto> characterArcs;
    private String protagonistStage;
    
    // 节奏
    private String pacing;
    private Double actionRatio;
    private Double dialogueRatio;
    private Double descriptionRatio;
    private Double introspectionRatio;
    
    // 情感曲线
    private List<EmotionPointDto> emotionCurve;
    private String emotionTrend;
    
    // 叙事技巧
    private List<String> narrativeTechniques;
    private Integer turningPointsCount;
    private Integer suspenseLevel;
    
    // 世界观
    private List<String> worldBuildingElements;
    private Integer worldBuildingScore;
    
    // 质量评估
    private Integer qualityScore;
    private Integer readabilityScore;
    private Integer creativityScore;
    private Integer coherenceScore;
    
    // 改进建议
    private List<ImprovementSuggestionDto> improvementSuggestions;
    
    // 元数据
    private Long analysisDurationMs;
    private LocalDateTime createdAt;
    
    @Data
    public static class PlotPointDto {
        private String type;
        private String description;
        private Double position;
        private Integer importance;
    }
    
    @Data
    public static class CharacterArcDto {
        private String name;
        private String arcType;
        private String development;
        private Integer growthScore;
    }
    
    @Data
    public static class EmotionPointDto {
        private Double position;
        private String emotion;
        private Integer intensity;
    }
    
    @Data
    public static class ImprovementSuggestionDto {
        private String category;
        private String suggestion;
        private Integer priority;
    }
}
