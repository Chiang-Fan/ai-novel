package com.aiwriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 内容分析请求DTO
 */
public class ContentAnalysisRequest {
    
    private Long novelId;
    
    private Long chapterId;
    
    @NotBlank(message = "内容不能为空")
    @Size(min = 50, message = "内容至少需要50个字符才能进行有效分析")
    private String content;
    
    /**
     * 是否需要深度分析（包含角色关系、伏笔等）
     */
    private Boolean deepAnalysis = true;
    
    public ContentAnalysisRequest() {}
    
    public ContentAnalysisRequest(Long novelId, String content) {
        this.novelId = novelId;
        this.content = content;
    }
    
    // Getters and Setters
    
    public Long getNovelId() {
        return novelId;
    }
    
    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }
    
    public Long getChapterId() {
        return chapterId;
    }
    
    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Boolean getDeepAnalysis() {
        return deepAnalysis;
    }
    
    public void setDeepAnalysis(Boolean deepAnalysis) {
        this.deepAnalysis = deepAnalysis;
    }
}
