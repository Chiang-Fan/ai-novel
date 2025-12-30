package com.aiwriter.dto;

import lombok.Data;

/**
 * 更新版本标签请求
 */
@Data
public class UpdateVersionTagRequest {
    private String versionTag;
    private String versionNote;
}
