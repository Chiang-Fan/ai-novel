package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 文本导入记录
 * 用于跟踪导入的文本文件及其处理状态
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "text_imports")
public class TextImport extends BaseEntity {

    /**
     * 关联的小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    /**
     * 原始文件名
     */
    @Column(nullable = false, length = 255)
    private String fileName;

    /**
     * 文件类型（txt/docx/pdf/html等）
     */
    @Column(length = 20)
    private String fileType;

    /**
     * 原始文本内容
     */
    @Column(columnDefinition = "LONGTEXT")
    private String rawContent;

    /**
     * 清理后的文本（去除格式）
     */
    @Column(columnDefinition = "LONGTEXT")
    private String cleanedContent;

    /**
     * 识别的章节数
     */
    @Column(name = "chapter_count")
    private Integer chapterCount = 0;

    /**
     * 总字数
     */
    @Column(name = "total_words")
    private Integer totalWords = 0;

    /**
     * 提取的地点列表（逗号分隔）
     */
    @Column(name = "extracted_locations", columnDefinition = "TEXT")
    private String extractedLocations;

    /**
     * 提取的人物列表（逗号分隔）
     */
    @Column(name = "extracted_characters", columnDefinition = "TEXT")
    private String extractedCharacters;

    /**
     * 提取的事件列表（逗号分隔）
     */
    @Column(name = "extracted_events", columnDefinition = "TEXT")
    private String extractedEvents;

    /**
     * 处理状态（uploaded/parsing/parsed/failed）
     */
    @Column(length = 20)
    private String status = "uploaded";

    /**
     * 错误信息（如果处理失败）
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 是否已确认导入
     */
    @Column(name = "confirmed")
    private Boolean confirmed = false;

    /**
     * 版本号
     */
    @Column(name = "version")
    private Integer version = 1;
}
