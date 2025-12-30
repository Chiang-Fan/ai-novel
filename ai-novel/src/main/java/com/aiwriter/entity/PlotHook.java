package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 伏笔管理实体
 * 专门管理伏笔的"埋设-触发-解决"完整生命周期
 * 与 PlotThread 的区别：
 * - PlotThread: 广义的剧情线索（主线、支线、伏笔等）
 * - PlotHook: 专门的伏笔管理，关注埋设和触发时机
 */
@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "plot_hooks", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_planted_chapter", columnList = "planted_in_chapter"),
    @Index(name = "idx_expected_chapter", columnList = "expected_chapter")
})
public class PlotHook extends BaseEntity {
    
    /**
     * 伏笔状态枚举
     */
    public enum Status {
        PENDING,     // 未触发（已埋设，等待触发）
        HINTED,      // 已暗示（铺垫中）
        TRIGGERED,   // 已触发（伏笔已揭示）
        RESOLVED     // 已解决（完全解决）
    }
    
    /**
     * 伏笔类型枚举
     */
    public enum ForeshadowingType {
        EXPLICIT,    // 明示伏笔（明确提示，如"三年之约"）
        IMPLICIT,    // 暗示伏笔（隐晦线索，如"神秘令牌"）
        CHEKHOV_GUN  // 契诃夫的枪（必须触发的元素）
    }
    
    /**
     * 所属小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    /**
     * 伏笔标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 伏笔描述
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    /**
     * 埋设章节号
     */
    @Column(name = "planted_in_chapter", nullable = false)
    private Integer plantedInChapter;
    
    /**
     * 建议揭示章节号（AI推荐或用户设定）
     */
    @Column(name = "expected_chapter")
    private Integer expectedChapter;
    
    /**
     * 实际触发章节号
     */
    @Column(name = "triggered_in_chapter")
    private Integer triggeredInChapter;
    
    /**
     * 解决章节号
     */
    @Column(name = "resolved_in_chapter")
    private Integer resolvedInChapter;
    
    /**
     * 伏笔状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;
    
    /**
     * 伏笔类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ForeshadowingType type;
    
    /**
     * 优先级（1-10，10 最高）
     */
    @Column(nullable = false)
    private Integer priority = 5;
    
    /**
     * 解决说明（如何解决的）
     */
    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;
    
    /**
     * 相关角色（JSON 数组格式）
     */
    @Column(name = "related_characters", columnDefinition = "TEXT")
    private String relatedCharacters;
    
    /**
     * 是否由 AI 自动检测
     */
    @Column(name = "is_auto_detected", nullable = false)
    private Boolean isAutoDetected = false;
    
    /**
     * 伏笔内容引用（埋设时的原文片段）
     */
    @Column(name = "content_reference", columnDefinition = "TEXT")
    private String contentReference;
    
    /**
     * 触发时间
     */
    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;
    
    /**
     * 解决时间
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    /**
     * 用户备注
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    /**
     * 判断伏笔是否超期未触发
     * @param currentChapter 当前章节号
     * @return 是否超期
     */
    public boolean isOverdue(int currentChapter) {
        if (status == Status.RESOLVED || status == Status.TRIGGERED) {
            return false;
        }
        if (expectedChapter == null) {
            return false;
        }
        // 超过预期章节 5 章还未触发，认为超期
        return currentChapter > expectedChapter + 5;
    }
    
    /**
     * 获取伏笔持续章节数
     */
    public Integer getDuration() {
        if (triggeredInChapter != null && plantedInChapter != null) {
            return triggeredInChapter - plantedInChapter;
        }
        return null;
    }
}
