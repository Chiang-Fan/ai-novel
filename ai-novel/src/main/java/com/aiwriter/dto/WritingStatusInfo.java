package com.aiwriter.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 当前写作状态信息
 */
@Data
public class WritingStatusInfo {
    
    // 写作风格信息
    private WritingStyleInfo writingStyle;
    
    // 核心角色信息
    private List<CharacterInfo> coreCharacters;
    
    // 近期出现的角色
    private List<CharacterInfo> recentCharacters;
    
    // 当前进度信息
    private ProgressInfo progress;
    
    // 建议续写方向
    private List<SuggestionInfo> continuationSuggestions;
    
    // 当前场景信息
    private SceneInfo currentScene;
    
    // 情节线索信息
    private List<PlotThreadInfo> plotThreads;
    
    @Data
    public static class WritingStyleInfo {
        private String perspective; // 叙述视角
        private String tone; // 语气风格
        private String sentenceStyle; // 句式特点
        private List<String> keywords; // 关键词
        private String description; // 风格描述
        private Integer avgSentenceLength; // 平均句长
        private Double dialogueRatio; // 对话占比
        private String descriptionDensity; // 描写密度
        private Double confidenceScore; // 置信度
    }
    
    @Data
    public static class CharacterInfo {
        private Long id;
        private String name;
        private String roleType; // 角色类型
        private String personality; // 性格特征
        private String importanceLevel; // 重要性级别
        private Boolean isGlobalProtagonist; // 是否全局主角
        private String appearance; // 外貌描述
        private String abilities; // 能力特长
        private String motivation; // 动机目标
    }
    
    @Data
    public static class ProgressInfo {
        private Integer currentChapter; // 当前章节号
        private Integer totalChapters; // 总章节数
        private Integer currentWordCount; // 当前字数
        private Integer targetWordCount; // 目标字数
        private Double progressPercentage; // 进度百分比
        private String status; // 状态
    }
    
    @Data
    public static class SuggestionInfo {
        private Long id;
        private String title;
        private String description;
        private String type; // 建议类型
        private Integer priority; // 优先级
        private Double relevanceScore; // 相关性评分
    }
    
    @Data
    public static class SceneInfo {
        private Long id;
        private String name;
        private String location;
        private String sceneType;
        private String description;
        private String atmosphere;
        private List<String> involvedCharacters;
    }
    
    @Data
    public static class PlotThreadInfo {
        private Long id;
        private String name;
        private String type; // 线索类型
        private String description;
        private String status; // 状态
        private List<String> involvedCharacters;
        private List<String> keyEvents;
    }
}