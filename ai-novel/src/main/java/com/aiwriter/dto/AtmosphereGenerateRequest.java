package com.aiwriter.dto;

import lombok.Data;

/**
 * 氛围生成请求
 */
@Data
public class AtmosphereGenerateRequest {
    private Long sceneId;
    private String atmosphereType;  // PEACEFUL, TENSE, ROMANTIC, MYSTERIOUS等
    private String plotContext;  // 情节上下文
    private String emotionalTone;  // 情感基调
    private Integer length;  // 期望长度（字数）
    private Long templateId;  // 可选：基于特定模板
}
