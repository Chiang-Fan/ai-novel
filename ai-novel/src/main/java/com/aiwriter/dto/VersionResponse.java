package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 版本响应
 */
@Data
public class VersionResponse {
    private Long id;
    private Long chapterId;
    private Integer versionNumber;
    private Integer wordCount;
    private String versionTag;
    private String versionNote;
    private String createdBy;
    private String createdType;
    private Boolean isCurrent;
    private LocalDateTime createdAt;
}
