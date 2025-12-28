package com.ai.novel.entity;

import com.ai.novel.entity.enums.NovelStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 小说实体
 */
@Entity
@Table(name = "novels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 书名
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * 作者
     */
    @Column(length = 100)
    private String author;

    /**
     * 小说类型
     */
    @Column(length = 50)
    private String type;

    /**
     * 简介
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 大纲
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String outline;

    /**
     * 用于风格提取的样本正文
     */
    @Lob
    @Column(name = "sample_text", columnDefinition = "TEXT")
    private String sampleText;

    /**
     * 提取的创作风格特征(JSON格式)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "writing_style", columnDefinition = "JSON")
    private Map<String, Object> writingStyle;

    /**
     * 目标字数
     */
    @Column(name = "target_word_count")
    private Integer targetWordCount;

    /**
     * 当前字数
     */
    @Column(name = "current_word_count")
    @Builder.Default
    private Integer currentWordCount = 0;

    /**
     * 章节数
     */
    @Column(name = "chapter_count")
    @Builder.Default
    private Integer chapterCount = 0;

    /**
     * 小说状态
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private NovelStatus status = NovelStatus.DRAFT;

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
     * 关联关系 - 章节
     */
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("novel")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();

    /**
     * 关联关系 - 角色
     */
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("novel")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Character> characters = new ArrayList<>();

    /**
     * 关联关系 - 场景
     */
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("novel")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Scene> scenes = new ArrayList<>();

    /**
     * 关联关系 - 大纲节点
     */
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("novel")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<OutlineNode> outlineNodes = new ArrayList<>();

    /**
     * 关联关系 - 情节线
     */
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("novel")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PlotThread> plotThreads = new ArrayList<>();

    /**
     * 关联关系 - 世界观设定
     */
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("novel")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<WorldSetting> worldSettings = new ArrayList<>();
}
