package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小说创建时的AI推荐响应
 * 包含大纲推荐和场景推荐
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelCreationRecommendationResponse {
    
    /**
     * 推荐的故事框架
     */
    private String storyFramework;
    
    /**
     * 推荐的三幕结构
     */
    private ThreeActStructure threeActStructure;
    
    /**
     * 推荐的主要情节点
     */
    private List<String> mainPlotPoints;
    
    /**
     * 推荐的初始场景设置
     */
    private InitialSceneRecommendation initialScene;
    
    /**
     * 推荐的角色设定
     */
    private List<CharacterRecommendation> characters;
    
    /**
     * 推荐的主题和中心思想
     */
    private List<String> themes;
    
    /**
     * 推荐的写作风格要素
     */
    private List<String> styleElements;
    
    /**
     * 预估的总字数范围
     */
    private WordCountRange wordCountRange;
    
    /**
     * 推荐的预期阅读时长（分钟）
     */
    private Integer estimatedReadingTime;
    
    /**
     * 三幕结构
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreeActStructure {
        private String setup;
        private String confrontation;
        private String resolution;
    }
    
    /**
     * 初始场景推荐
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialSceneRecommendation {
        private String setting;
        private String atmosphere;
        private List<String> keyElements;
        private String suggestedOpeningHook;
        private Integer suggestedWordCount;
    }
    
    /**
     * 角色推荐
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterRecommendation {
        private String name;
        private String role;
        private String personalityTraits;
        private String motivation;
        private String arcSummary;
    }
    
    /**
     * 字数范围
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordCountRange {
        private Integer minWords;
        private Integer maxWords;
        private Integer recommendedChapters;
    }
}
