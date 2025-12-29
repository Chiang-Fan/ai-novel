package com.aiwriter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 * 配置小说创作相关参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    
    private NovelConfig novel = new NovelConfig();
    private HistoryConfig history = new HistoryConfig();
    
    @Data
    public static class NovelConfig {
        /**
         * 上下文最大章节数
         */
        private Integer maxContextChapters = 3;
        
        /**
         * 默认字数
         */
        private Integer defaultWordCount = 2000;
        
        /**
         * 最大字数
         */
        private Integer maxWordCount = 10000;
        
        /**
         * 最小字数
         */
        private Integer minWordCount = 500;
    }
    
    @Data
    public static class HistoryConfig {
        /**
         * 最大历史记录条数
         */
        private Integer maxEntries = 100;
        
        /**
         * 历史记录保留天数
         */
        private Integer retentionDays = 90;
    }
}
