package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

/**
 * 小说实体
 * 对应Python的Novel模型
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "novels")
public class Novel extends BaseEntity {
    
    /**
     * 小说标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 小说简介
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 小说类型（玄幻、都市、科幻等）
     */
    @Column(length = 50)
    private String genre;
    
    /**
     * 目标读者群体
     */
    @Column(name = "target_audience", length = 50)
    private String targetAudience;
    
    /**
     * 创作风格描述
     */
    @Column(name = "writing_style", columnDefinition = "TEXT")
    private String writingStyle;
    
    /**
     * 小说状态（planning, writing, completed）
     */
    @Column(length = 20)
    private String status;
    
    /**
     * 总章节数
     */
    @Column(name = "total_chapters")
    private Integer totalChapters = 0;
    
    /**
     * 总字数
     */
    @Column(name = "total_words")
    private Integer totalWords = 0;
    
    /**
     * 初始大纲ID（必填）
     */
    @Column(name = "initial_outline_id")
    private Long initialOutlineId;
    
    /**
     * 初始场景ID（必填）
     */
    @Column(name = "initial_scene_id")
    private Long initialSceneId;
    
    /**
     * 是否使用AI推荐辅助
     */
    @Column(name = "use_ai_recommendation")
    private Boolean useAiRecommendation = true;
    
    /**
     * 核心主题（JSON数组）
     * 示例：["identity", "sacrifice", "redemption"]
     * Qwen-Project.md 新增字段
     */
    @Column(name = "core_themes", columnDefinition = "TEXT")
    private String coreThemes;
    
    /**
     * 循环意象（JSON数组）
     * 示例：["clock", "rain", "mirror"]
     * Qwen-Project.md 新增字段
     */
    @Column(name = "recurring_motifs", columnDefinition = "TEXT")
    private String recurringMotifs;
    
    /**
     * 小说大纲（整体剧情规划）
     */
    @Column(columnDefinition = "TEXT")
    private String outline;
    
    // Getters and Setters
    
    public Long getInitialOutlineId() {
        return initialOutlineId;
    }
    
    public void setInitialOutlineId(Long initialOutlineId) {
        this.initialOutlineId = initialOutlineId;
    }
    
    public Long getInitialSceneId() {
        return initialSceneId;
    }
    
    public void setInitialSceneId(Long initialSceneId) {
        this.initialSceneId = initialSceneId;
    }
    
    public Boolean getUseAiRecommendation() {
        return useAiRecommendation;
    }
    
    public void setUseAiRecommendation(Boolean useAiRecommendation) {
        this.useAiRecommendation = useAiRecommendation;
    }
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public String getTargetAudience() {
        return targetAudience;
    }
    
    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }
    
    public String getWritingStyle() {
        return writingStyle;
    }
    
    public void setWritingStyle(String writingStyle) {
        this.writingStyle = writingStyle;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getTotalChapters() {
        return totalChapters;
    }
    
    public void setTotalChapters(Integer totalChapters) {
        this.totalChapters = totalChapters;
    }
    
    public Integer getTotalWords() {
        return totalWords;
    }
    
    public void setTotalWords(Integer totalWords) {
        this.totalWords = totalWords;
    }
    
    public String getCoreThemes() {
        return coreThemes;
    }
    
    public void setCoreThemes(String coreThemes) {
        this.coreThemes = coreThemes;
    }
    
    public String getRecurringMotifs() {
        return recurringMotifs;
    }
    
    public void setRecurringMotifs(String recurringMotifs) {
        this.recurringMotifs = recurringMotifs;
    }
    
    public String getOutline() {
        return outline;
    }
    
    public void setOutline(String outline) {
        this.outline = outline;
    }
}
