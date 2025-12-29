package com.aiwriter.dto;

import lombok.Data; /**
 * 章节摘要DTO（列表使用）
 */
@Data
public class ChapterSummaryDTO {
    private Long id;
    private String title;
    private Integer chapterNumber;
    private Integer wordCount;
    private Boolean isAiGenerated;
}
