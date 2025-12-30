package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 情节推演请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlotProjectionRequest {
    
    private Long novelId;
    private Long currentChapterId;
    private String projectionType; // SHORT_TERM, MEDIUM_TERM, LONG_TERM
    
    @Builder.Default
    private Integer chaptersAhead = 3;
    
    private Boolean includeCharacterArcs;
    private Boolean includePlotHooks;
}
