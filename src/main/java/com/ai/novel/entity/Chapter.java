package com.ai.novel.entity;

import com.ai.novel.entity.enums.ChapterStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 章节实体
 */
@Entity
@Table(name = "chapters", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_scene_id", columnList = "scene_id"),
    @Index(name = "idx_chapter_number", columnList = "chapter_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属小说
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonIgnoreProperties({"chapters", "characters", "scenes", "outlineNodes", "plotThreads", "worldSettings"})
    @ToString.Exclude
    private Novel novel;

    /**
     * 所属场景
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    @JsonIgnoreProperties({"novel", "chapters"})
    @ToString.Exclude
    private Scene scene;

    /**
     * 章节号(全书)
     */
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    /**
     * 场景内章节号
     */
    @Column(name = "chapter_in_scene")
    private Integer chapterInScene;

    /**
     * 章节标题
     */
    @Column(length = 255)
    private String title;

    /**
     * 章节完整内容
     */
    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    /**
     * 章节摘要
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    /**
     * 续写方向指导
     */
    @Lob
    @Column(name = "writing_direction", columnDefinition = "TEXT")
    private String writingDirection;

    /**
     * 字数统计
     */
    @Column(name = "word_count")
    @Builder.Default
    private Integer wordCount = 0;

    /**
     * 章节状态
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private ChapterStatus status = ChapterStatus.DRAFT;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 更新字数统计
     */
    public void updateWordCount() {
        if (content != null) {
            this.wordCount = content.length();
        } else {
            this.wordCount = 0;
        }
    }
}
