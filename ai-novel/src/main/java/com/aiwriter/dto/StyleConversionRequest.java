package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风格转换请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleConversionRequest {
    
    private Long chapterId;
    private String text;
    private Long targetStyleId;
    private Boolean analyzeSource; // 是否分析源风格
    
    @Builder.Default
    private Integer maxVersions = 3;
}
