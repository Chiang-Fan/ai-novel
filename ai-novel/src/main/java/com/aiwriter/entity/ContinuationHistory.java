package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 智能续写历史记录
 * 记录每次续写的上下文、生成内容、配置和评分
 */
@Data
@Entity
@Table(name = "continuation_history")
public class ContinuationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 所属章节ID
     */
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    /**
     * 续写位置（字符偏移量）
     */
    @Column(nullable = false)
    private Integer position;
    
    /**
     * 续写前的上下文
     */
    @Lob
    @Column(name = "context_before", columnDefinition = "TEXT")
    private String contextBefore;
    
    /**
     * 上下文长度（字数）
     */
    @Column(name = "context_length")
    private Integer contextLength;
    
    /**
     * 生成的续写内容
     */
    @Lob
    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent;
    
    /**
     * 续写配置（JSON格式）
     * {
     *   "length": "MEDIUM",        // SHORT/MEDIUM/LONG
     *   "style": "CONSISTENT",     // 文风选择
     *   "plotHint": "...",         // 情节提示
     *   "emotionTone": "...",      // 情感倾向
     *   "temperature": 0.7         // 创造性温度
     * }
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String settings;
    
    /**
     * 生成质量评分（0.0-1.0）
     */
    @Column(name = "quality_score")
    private Double qualityScore;
    
    /**
     * 文风一致性评分（0.0-1.0）
     */
    @Column(name = "style_consistency")
    private Double styleConsistency;
    
    /**
     * 情节连贯性评分（0.0-1.0）
     */
    @Column(name = "plot_coherence")
    private Double plotCoherence;
    
    /**
     * 是否被采纳
     */
    @Column(nullable = false)
    private Boolean accepted = false;
    
    /**
     * 采纳时间
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    /**
     * 生成方案编号（同一次请求可能生成多个方案）
     */
    @Column(name = "variant_number")
    private Integer variantNumber = 1;
    
    /**
     * 关联的批次ID（同一次生成的多个方案共享此ID）
     */
    @Column(name = "batch_id")
    private String batchId;
    
    /**
     * 用户反馈
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * 续写长度类型
     */
    public enum LengthType {
        SHORT,      // 短续写 200-500字
        MEDIUM,     // 中续写 500-1000字
        LONG        // 长续写 1000-2000字
    }
    
    /**
     * 文风类型
     */
    public enum StyleType {
        CONSISTENT,     // 保持一致
        CREATIVE,       // 创意发挥
        FORMAL,         // 正式严谨
        CASUAL          // 轻松随意
    }
}
