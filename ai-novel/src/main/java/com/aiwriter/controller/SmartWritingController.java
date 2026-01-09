package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.ContentAnalysisService;
import com.aiwriter.service.ContinuationSuggestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能创作Controller
 * 提供内容分析和续写建议的API接口
 */
@RestController
@RequestMapping("/api/smart-writing")
@CrossOrigin(origins = "*")
public class SmartWritingController {
    
    @Autowired
    private ContentAnalysisService analysisService;
    
    @Autowired
    private ContinuationSuggestionService suggestionService;
    
    @Autowired
    private com.aiwriter.service.WritingStatusService writingStatusService;
    
    // ==================== 内容分析API ====================
    
    /**
     * 分析内容片段
     * POST /api/smart-writing/analyze
     */
    @PostMapping("/analyze")
    public ApiResponse<ContentAnalysisResponse> analyzeContent(
            @Valid @RequestBody ContentAnalysisRequest request) {
        ContentAnalysisResponse result = analysisService.analyzeContent(request);
        return ApiResponse.success("分析完成", result);
    }
    
    /**
     * 获取小说的最新分析
     * GET /api/smart-writing/analysis/novel/{novelId}/latest
     */
    @GetMapping("/analysis/novel/{novelId}/latest")
    public ApiResponse<ContentAnalysisResponse> getLatestAnalysis(
            @PathVariable Long novelId) {
        ContentAnalysisResponse result = analysisService.getLatestAnalysis(novelId);
        if (result == null) {
            return ApiResponse.error("该小说暂无分析记录");
        }
        return ApiResponse.success(result);
    }
    
    /**
     * 获取小说的分析历史
     * GET /api/smart-writing/analysis/novel/{novelId}/history
     */
    @GetMapping("/analysis/novel/{novelId}/history")
    public ApiResponse<List<ContentAnalysisResponse>> getAnalysisHistory(
            @PathVariable Long novelId) {
        List<ContentAnalysisResponse> result = analysisService.getAnalysisHistory(novelId);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取章节的分析
     * GET /api/smart-writing/analysis/chapter/{chapterId}
     */
    @GetMapping("/analysis/chapter/{chapterId}")
    public ApiResponse<ContentAnalysisResponse> getChapterAnalysis(
            @PathVariable Long chapterId) {
        ContentAnalysisResponse result = analysisService.getChapterAnalysis(chapterId);
        if (result == null) {
            return ApiResponse.error("该章节暂无分析记录");
        }
        return ApiResponse.success(result);
    }
    
    // ==================== 续写建议API ====================
    
    /**
     * 生成续写建议
     * POST /api/smart-writing/suggestions/generate
     */
    @PostMapping("/suggestions/generate")
    public ApiResponse<List<ContinuationSuggestionResponse>> generateSuggestions(
            @Valid @RequestBody SuggestionGenerateRequest request) {
        List<ContinuationSuggestionResponse> result = suggestionService.generateSuggestions(request);
        return ApiResponse.success("生成成功", result);
    }
    
    /**
     * 获取小说的未采用建议
     * GET /api/smart-writing/suggestions/novel/{novelId}/unadopted
     */
    @GetMapping("/suggestions/novel/{novelId}/unadopted")
    public ApiResponse<List<ContinuationSuggestionResponse>> getUnadoptedSuggestions(
            @PathVariable Long novelId) {
        List<ContinuationSuggestionResponse> result = suggestionService.getUnadoptedSuggestions(novelId);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取建议详情
     * GET /api/smart-writing/suggestions/{id}
     */
    @GetMapping("/suggestions/{id}")
    public ApiResponse<ContinuationSuggestionResponse> getSuggestion(
            @PathVariable Long id) {
        ContinuationSuggestionResponse result = suggestionService.getSuggestion(id);
        if (result == null) {
            return ApiResponse.error("续写建议不存在");
        }
        return ApiResponse.success(result);
    }
    
    /**
     * 采用续写建议
     * PUT /api/smart-writing/suggestions/{id}/adopt
     */
    @PutMapping("/suggestions/{id}/adopt")
    public ApiResponse<String> adoptSuggestion(
            @PathVariable Long id,
            @RequestParam Long chapterId) {
        suggestionService.adoptSuggestion(id, chapterId);
        return ApiResponse.success("已标记为采用");
    }
    
    /**
     * 基于当前上下文生成新的智能建议
     * POST /api/smart-writing/suggestions/contextual
     */
    @PostMapping("/suggestions/contextual")
    public ApiResponse<List<ContinuationSuggestionResponse>> getContextualSuggestions(
            @Valid @RequestBody ContextualSuggestionRequest request) {
        List<ContinuationSuggestionResponse> result = suggestionService.getContextualSuggestions(request);
        return ApiResponse.success("基于当前上下文的建议生成成功", result);
    }
    
    // ==================== 组合API ====================
    
    /**
     * 一键分析并生成建议
     * POST /api/smart-writing/analyze-and-suggest
     */
    @PostMapping("/analyze-and-suggest")
    public ApiResponse<AnalysisAndSuggestionResult> analyzeAndSuggest(
            @Valid @RequestBody AnalyzeAndSuggestRequest request) {
        
        // 1. 先分析内容
        ContentAnalysisRequest analysisRequest = new ContentAnalysisRequest();
        analysisRequest.setNovelId(request.getNovelId());
        analysisRequest.setContent(request.getContent());
        analysisRequest.setDeepAnalysis(true);
        
        ContentAnalysisResponse analysis = analysisService.analyzeContent(analysisRequest);
        
        // 2. 生成续写建议
        SuggestionGenerateRequest suggestionRequest = new SuggestionGenerateRequest();
        suggestionRequest.setNovelId(request.getNovelId());
        suggestionRequest.setAnalysisId(analysis.getId());
        suggestionRequest.setCount(request.getSuggestionCount() != null ? request.getSuggestionCount() : 3);
        
        List<ContinuationSuggestionResponse> suggestions = 
            suggestionService.generateSuggestions(suggestionRequest);
        
        // 3. 组合结果
        AnalysisAndSuggestionResult result = new AnalysisAndSuggestionResult();
        result.setAnalysis(analysis);
        result.setSuggestions(suggestions);
        
        return ApiResponse.success("分析和建议生成完成", result);
    }
    
    /**
     * 获取当前写作状态
     * GET /api/smart-writing/status/{novelId}
     */
    @GetMapping("/status/{novelId}")
    public ApiResponse<WritingStatusInfo> getWritingStatus(
            @PathVariable Long novelId) {
        WritingStatusInfo result = writingStatusService.getWritingStatus(novelId);
        return ApiResponse.success("获取写作状态成功", result);
    }
    
    /**
     * 组合请求DTO
     */
    public static class AnalyzeAndSuggestRequest {
        @jakarta.validation.constraints.NotNull(message = "小说ID不能为空")
        private Long novelId;
        
        @jakarta.validation.constraints.NotBlank(message = "内容不能为空")
        private String content;
        
        private Integer suggestionCount = 3;
        
        // Getters and Setters
        public Long getNovelId() { return novelId; }
        public void setNovelId(Long novelId) { this.novelId = novelId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Integer getSuggestionCount() { return suggestionCount; }
        public void setSuggestionCount(Integer suggestionCount) { this.suggestionCount = suggestionCount; }
    }
    
    /**
     * 组合结果DTO
     */
    public static class AnalysisAndSuggestionResult {
        private ContentAnalysisResponse analysis;
        private List<ContinuationSuggestionResponse> suggestions;
        
        // Getters and Setters
        public ContentAnalysisResponse getAnalysis() { return analysis; }
        public void setAnalysis(ContentAnalysisResponse analysis) { this.analysis = analysis; }
        
        public List<ContinuationSuggestionResponse> getSuggestions() { return suggestions; }
        public void setSuggestions(List<ContinuationSuggestionResponse> suggestions) { 
            this.suggestions = suggestions; 
        }
    }
}