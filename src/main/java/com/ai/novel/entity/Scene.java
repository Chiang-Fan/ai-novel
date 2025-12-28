package com.ai.novel.entity;

import com.ai.novel.entity.enums.SceneStatus;
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

/**
 * 场景实体
 */
@Entity
@Table(name = "scenes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonIgnoreProperties({"chapters", "characters", "scenes"})
    @ToString.Exclude
    private Novel novel;

    @Column(name = "scene_number", nullable = false)
    private Integer sceneNumber;

    @Column(nullable = false, length = 255)
    private String title;
    
    /**
     * 场景名称(与title相同,用于API兼容)
     */
    @Column(name = "name", length = 255)
    private String name;
    
    /**
     * 场景描述
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "scene_outline", columnDefinition = "TEXT")
    private String sceneOutline;
    
    /**
     * 起始章节
     */
    @Column(name = "start_chapter")
    private Integer startChapter;
    
    /**
     * 结束章节
     */
    @Column(name = "end_chapter")
    private Integer endChapter;
    
    /**
     * 场景目标
     */
    @Lob
    @Column(name = "scene_goals", columnDefinition = "TEXT")
    private String sceneGoals;

    @Column(name = "target_chapters")
    private Integer targetChapters;

    @Column(name = "target_word_count")
    private Integer targetWordCount;

    @Column(length = 500)
    private String atmosphere;

    @Lob
    @Column(name = "main_conflict", columnDefinition = "TEXT")
    private String mainConflict;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_events", columnDefinition = "JSON")
    private List<String> keyEvents;

    @Lob
    @Column(name = "scene_style_notes", columnDefinition = "TEXT")
    private String sceneStyleNotes;

    @Column(name = "current_chapters")
    @Builder.Default
    private Integer currentChapters = 0;

    @Column(name = "current_word_count")
    @Builder.Default
    private Integer currentWordCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private SceneStatus status = SceneStatus.PLANNING;

    @Column(name = "order_index")
    private Integer orderIndex;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("scene")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();
}
