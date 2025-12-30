package com.aiwriter.dto;

import lombok.Data;

/**
 * 续写建议
 */
@Data
public class ContinuationSuggestion {
    
    /**
     * 建议类型
     */
    private String type;
    
    /**
     * 建议描述
     */
    private String description;
    
    /**
     * 具体建议内容
     */
    private String suggestion;
    
    /**
     * 优先级（1-5，1最高）
     */
    private Integer priority;
}