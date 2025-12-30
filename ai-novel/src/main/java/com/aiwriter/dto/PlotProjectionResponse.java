package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 情节推演响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotProjectionResponse {
    
    private Long id;
    private Long novelId;
    private Long currentChapterId;
    private String projectionText;
    private String projectionType;
    private Integer chaptersAhead;
    private Integer confidenceScore;
    private List<KeyEvent> keyEvents;
    private Map<String, String> characterChanges;
    private List<PlotThread> plotThreads;
    private Boolean isAdopted;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyEvent {
        private String event;
        private Integer chapterNumber;
        private String importance;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlotThread {
        private String threadName;
        private String development;
        private String status;
    }
}
