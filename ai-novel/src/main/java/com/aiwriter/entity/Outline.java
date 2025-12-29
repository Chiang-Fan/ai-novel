package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "outlines")
public class Outline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "parent_id")
    private Long parentId; // 父节点ID，用于树形结构
    
    @Column(name = "node_type", nullable = false, length = 20)
    private String nodeType; // ARC, VOLUME, CHAPTER, SECTION
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber; // 序号
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String summary; // 概要
    
    @Column(name = "target_word_count")
    private Integer targetWordCount; // 目标字数
    
    @Column(name = "key_events", columnDefinition = "TEXT")
    private String keyEvents; // 关键事件JSON数组
    
    @Column(name = "character_focus", columnDefinition = "TEXT")
    private String characterFocus; // 焦点角色JSON数组
    
    @Column(name = "plot_points", columnDefinition = "TEXT")
    private String plotPoints; // 情节点JSON数组
    
    @Column(columnDefinition = "TEXT")
    private String themes; // 主题标签JSON数组
    
    @Column(length = 20)
    private String status = "PLANNED"; // PLANNED, IN_PROGRESS, COMPLETED
    
    @Column(name = "chapter_id")
    private Long chapterId; // 关联的实际章节ID
    
    @Column(columnDefinition = "TEXT")
    private String notes; // 备注
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
