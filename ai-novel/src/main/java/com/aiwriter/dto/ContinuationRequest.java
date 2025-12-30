package com.aiwriter.dto;

import lombok.Data;

/**
 * 续写请求
 */
@Data
public class ContinuationRequest {
    private Long chapterId;
    private String sourceText;  // 上文内容
    private String style;  // SERIOUS, LIGHT, SUSPENSE, ROMANTIC, ACTION
    private String length;  // SENTENCE, PARAGRAPH, SECTION
    private String additionalContext;  // 额外上下文（可选）
    private Boolean checkOutline;  // 是否检查大纲一致性
}
