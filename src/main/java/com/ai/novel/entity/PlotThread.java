package com.ai.novel.entity;

import com.ai.novel.entity.enums.PlotThreadStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 情节线/伏笔实体
 */
@Entity
@Table(name = "plot_threads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PlotThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonIgnoreProperties({"plotThreads"})
    @ToString.Exclude
    private Novel novel;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "planted_chapter")
    private Integer plantedChapter;

    @Column(name = "expected_reveal_chapter")
    private Integer expectedRevealChapter;

    @Column(name = "actual_reveal_chapter")
    private Integer actualRevealChapter;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private PlotThreadStatus status = PlotThreadStatus.PLANTED;

    @Column(name = "importance_level", length = 20)
    @Builder.Default
    private String importanceLevel = "MINOR";

    @Column(length = 500)
    private String tags;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
