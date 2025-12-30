package com.aiwriter.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 情节一致性检查结果
 */
@Data
public class PlotConsistencyCheckResult {
    
    /**
     * 总体一致性评分（0-100）
     */
    private Integer overallConsistencyScore;
    
    /**
     * 是否通过检查
     */
    private Boolean passed;
    
    /**
     * 问题列表
     */
    private List<ConsistencyIssue> issues = new ArrayList<>();
    
    /**
     * 改进建议
     */
    private String improvementSuggestions;
    
    /**
     * 检查时间
     */
    private String checkedAt;
    
    @Data
    public static class ConsistencyIssue {
        private String type;
        private String description;
        private String location;
        private String suggestion;
        private Integer severity;
    }
}