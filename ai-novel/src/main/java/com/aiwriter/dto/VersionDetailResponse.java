package com.aiwriter.dto;

import lombok.Data;

/**
 * 版本详情响应（包含内容）
 */
@Data
public class VersionDetailResponse extends VersionResponse {
    private String content;
}
