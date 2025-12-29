package com.aiwriter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 续写建议生成请求DTO
 */
public class SuggestionGenerateRequest {
    
    @NotNull(message = "小说ID不能为空")
    private Long novelId;
    
    /**
     * 基于的内容分析ID（可选）
     */
    private Long analysisId;
    
    /**
     * 需要生成的建议数量
     */
    @Min(value = 1, message = "至少生成1个建议")
    private Integer count = 3;
    
    /**
     * 期望的字数范围
     */
    private Integer expectedWordCount = 2000;
    
    public SuggestionGenerateRequest() {}
    
    public SuggestionGenerateRequest(Long novelId, Integer count) {
        this.novelId = novelId;
        this.count = count;
    }
    
    // Getters and Setters
    
    public Long getNovelId() {
        return novelId;
    }
    
    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }
    
    public Long getAnalysisId() {
        return analysisId;
    }
    
    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public Integer getExpectedWordCount() {
        return expectedWordCount;
    }
    
    public void setExpectedWordCount(Integer expectedWordCount) {
        this.expectedWordCount = expectedWordCount;
    }
}
