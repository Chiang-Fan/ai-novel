package com.ai.novel.entity;

import com.ai.novel.entity.enums.OutlineNodeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 大纲节点实体
 */
@Entity
@Table(name = "outline_nodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OutlineNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonIgnoreProperties({"outlineNodes"})
    @ToString.Exclude
    private Novel novel;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 20)
    private OutlineNodeType nodeType;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private OutlineNode parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("parent")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<OutlineNode> children = new ArrayList<>();

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "scene_id")
    private Long sceneId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(name = "target_word_count")
    private Integer targetWordCount;

    @Column(name = "target_chapters")
    private Integer targetChapters;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_completed", columnDefinition = "TINYINT DEFAULT 0")
    @Builder.Default
    private Integer isCompleted = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
