package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本导入响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextImportResponse {
    /**
     * 导入ID
     */
    private Long importId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 章节数
     */
    private Integer chapterCount;

    /**
     * 总字数
     */
    private Integer totalWords;

    /**
     * 提取的地点
     */
    private List<String> locations;

    /**
     * 提取的人物
     */
    private List<String> characters;

    /**
     * 提取的事件
     */
    private List<String> events;

    /**
     * 识别的章节预览
     */
    private List<ChapterPreview> chapterPreviews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterPreview {
        private Integer chapterNumber;
        private String title;
        private Integer wordCount;
        private String contentPreview; // 前100字预览
    }
}
