package com.aiwriter.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能推荐实体
 * 
 * 根据小说当前状态（文风、伏笔、章节分析）自动生成写作建议
 */
@Data
@Entity
@Table(name = "auto_suggestions", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_chapter_id", columnList = "chapter_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_priority", columnList = "priority"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_relevance_score", columnList = "relevance_score")
})
public class AutoSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(name = "chapter_number")
    private Integer chapterNumber;

    /**
     * 推荐类型
     * PLOT: 情节推荐
     * CHARACTER: 角色发展推荐
     * STYLE: 文风调整推荐
     * PACING: 节奏调整推荐
     * HOOK: 伏笔提醒
     * CONFLICT: 冲突强化推荐
     * THEME: 主题深化推荐
     * QUALITY: 质量优化推荐
     * WORLD_BUILDING: 世界观补充推荐
     * EMOTION: 情感曲线推荐
     */
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private SuggestionType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 优先级 1-10
     */
    @Column(nullable = false)
    private Integer priority;

    /**
     * 相关性评分 0.0-1.0
     */
    @Column(name = "relevance_score", nullable = false)
    private Double relevanceScore;

    /**
     * 推荐依据（JSON数组）
     */
    @Column(columnDefinition = "TEXT")
    private String basis;

    /**
     * 状态
     * ACTIVE: 活跃
     * ACCEPTED: 已采纳
     * REJECTED: 已拒绝
     * EXPIRED: 已过期
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SuggestionStatus status = SuggestionStatus.ACTIVE;

    /**
     * 预期影响（JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String expectedImpact;

    /**
     * 参考数据ID（例如：相关的伏笔ID、分析结果ID等）
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * 参考类型（PLOT_HOOK/ANALYSIS_RESULT/WRITING_STYLE等）
     */
    @Column(name = "reference_type", length = 30)
    private String referenceType;

    /**
     * 用户反馈
     */
    @Column(columnDefinition = "TEXT")
    private String feedback;

    /**
     * 采纳时间
     */
    @Column(name = "accepted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;

    /**
     * 拒绝时间
     */
    @Column(name = "rejected_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectedAt;

    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SuggestionStatus.ACTIVE;
        }
        // 默认7天后过期
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(7);
        }
    }

    public enum SuggestionType {
        PLOT,           // 情节推荐
        CHARACTER,      // 角色发展推荐
        STYLE,          // 文风调整推荐
        PACING,         // 节奏调整推荐
        HOOK,           // 伏笔提醒
        CONFLICT,       // 冲突强化推荐
        THEME,          // 主题深化推荐
        QUALITY,        // 质量优化推荐
        WORLD_BUILDING, // 世界观补充推荐
        EMOTION         // 情感曲线推荐
    }

    public enum SuggestionStatus {
        ACTIVE,    // 活跃
        ACCEPTED,  // 已采纳
        REJECTED,  // 已拒绝
        EXPIRED    // 已过期
    }
}
