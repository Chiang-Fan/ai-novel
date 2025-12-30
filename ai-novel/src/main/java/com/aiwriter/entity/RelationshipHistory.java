package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色关系历史实体
 */
@Data
@Entity
@Table(name = "relationship_history")
public class RelationshipHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "relationship_id", nullable = false)
    private Long relationshipId;
    
    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;
    
    @Column(name = "old_strength")
    private Integer oldStrength;
    
    @Column(name = "new_strength")
    private Integer newStrength;
    
    @Column(name = "old_type", length = 50)
    private String oldType;
    
    @Column(name = "new_type", length = 50)
    private String newType;
    
    @Column(name = "event_desc", columnDefinition = "TEXT")
    private String eventDesc;
    
    @Column(name = "related_chapter_id")
    private Long relatedChapterId;
    
    @Column(name = "change_time", nullable = false)
    private LocalDateTime changeTime;
    
    @PrePersist
    protected void onCreate() {
        if (changeTime == null) {
            changeTime = LocalDateTime.now();
        }
    }
}
