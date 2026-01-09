package com.aiwriter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 自动提取功能配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai-writer.auto-extraction")
public class AutoExtractionConfig {
    
    /**
     * 是否启用自动提取
     */
    private boolean enabled = true;
    
    /**
     * 相似度阈值配置
     */
    private SimilarityThresholds similarity = new SimilarityThresholds();
    
    /**
     * 功能开关
     */
    private FeatureToggles features = new FeatureToggles();
    
    @Data
    public static class SimilarityThresholds {
        /**
         * 场景相似度阈值（0-1），超过此值视为相似场景
         */
        private double scene = 0.75;
        
        /**
         * 大纲相似度阈值
         */
        private double outline = 0.70;
        
        /**
         * 伏笔相似度阈值
         */
        private double plotHook = 0.80;
        
        /**
         * 文风相似度阈值
         */
        private double writingStyle = 0.85;
        
        /**
         * 角色相似度阈值
         */
        private double character = 0.80;
    }
    
    @Data
    public static class FeatureToggles {
        /**
         * 自动提取文风特征
         */
        private boolean extractWritingStyle = true;
        
        /**
         * 自动识别场景
         */
        private boolean extractScene = true;
        
        /**
         * 自动生成大纲
         */
        private boolean extractOutline = true;
        
        /**
         * 自动识别伏笔
         */
        private boolean extractPlotHook = true;
        
        /**
         * 自动提取角色
         */
        private boolean extractCharacter = true;
    }
}
