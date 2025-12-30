package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthAnalysisResponse {
    private Long characterId;
    private String characterName;
    private String timeRange;
    private String summary;
    private List<AttributeTrend> trends;
    private List<String> suggestions;
    private List<String> warnings;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeTrend {
        private String attribute;
        private String attributeName;
        private String trend; // RISING, FALLING, STABLE
        private Double change;
        private String analysis;
    }
}
