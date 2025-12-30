package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

/**
 * 结局分析响应
 */
@Data
public class EndingAnalysisResponse {
    private List<EndingOption> endings;
    private EndingOption recommendedEnding;
    
    @Data
    public static class EndingOption {
        private Long branchId;
        private String endingName;
        private String description;
        private Double qualityScore;
        private String pros; // 优点
        private String cons; // 缺点
        private String characterFate; // 角色命运
        private String plotResolution; // 情节解决方式
        private String emotionalImpact; // 情感影响
    }
}
