package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 章节版本实体
 */
@Data
@Entity
@Table(name = "chapter_versions")
public class ChapterVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;
    
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(name = "word_count")
    private Integer wordCount = 0;
    
    @Column(name = "version_tag", length = 100)
    private String versionTag;
    
    @Column(name = "version_note", columnDefinition = "TEXT")
    private String versionNote;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "created_type", length = 50)
    private String createdType; // MANUAL, AUTO_SAVE, AUTO_SNAPSHOT
    
    @Column(name = "is_current")
    private Boolean isCurrent = false;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
