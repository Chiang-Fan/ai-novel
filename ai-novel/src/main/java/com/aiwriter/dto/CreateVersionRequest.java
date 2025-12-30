package com.aiwriter.dto;

import lombok.Data;

/**
 * 创建版本请求
 */
@Data
public class CreateVersionRequest {
    private Long chapterId;
    private String content;
    private String versionTag;
    private String versionNote;
    private String createdType; // MANUAL, AUTO_SAVE, AUTO_SNAPSHOT
}
