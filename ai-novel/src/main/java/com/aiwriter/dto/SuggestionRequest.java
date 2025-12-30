package com.aiwriter.dto;

import lombok.Data;

/**
 * 写作建议请求
 */
@Data
public class SuggestionRequest {
    private Long chapterId;
    private String suggestionType;  // PLOT, CONFLICT, CHARACTER, DIALOGUE, PACING
    private String contextText;  // 上下文
    private Boolean includeOutline;  // 是否包含大纲分析
}
