package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能章节创建响应
 * 包含章节信息和提取的元数据
 */
@Data
public class SmartChapterCreateResponse {
    
    // ========== 章节基础信息 ==========
    private Long id;
    private Long novelId;
    private Integer chapterNumber;
    private String title;
    private String content;
    private Integer wordCount;
    private String summary;
    private String continuationDirection;
    private LocalDateTime createdAt;
    
    // ========== 提取的元数据 ==========
    
    /**
     * 提取的章节标题（如用户未提供）
     */
    private String extractedTitle;
    
    /**
     * 提取的关键词列表
     */
    private List<String> keywords;
    
    /**
     * 提取的角色列表
     */
    private List<CharacterInfo> characters;
    
    /**
     * 提取的场景信息
     */
    private SceneInfo scene;
    
    /**
     * 提取的情节信息
     */
    private PlotInfo plot;
    
    /**
     * 写作风格分析
     */
    private StyleInfo style;
    
    /**
     * 内容完整性评分（0-100）
     */
    private Integer completenessScore;
    
    /**
     * 内容分析ID
     */
    private Long analysisId;
    
    // ========== 内嵌类：角色信息 ==========
    @Data
    public static class CharacterInfo {
        private String name;
        private String role;  // 主角/配角/反派等
        private String description;
    }
    
    // ========== 内嵌类：场景信息 ==========
    @Data
    public static class SceneInfo {
        private String description;
        private String location;
        private String time;
        private String atmosphere;
    }
    
    // ========== 内嵌类：情节信息 ==========
    @Data
    public static class PlotInfo {
        private String conflict;      // 当前冲突
        private String emotionalTone; // 情感基调
        private List<String> foreshadowing; // 伏笔
    }
    
    // ========== 内嵌类：风格信息 ==========
    @Data
    public static class StyleInfo {
        private String type;        // 风格类型
        private String description; // 风格描述
        private String pacing;      // 节奏
    }
}
