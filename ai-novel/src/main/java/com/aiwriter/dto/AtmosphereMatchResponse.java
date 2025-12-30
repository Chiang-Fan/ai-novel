package com.aiwriter.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 氛围匹配建议响应
 */
@Data
@Builder
public class AtmosphereMatchResponse {
    private String recommendedType;  // 推荐的氛围类型
    private String reason;  // 推荐原因
    private List<TemplateRecommendation> templates;  // 推荐的模板
    private ConsistencyCheck consistencyCheck;  // 一致性检查
    
    @Data
    @Builder
    public static class TemplateRecommendation {
        private Long templateId;
        private String templateName;
        private String atmosphereType;
        private Integer matchScore;  // 匹配分数 0-100
        private String matchReason;
    }
    
    @Data
    @Builder
    public static class ConsistencyCheck {
        private Boolean isConsistent;
        private String issue;  // 不一致的问题
        private String suggestion;  // 改进建议
    }
}
