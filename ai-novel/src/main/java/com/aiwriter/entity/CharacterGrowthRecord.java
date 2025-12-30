package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色成长记录实体
 */
@Entity
@Table(name = "character_growth_records", indexes = {
    @Index(name = "idx_character_id", columnList = "character_id"),
    @Index(name = "idx_chapter_id", columnList = "chapter_id"),
    @Index(name = "idx_record_time", columnList = "record_time")
})
@Data
public class CharacterGrowthRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "character_id", nullable = false)
    private Long characterId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;
    
    /**
     * 属性快照（JSON格式）
     * 示例: {"personality": {"brave": 7, "cautious": 3}, "abilities": {"swordPlay": 6}}
     */
    @Column(columnDefinition = "JSON")
    private String attributes;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (recordTime == null) {
            recordTime = LocalDateTime.now();
        }
    }
}
