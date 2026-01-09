package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "characters")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "role_type", nullable = false, length = 20)
    private String roleType; // PROTAGONIST, ANTAGONIST, SUPPORTING, MINOR
    
    /**
     * 是否为整本书的主角（贯穿全书的核心人物）
     * true: 整体主角，false: 可能是配角或临时主角
     */
    @Column(name = "is_global_protagonist")
    private Boolean isGlobalProtagonist = false;
    
    /**
     * 角色重要性级别（1-10，10为最高）
     * 用于区分主角、重要配角和次要角色
     */
    @Column(name = "importance_level")
    private Integer importanceLevel = 5;
    
    @Column(length = 10)
    private String gender; // MALE, FEMALE, OTHER
    
    private Integer age;
    
    @Column(columnDefinition = "TEXT")
    private String personality; // 性格特征
    
    @Column(columnDefinition = "TEXT")
    private String background; // 背景故事
    
    @Column(columnDefinition = "TEXT")
    private String appearance; // 外貌描述
    
    @Column(columnDefinition = "TEXT")
    private String abilities; // 能力特长
    
    @Column(columnDefinition = "TEXT")
    private String relationships; // 人物关系JSON
    
    @Column(columnDefinition = "TEXT")
    private String motivation; // 动机目标
    
    @Column(columnDefinition = "TEXT")
    private String arc; // 角色弧光
    
    @Column(columnDefinition = "TEXT")
    private String notes; // 备注
    
    /**
     * 角色弧光当前阶段（Qwen-Project.md 新增）
     * 取值：initial（初始）, conflict（冲突）, transformation（转变）, resolution（解决）
     */
    @Column(name = "current_arc_stage", length = 20)
    private String currentArcStage = "initial";
    
    /**
     * 核心信念（Qwen-Project.md 新增）
     * 示例："强者才能生存"
     */
    @Column(name = "core_belief", columnDefinition = "TEXT")
    private String coreBelief;
    
    /**
     * 演变中的信念（Qwen-Project.md 新增）
     * 示例："弱小者也有存在的意义"
     */
    @Column(name = "evolving_belief", columnDefinition = "TEXT")
    private String evolvingBelief;
    
    /**
     * 最后更新的章节号（Qwen-Project.md 新增）
     * 用于追踪角色最近出现的章节
     */
    @Column(name = "last_updated_chapter")
    private Integer lastUpdatedChapter;
    
    /**
     * 首次出现章节号
     */
    @Column(name = "first_appearance_chapter")
    private Integer firstAppearanceChapter;
    
    /**
     * 最后出现章节号
     */
    @Column(name = "last_appearance_chapter")
    private Integer lastAppearanceChapter;
    
    /**
     * 出现次数
     */
    @Column(name = "appearance_count")
    private Integer appearanceCount = 0;
    
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
