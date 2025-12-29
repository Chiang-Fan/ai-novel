package com.aiwriter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 续写建议响应DTO
 */
public class ContinuationSuggestionResponse {
    
    private Long id;
    private Long novelId;
    private Long analysisId;
    private String title;
    private String description;
    private String storyDevelopment;
    private String impact;
    private String plotDirection;
    private String involvedCharacters;
    private Integer expectedWordCount;
    private Integer difficultyLevel;
    private Integer priority;
    private Boolean isAdopted;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    public ContinuationSuggestionResponse() {}
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
