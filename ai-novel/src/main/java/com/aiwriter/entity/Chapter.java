package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

/**
 * 章节实体
 * 对应Python的Chapter模型
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chapters", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_scene_id", columnList = "scene_id")
})
public class Chapter extends BaseEntity {
    
    /**
     * 所属小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    /**
     * 关联场景ID（可选）
     */
    @Column(name = "scene_id")
    private Long sceneId;
    
    /**
     * 章节视角角色ID（POV角色，章节的临时主角）
     * 用于区分整本书的主角和当前章节的视角人物
     */
    @Column(name = "viewpoint_character_id")
    private Long viewpointCharacterId;
    
    /**
     * 关联大纲节点ID（可选）
     */
    @Column(name = "outline_node_id")
    private Long outlineNodeId;
    
    /**
     * 章节标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 章节内容
     */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;
    
    /**
     * 章节序号
     */
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;
    
    /**
     * 字数
     */
    @Column(name = "word_count")
    private Integer wordCount = 0;
    
    /**
     * 章节摘要（AI生成）
     */
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    /**
     * 续写方向
     */
    @Column(name = "continuation_direction", columnDefinition = "TEXT")
    private String continuationDirection;
    
    /**
     * 是否AI生成
     */
    @Column(name = "is_ai_generated")
    private Boolean isAiGenerated = false;
    
    /**
     * 场景类型（Qwen-Project.md 新增）
     * 取值：setup（铺垫）, conflict（冲突）, turning_point（转折点）, reflection（反思）
     */
    @Column(name = "scene_type", length = 20)
    private String sceneType;
    
    /**
     * 是否高重要场景（Qwen-Project.md 新增）
     * 用于节奏控制，高重要场景不宜连续出现过多
     */
    @Column(name = "is_high_stakes")
    private Boolean isHighStakes = false;
    
    /**
     * 章节状态
     */
    @Column(name = "status", length = 20)
    private String status;
    
    // Getters and Setters
    public Long getNovelId() {
        return novelId;
    }
    
    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }
    
    public Long getSceneId() {
        return sceneId;
    }
    
    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }
    
    public Long getViewpointCharacterId() {
        return viewpointCharacterId;
    }
    
    public void setViewpointCharacterId(Long viewpointCharacterId) {
        this.viewpointCharacterId = viewpointCharacterId;
    }
    
    public Long getOutlineNodeId() {
        return outlineNodeId;
    }
    
    public void setOutlineNodeId(Long outlineNodeId) {
        this.outlineNodeId = outlineNodeId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getChapterNumber() {
        return chapterNumber;
    }
    
    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }
    
    public Integer getWordCount() {
        return wordCount;
    }
    
    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getContinuationDirection() {
        return continuationDirection;
    }
    
    public void setContinuationDirection(String continuationDirection) {
        this.continuationDirection = continuationDirection;
    }
    
    public Boolean getIsAiGenerated() {
        return isAiGenerated;
    }
    
    public void setIsAiGenerated(Boolean isAiGenerated) {
        this.isAiGenerated = isAiGenerated;
    }
    
    public String getSceneType() {
        return sceneType;
    }
    
    public void setSceneType(String sceneType) {
        this.sceneType = sceneType;
    }
    
    public Boolean getIsHighStakes() {
        return isHighStakes;
    }
    
    public void setIsHighStakes(Boolean isHighStakes) {
        this.isHighStakes = isHighStakes;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}