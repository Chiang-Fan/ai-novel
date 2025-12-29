package com.aiwriter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容分析响应DTO
 */
public class ContentAnalysisResponse {
    
    private Long id;
    private Long novelId;
    private Long chapterId;
    private String contentSnippet;
    private String protagonistName;
    private String viewpointCharacter;  // 章节视角角色（临时主角）
    private Boolean isGlobalProtagonistPov;  // 是否使用整体主角视角
    private String narrativePerspective;  // 叙述视角
    private List<ExtractedCharacterDTO> extractedCharacters;
    private SceneInfoDTO sceneInfo;
    private WritingStyleDTO styleInfo;
    private String currentConflict;
    private String emotionalTone;
    private Integer completenessScore;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 嵌套DTO
    
    public static class ExtractedCharacterDTO {
        private String name;
        private String role;
        private String traits;
        private String description;
        private Boolean isGlobalProtagonist;  // 是否为整体主角
        private Integer importanceLevel;  // 重要性级别（1-10）
        
        public ExtractedCharacterDTO() {}
        
        public ExtractedCharacterDTO(String name, String role, String traits) {
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
        
        public Boolean getIsGlobalProtagonist() { return isGlobalProtagonist; }
        public void setIsGlobalProtagonist(Boolean isGlobalProtagonist) { this.isGlobalProtagonist = isGlobalProtagonist; }
        
        public Integer getImportanceLevel() { return importanceLevel; }
        public void setImportanceLevel(Integer importanceLevel) { this.importanceLevel = importanceLevel; }
    }
    
    public static class SceneInfoDTO {
        private String description;
        private String location;
        private String time;
        private String atmosphere;
        
        public SceneInfoDTO() {}
        
        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        
        public String getAtmosphere() { return atmosphere; }
        public void setAtmosphere(String atmosphere) { this.atmosphere = atmosphere; }
    }
    
    public static class WritingStyleDTO {
        private String style;
        private String description;
        private String plotPace;
        
        public WritingStyleDTO() {}
        
        // Getters and Setters
        public String getStyle() { return style; }
        public void setStyle(String style) { this.style = style; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPlotPace() { return plotPace; }
        public void setPlotPace(String plotPace) { this.plotPace = plotPace; }
    }
    
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
    
    public List<ExtractedCharacterDTO> getExtractedCharacters() {
        return extractedCharacters;
    }
    
    public void setExtractedCharacters(List<ExtractedCharacterDTO> extractedCharacters) {
        this.extractedCharacters = extractedCharacters;
    }
    
    public SceneInfoDTO getSceneInfo() {
        return sceneInfo;
    }
    
    public void setSceneInfo(SceneInfoDTO sceneInfo) {
        this.sceneInfo = sceneInfo;
    }
    
    public WritingStyleDTO getStyleInfo() {
        return styleInfo;
    }
    
    public void setStyleInfo(WritingStyleDTO styleInfo) {
        this.styleInfo = styleInfo;
    }
    
    public String getCurrentConflict() {
        return currentConflict;
    }
    
    public void setCurrentConflict(String currentConflict) {
        this.currentConflict = currentConflict;
    }
    
    public String getEmotionalTone() {
        return emotionalTone;
    }
    
    public void setEmotionalTone(String emotionalTone) {
        this.emotionalTone = emotionalTone;
    }
    
    public Integer getCompletenessScore() {
        return completenessScore;
    }
    
    public void setCompletenessScore(Integer completenessScore) {
        this.completenessScore = completenessScore;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
