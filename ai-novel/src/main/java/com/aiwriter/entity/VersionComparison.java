package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 版本对比缓存实体
 */
@Data
@Entity
@Table(name = "version_comparisons")
public class VersionComparison {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "version_id_from", nullable = false)
    private Long versionIdFrom;
    
    @Column(name = "version_id_to", nullable = false)
    private Long versionIdTo;
    
    @Lob
    @Column(name = "diff_result", columnDefinition = "LONGTEXT")
    private String diffResult;
    
    @Column(name = "added_count")
    private Integer addedCount = 0;
    
    @Column(name = "deleted_count")
    private Integer deletedCount = 0;
    
    @Column(name = "modified_count")
    private Integer modifiedCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
