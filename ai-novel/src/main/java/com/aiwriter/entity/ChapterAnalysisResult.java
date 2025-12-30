package com.aiwriter.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 章节深度分析结果
 * 
 * 比 ContentAnalysis 更深入的章节分析，包括：
 * - 情节点（Plot Points）识别
 * - 主题和母题分析
 * - 冲突层级检测
 * - 角色弧光追踪
 * - 节奏分析
 */
@Data
@Entity
@Table(name = "chapter_analysis_results")
public class ChapterAnalysisResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;
    
    // ========== 情节点分析 ==========
    
    /**
     * 情节点列表（JSON数组）
     * [{type: "INCITING_INCIDENT", description: "...", timestamp: "..."}]
     */
    @Column(name = "plot_points", columnDefinition = "TEXT")
    private String plotPoints;
    
    /**
     * 主要冲突类型（INTERNAL/EXTERNAL/MIXED）
     */
    @Column(name = "conflict_type", length = 20)
    private String conflictType;
    
    /**
     * 冲突强度（1-10）
     */
    @Column(name = "conflict_intensity")
    private Integer conflictIntensity;
    
    /**
     * 冲突升级方向（ESCALATING/DE_ESCALATING/STABLE）
     */
    @Column(name = "conflict_direction", length = 20)
    private String conflictDirection;
    
    // ========== 主题和母题 ==========
    
    /**
     * 主题列表（JSON数组）
     * ["成长", "复仇", "爱情"]
     */
    @Column(name = "themes", columnDefinition = "TEXT")
    private String themes;
    
    /**
     * 主题强度（1-10）
     */
    @Column(name = "theme_intensity")
    private Integer themeIntensity;
    
    /**
     * 主题描述
     */
    @Column(name = "theme_description", columnDefinition = "TEXT")
    private String themeDescription;
    
    // ========== 角色弧光 ==========
    
    /**
     * 角色发展列表（JSON数组）
     * [{name: "张三", arc_type: "POSITIVE", development: "..."}]
     */
    @Column(name = "character_arcs", columnDefinition = "TEXT")
    private String characterArcs;
    
    /**
     * 主角成长阶段（SETUP/RISING/MIDPOINT/CLIMAX/RESOLUTION）
     */
    @Column(name = "protagonist_stage", length = 30)
    private String protagonistStage;
    
    // ========== 节奏分析 ==========
    
    /**
     * 章节节奏（SLOW/MEDIUM/FAST/VARIABLE）
     */
    @Column(name = "pacing", length = 20)
    private String pacing;
    
    /**
     * 动作占比（0-1）
     */
    @Column(name = "action_ratio")
    private Double actionRatio;
    
    /**
     * 对话占比（0-1）
     */
    @Column(name = "dialogue_ratio")
    private Double dialogueRatio;
    
    /**
     * 描写占比（0-1）
     */
    @Column(name = "description_ratio")
    private Double descriptionRatio;
    
    /**
     * 内心独白占比（0-1）
     */
    @Column(name = "introspection_ratio")
    private Double introspectionRatio;
    
    // ========== 情感曲线 ==========
    
    /**
     * 情感曲线数据（JSON数组）
     * [{position: 0.1, emotion: "平静", intensity: 3}, ...]
     */
    @Column(name = "emotion_curve", columnDefinition = "TEXT")
    private String emotionCurve;
    
    /**
     * 整体情感趋势（POSITIVE/NEGATIVE/NEUTRAL/FLUCTUATING）
     */
    @Column(name = "emotion_trend", length = 20)
    private String emotionTrend;
    
    // ========== 叙事技巧 ==========
    
    /**
     * 使用的叙事技巧（JSON数组）
     * ["倒叙", "插叙", "伏笔", "悬念"]
     */
    @Column(name = "narrative_techniques", columnDefinition = "TEXT")
    private String narrativeTechniques;
    
    /**
     * 转折点数量
     */
    @Column(name = "turning_points_count")
    private Integer turningPointsCount;
    
    /**
     * 悬念强度（1-10）
     */
    @Column(name = "suspense_level")
    private Integer suspenseLevel;
    
    // ========== 世界观构建 ==========
    
    /**
     * 世界观元素（JSON数组）
     * ["魔法体系", "社会结构", "历史背景"]
     */
    @Column(name = "world_building_elements", columnDefinition = "TEXT")
    private String worldBuildingElements;
    
    /**
     * 世界观完整度（1-10）
     */
    @Column(name = "world_building_score")
    private Integer worldBuildingScore;
    
    // ========== 质量评估 ==========
    
    /**
     * 整体质量评分（1-10）
     */
    @Column(name = "quality_score")
    private Integer qualityScore;
    
    /**
     * 可读性评分（1-10）
     */
    @Column(name = "readability_score")
    private Integer readabilityScore;
    
    /**
     * 创意性评分（1-10）
     */
    @Column(name = "creativity_score")
    private Integer creativityScore;
    
    /**
     * 逻辑连贯性评分（1-10）
     */
    @Column(name = "coherence_score")
    private Integer coherenceScore;
    
    // ========== 改进建议 ==========
    
    /**
     * 改进建议列表（JSON数组）
     * [{category: "情节", suggestion: "...", priority: 8}]
     */
    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;
    
    // ========== 元数据 ==========
    
    @Column(name = "analysis_duration_ms")
    private Long analysisDurationMs;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // ========== 辅助方法 ==========
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取情节点列表
     */
    public List<PlotPoint> getPlotPointsList() {
        if (plotPoints == null || plotPoints.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(plotPoints, new TypeReference<List<PlotPoint>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取主题列表
     */
    public List<String> getThemesList() {
        if (themes == null || themes.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(themes, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取角色弧光列表
     */
    public List<CharacterArc> getCharacterArcsList() {
        if (characterArcs == null || characterArcs.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(characterArcs, new TypeReference<List<CharacterArc>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    // ========== 内部类 ==========
    
    @Data
    public static class PlotPoint {
        private String type; // INCITING_INCIDENT, RISING_ACTION, CLIMAX, etc.
        private String description;
        private Double position; // 0-1, 表示在章节中的相对位置
        private Integer importance; // 1-10
    }
    
    @Data
    public static class CharacterArc {
        private String name;
        private String arcType; // POSITIVE, NEGATIVE, FLAT, COMPLEX
        private String development;
        private Integer growthScore; // 1-10
    }
    
    @Data
    public static class ImprovementSuggestion {
        private String category; // 情节/角色/节奏/风格
        private String suggestion;
        private Integer priority; // 1-10
    }
}
