package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 优化规则响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRuleResponse {
    
    private Long id;
    private String name;
    private String ruleType;
    private String pattern;
    private String description;
    private String suggestion;
    private String severity;
    private Boolean isEnabled;
    private Integer usageCount;
    private LocalDateTime createdAt;
}
