package com.aiwriter.dto;

import lombok.Data;

/**
 * 版本对比请求
 */
@Data
public class CompareVersionRequest {
    private Long versionIdFrom;
    private Long versionIdTo;
}
