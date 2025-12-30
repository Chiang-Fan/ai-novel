package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.PlotDevelopmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 情节发展建议Controller
 */
@RestController
@RequestMapping("/api/plot")
@RequiredArgsConstructor
@Slf4j
public class PlotDevelopmentController {
    
    private final PlotDevelopmentService plotDevelopmentService;
    
    /**
     * 生成情节建议
     */
    @PostMapping("/suggestions/generate")
    public ApiResponse<List<PlotSuggestionResponse>> generateSuggestions(@RequestParam Long novelId) {
        return ApiResponse.success("生成成功", 
                plotDevelopmentService.generateSuggestions(novelId));
    }
    
    /**
     * 获取建议列表
     */
    @GetMapping("/suggestions/{novelId}")
    public ApiResponse<List<PlotSuggestionResponse>> getSuggestions(
            @PathVariable Long novelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        return ApiResponse.success("获取成功", 
                plotDevelopmentService.getSuggestions(novelId, status, type));
    }
    
    /**
     * 更新建议状态
     */
    @PutMapping("/suggestions/{suggestionId}/status")
    public ApiResponse<String> updateSuggestionStatus(
            @PathVariable Long suggestionId,
            @RequestParam String status) {
        plotDevelopmentService.updateSuggestionStatus(suggestionId, status);
        return ApiResponse.success("更新成功", null);
    }
    
    /**
     * 情节推演
     */
    @PostMapping("/projections")
    public ApiResponse<PlotProjectionResponse> projectPlot(@RequestBody PlotProjectionRequest request) {
        return ApiResponse.success("推演完成", 
                plotDevelopmentService.projectPlot(request));
    }
    
    /**
     * 获取推演历史
     */
    @GetMapping("/projections/{novelId}")
    public ApiResponse<List<PlotProjectionResponse>> getProjections(
            @PathVariable Long novelId,
            @RequestParam(required = false) String type) {
        return ApiResponse.success("获取成功", 
                plotDevelopmentService.getProjections(novelId, type));
    }
    
    /**
     * 采纳推演
     */
    @PostMapping("/projections/{projectionId}/adopt")
    public ApiResponse<String> adoptProjection(@PathVariable Long projectionId) {
        plotDevelopmentService.adoptProjection(projectionId);
        return ApiResponse.success("采纳成功", null);
    }
    
    /**
     * 获取冲突列表
     */
    @GetMapping("/conflicts/{novelId}")
    public ApiResponse<List<ConflictTrackingResponse>> getConflicts(
            @PathVariable Long novelId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success("获取成功", 
                plotDevelopmentService.getConflicts(novelId, status));
    }
}
