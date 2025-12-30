package com.aiwriter.dto;

import lombok.Data;

/**
 * 创建/更新风格请求
 */
@Data
public class UpdateWritingStyleRequest {
    private String perspective;
    private String tone;
    private String sentenceStyle;
    private String keywords; // JSON 字符串
    private String descriptionDensity;
}
