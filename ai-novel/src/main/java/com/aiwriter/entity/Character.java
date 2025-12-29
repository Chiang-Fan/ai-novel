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
