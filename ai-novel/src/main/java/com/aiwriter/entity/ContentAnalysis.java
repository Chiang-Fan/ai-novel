package com.aiwriter.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 内容分析结果实体
 * 存储AI对小说片段的智能分析结果
 */
@Entity
@Table(name = "content_analysis", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_chapter_id", columnList = "chapter_id")
})
public class ContentAnalysis extends BaseEntity {
    
    /**
     * 所属小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    /**
     * 关联章节ID（可选）
     */
    @Column(name = "chapter_id")
    private Long chapterId;
    
    /**
     * 分析的原文内容片段
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contentSnippet;
    
    /**
     * 识别的主角名称
     */
    @Column(name = "protagonist_name", length = 100)
    private String protagonistName;
    
    /**
     * 章节视角角色名称（POV角色，当前章节的临时主角）
     * 用于区分整体主角和章节视角人物
     */
    @Column(name = "viewpoint_character", length = 100)
    private String viewpointCharacter;
    
    /**
     * 是否使用整体主角视角（true表示当前章节使用全书主角视角）
     */
    @Column(name = "is_global_protagonist_pov")
    private Boolean isGlobalProtagonistPov = true;
    
    /**
     * 视角说明（如：第一人称、第三人称全知、第三人称限知等）
     */
    @Column(name = "narrative_perspective", length = 50)
    private String narrativePerspective;
    
    /**
     * 提取的角色信息（JSON数组）
     * 格式: [{"name":"张三","role":"主角","traits":"勇敢、聪明"}]
     */
    @Column(name = "extracted_characters", columnDefinition = "TEXT")
    private String extractedCharacters;
    
    /**
     * 当前场景描述
     */
    @Column(name = "current_scene", columnDefinition = "TEXT")
    private String currentScene;
    
    /**
     * 场景位置
     */
    @Column(name = "scene_location", length = 200)
    private String sceneLocation;
    
    /**
     * 场景时间
     */
    @Column(name = "scene_time", length = 100)
    private String sceneTime;
    
    /**
     * 场景氛围（紧张、轻松、悲伤等）
     */
    @Column(name = "scene_atmosphere", length = 50)
    private String sceneAtmosphere;
    
    /**
     * 检测到的写作风格
     * 如：幽默、严肃、诗意、简洁、华丽等
     */
    @Column(name = "writing_style", length = 100)
    private String writingStyle;
    
    /**
     * 写作风格详细描述
     */
    @Column(name = "style_description", columnDefinition = "TEXT")
    private String styleDescription;
    
    /**
     * 情节节奏（快速、中等、缓慢）
     */
    @Column(name = "plot_pace", length = 50)
    private String plotPace;
    
    /**
     * 当前情节冲突
     */
    @Column(name = "current_conflict", columnDefinition = "TEXT")
    private String currentConflict;
    
    /**
     * 潜在伏笔（JSON数组）
     */
    @Column(name = "potential_foreshadowing", columnDefinition = "TEXT")
    private String potentialForeshadowing;
    
    /**
     * 角色关系分析（JSON数组）
     */
    @Column(name = "character_relationships", columnDefinition = "TEXT")
    private String characterRelationships;
    
    /**
     * 情感基调（正面、负面、中性）
     */
    @Column(name = "emotional_tone", length = 50)
    private String emotionalTone;
    
    /**
     * 分析版本（用于追踪分析算法版本）
     */
    @Column(name = "analysis_version", length = 20)
    private String analysisVersion = "1.0";
    
    /**
     * 分析完整性得分（0-100）
     */
    @Column(name = "completeness_score")
    private Integer completenessScore;
    
    // Getters and Setters
    
    public Long getNovelId() {
        return novelId;
    }
    
    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }
    
    public Long getChapterId() {
        return chapterId;
    }
    
    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }
    
    public String getContentSnippet() {
        return contentSnippet;
    }
    
    public void setContentSnippet(String contentSnippet) {
        this.contentSnippet = contentSnippet;
    }
    
    public String getProtagonistName() {
        return protagonistName;
    }
    
    public void setProtagonistName(String protagonistName) {
        this.protagonistName = protagonistName;
    }
    
    public String getViewpointCharacter() {
        return viewpointCharacter;
    }
    
    public void setViewpointCharacter(String viewpointCharacter) {
        this.viewpointCharacter = viewpointCharacter;
    }
    
    public Boolean getIsGlobalProtagonistPov() {
        return isGlobalProtagonistPov;
    }
    
    public void setIsGlobalProtagonistPov(Boolean isGlobalProtagonistPov) {
        this.isGlobalProtagonistPov = isGlobalProtagonistPov;
    }
    
    public String getNarrativePerspective() {
        return narrativePerspective;
    }
    
    public void setNarrativePerspective(String narrativePerspective) {
        this.narrativePerspective = narrativePerspective;
    }
    
    public String getExtractedCharacters() {
        return extractedCharacters;
    }
    
    public void setExtractedCharacters(String extractedCharacters) {
        this.extractedCharacters = extractedCharacters;
    }
    
    public String getCurrentScene() {
        return currentScene;
    }
    
    public void setCurrentScene(String currentScene) {
        this.currentScene = currentScene;
    }
    
    public String getSceneLocation() {
        return sceneLocation;
    }
    
    public void setSceneLocation(String sceneLocation) {
        this.sceneLocation = sceneLocation;
    }
    
    public String getSceneTime() {
        return sceneTime;
    }
    
    public void setSceneTime(String sceneTime) {
        this.sceneTime = sceneTime;
    }
    
    public String getSceneAtmosphere() {
        return sceneAtmosphere;
    }
    
    public void setSceneAtmosphere(String sceneAtmosphere) {
        this.sceneAtmosphere = sceneAtmosphere;
    }
    
    public String getWritingStyle() {
        return writingStyle;
    }
    
    public void setWritingStyle(String writingStyle) {
        this.writingStyle = writingStyle;
    }
    
    public String getStyleDescription() {
        return styleDescription;
    }
    
    public void setStyleDescription(String styleDescription) {
        this.styleDescription = styleDescription;
    }
    
    public String getPlotPace() {
        return plotPace;
    }
    
    public void setPlotPace(String plotPace) {
        this.plotPace = plotPace;
    }
    
    public String getCurrentConflict() {
        return currentConflict;
    }
    
    public void setCurrentConflict(String currentConflict) {
        this.currentConflict = currentConflict;
    }
    
    public String getPotentialForeshadowing() {
        return potentialForeshadowing;
    }
    
    public void setPotentialForeshadowing(String potentialForeshadowing) {
        this.potentialForeshadowing = potentialForeshadowing;
    }
    
    public String getCharacterRelationships() {
        return characterRelationships;
    }
    
    public void setCharacterRelationships(String characterRelationships) {
        this.characterRelationships = characterRelationships;
    }
    
    public String getEmotionalTone() {
        return emotionalTone;
    }
    
    public void setEmotionalTone(String emotionalTone) {
        this.emotionalTone = emotionalTone;
    }
    
    public String getAnalysisVersion() {
        return analysisVersion;
    }
    
    public void setAnalysisVersion(String analysisVersion) {
        this.analysisVersion = analysisVersion;
    }
    
    public Integer getCompletenessScore() {
        return completenessScore;
    }
    
    public void setCompletenessScore(Integer completenessScore) {
        this.completenessScore = completenessScore;
    }
    
    // JSON辅助方法
    
    public List<ExtractedCharacter> getExtractedCharactersList() {
        if (extractedCharacters == null || extractedCharacters.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(extractedCharacters, 
                new TypeReference<List<ExtractedCharacter>>(){});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
    
    public void setExtractedCharactersList(List<ExtractedCharacter> characters) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.extractedCharacters = mapper.writeValueAsString(characters);
        } catch (JsonProcessingException e) {
            this.extractedCharacters = "[]";
        }
    }
    
    /**
     * 提取的角色信息内部类
     */
    public static class ExtractedCharacter {
        private String name;
        private String role;
        private String traits;
        private String description;
        
        public ExtractedCharacter() {}
        
        public ExtractedCharacter(String name, String role, String traits) {
            this.name = name;
            this.role = role;
            this.traits = traits;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getTraits() { return traits; }
        public void setTraits(String traits) { this.traits = traits; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
