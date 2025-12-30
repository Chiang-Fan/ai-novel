package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容优化请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRequest {
    
    private Long chapterId;
    private String text;
    private String optimizationType; // POLISH, GRAMMAR, DIALOGUE, DESCRIPTION, RHYTHM
    private Boolean includeExplanation;
    
    @Builder.Default
    private Integer maxVersions = 3;
}
