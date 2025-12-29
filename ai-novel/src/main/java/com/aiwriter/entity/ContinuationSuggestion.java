package com.aiwriter.entity;

import jakarta.persistence.*;

/**
 * 续写方向推荐实体
 * 存储AI推荐的多个续写方向选项
 */
@Entity
@Table(name = "continuation_suggestions", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_analysis_id", columnList = "analysis_id")
})
public class ContinuationSuggestion extends BaseEntity {
    
    /**
     * 所属小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    /**
     * 关联的内容分析ID
     */
    @Column(name = "analysis_id")
    private Long analysisId;
    
    /**
     * 推荐方向标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 方向描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 可能的故事发展
     */
    @Column(name = "story_development", columnDefinition = "TEXT")
    private String storyDevelopment;
    
    /**
     * 对故事的影响
     */
    @Column(columnDefinition = "TEXT")
    private String impact;
    
    /**
     * 情节走向（上升、下降、转折）
     */
    @Column(name = "plot_direction", length = 50)
    private String plotDirection;
    
    /**
     * 涉及角色（逗号分隔）
     */
    @Column(name = "involved_characters", length = 500)
    private String involvedCharacters;
    
    /**
     * 预期字数
     */
    @Column(name = "expected_word_count")
    private Integer expectedWordCount;
    
    /**
     * 难度系数（1-5）
     */
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;
    
    /**
     * 推荐优先级（1-10，越高越优先）
     */
    @Column(nullable = false)
    private Integer priority = 5;
    
    /**
     * 是否已被采用
     */
    @Column(name = "is_adopted")
    private Boolean isAdopted = false;
    
    /**
     * 采用后生成的章节ID
     */
    @Column(name = "generated_chapter_id")
    private Long generatedChapterId;
    
    // Getters and Setters
    
    public Long getNovelId() {
        return novelId;
    }
    
    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }
    
    public Long getAnalysisId() {
        return analysisId;
    }
    
    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
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
    
    public String getStoryDevelopment() {
        return storyDevelopment;
    }
    
    public void setStoryDevelopment(String storyDevelopment) {
        this.storyDevelopment = storyDevelopment;
    }
    
    public String getImpact() {
        return impact;
    }
    
    public void setImpact(String impact) {
        this.impact = impact;
    }
    
    public String getPlotDirection() {
        return plotDirection;
    }
    
    public void setPlotDirection(String plotDirection) {
        this.plotDirection = plotDirection;
    }
    
    public String getInvolvedCharacters() {
        return involvedCharacters;
    }
    
    public void setInvolvedCharacters(String involvedCharacters) {
        this.involvedCharacters = involvedCharacters;
    }
    
    public Integer getExpectedWordCount() {
        return expectedWordCount;
    }
    
    public void setExpectedWordCount(Integer expectedWordCount) {
        this.expectedWordCount = expectedWordCount;
    }
    
    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Boolean getIsAdopted() {
        return isAdopted;
    }
    
    public void setIsAdopted(Boolean isAdopted) {
        this.isAdopted = isAdopted;
    }
    
    public Long getGeneratedChapterId() {
        return generatedChapterId;
    }
    
    public void setGeneratedChapterId(Long generatedChapterId) {
        this.generatedChapterId = generatedChapterId;
    }
}
