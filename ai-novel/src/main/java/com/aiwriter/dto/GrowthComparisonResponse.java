package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthComparisonResponse {
    private Long characterId;
    private String characterName;
    private Map<String, AttributeComparison> comparison;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeComparison {
        private Object from;
        private Object to;
        private Object change;
        private Double changeRate;
    }
}
