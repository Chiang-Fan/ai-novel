package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 导入的章节
 * 存储从文本导入识别出的章节内容
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "imported_chapters")
public class ImportedChapter extends BaseEntity {

    /**
     * 关联的导入记录ID
     */
    @Column(name = "text_import_id", nullable = false)
    private Long textImportId;

    /**
     * 关联的小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    /**
     * 章节标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 章节内容
     */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 章节序号
     */
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    /**
     * 字数统计
     */
    @Column(name = "word_count")
    private Integer wordCount = 0;

    /**
     * 段落数
     */
    @Column(name = "paragraph_count")
    private Integer paragraphCount = 0;

    /**
     * 是否已转换为正式章节
     */
    @Column(name = "converted_to_chapter")
    private Boolean convertedToChapter = false;

    /**
     * 转换后的章节ID
     */
    @Column(name = "converted_chapter_id")
    private Long convertedChapterId;

    /**
     * 质量评分（由AI评估）
     */
    @Column(name = "quality_score")
    private Double qualityScore;

    /**
     * 建议（AI提出的改进建议）
     */
    @Column(columnDefinition = "TEXT")
    private String suggestions;
}
