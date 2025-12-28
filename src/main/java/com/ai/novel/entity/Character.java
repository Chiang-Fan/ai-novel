package com.ai.novel.entity;

import com.ai.novel.entity.enums.CharacterImportance;
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
 * 角色实体
 */
@Entity
@Table(name = "characters", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_importance", columnList = "importance_level"),
    @Index(name = "idx_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属小说
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonIgnoreProperties({"chapters", "characters", "scenes", "outlineNodes", "plotThreads", "worldSettings"})
    @ToString.Exclude
    private Novel novel;

    /**
     * 人物姓名
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 别名/外号
     */
    @Column(length = 200)
    private String alias;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别
     */
    @Column(length = 20)
    private String gender;

    /**
     * 外貌描述
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String appearance;

    /**
     * 显著特征
     */
    @Lob
    @Column(name = "distinctive_features", columnDefinition = "TEXT")
    private String distinctiveFeatures;

    /**
     * 着装风格
     */
    @Lob
    @Column(name = "clothing_style", columnDefinition = "TEXT")
    private String clothingStyle;

    /**
     * 性格描述
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String personality;

    /**
     * 性格标签
     */
    @Column(name = "personality_tags", length = 500)
    private String personalityTags;

    /**
     * 角色简介/描述
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 背景故事
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String background;
    
    /**
     * 背景故事(与background相同,用于API兼容)
     */
    @Lob
    @Column(name = "backstory", columnDefinition = "TEXT")
    private String backstory;

    /**
     * 核心动机
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String motivation;
    
    /**
     * 角色目标
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String goals;

    /**
     * 恐惧/弱点
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String fears;

    /**
     * 秘密
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String secrets;

    /**
     * 能力/技能
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String abilities;

    /**
     * 实力等级
     */
    @Column(name = "power_level", length = 50)
    private String powerLevel;

    /**
     * 重要程度
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "importance_level", length = 20, nullable = false)
    @Builder.Default
    private CharacterImportance importanceLevel = CharacterImportance.MINOR;
    
    /**
     * 重要程度(与importanceLevel相同,用于API兼容)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "importance", length = 20)
    private CharacterImportance importance;

    /**
     * 所属阵营
     */
    @Column(length = 50)
    @Builder.Default
    private String camp = "NEUTRAL";

    /**
     * 自定义标签
     */
    @Column(length = 500)
    private String tags;

    /**
     * 角色类型
     */
    @Column(name = "character_type", length = 100)
    private String characterType;

    /**
     * 当前状态
     */
    @Lob
    @Column(name = "current_status", columnDefinition = "TEXT")
    private String currentStatus;
    
    /**
     * 当前状态(与currentStatus相同,用于API兼容)
     */
    @Lob
    @Column(name = "current_state", columnDefinition = "TEXT")
    private String currentState;

    /**
     * 当前位置
     */
    @Column(name = "current_location", length = 200)
    private String currentLocation;

    /**
     * 是否存活
     */
    @Builder.Default
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer alive = 1;

    /**
     * 首次出现章节
     */
    @Column(name = "first_appearance_chapter")
    private Integer firstAppearanceChapter;
    
    /**
     * 首次出现时间(用于API兼容)
     */
    @Column(name = "first_appearance")
    private LocalDateTime firstAppearance;

    /**
     * 最后出现章节
     */
    @Column(name = "last_appearance_chapter")
    private Integer lastAppearanceChapter;
    
    /**
     * 最后出现时间(用于API兼容)
     */
    @Column(name = "last_appearance")
    private LocalDateTime lastAppearance;

    /**
     * 出场次数
     */
    @Column(name = "appearance_count")
    @Builder.Default
    private Integer appearanceCount = 0;

    /**
     * 关键场景列表(JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_scenes", columnDefinition = "JSON")
    private List<Map<String, Object>> keyScenes;

    /**
     * 说话风格/口癖
     */
    @Lob
    @Column(name = "speech_pattern", columnDefinition = "TEXT")
    private String speechPattern;

    /**
     * 其他扩展信息(JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_info", columnDefinition = "JSON")
    private Map<String, Object> extraInfo;

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
     * 关系 - 作为源角色的关系
     */
    @OneToMany(mappedBy = "sourceCharacter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"sourceCharacter", "targetCharacter"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<CharacterRelationship> relationshipsAsSource = new ArrayList<>();

    /**
     * 关系 - 作为目标角色的关系
     */
    @OneToMany(mappedBy = "targetCharacter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"sourceCharacter", "targetCharacter"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<CharacterRelationship> relationshipsAsTarget = new ArrayList<>();
}
