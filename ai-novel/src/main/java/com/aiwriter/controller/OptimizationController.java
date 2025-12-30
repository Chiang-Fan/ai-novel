package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.ContentOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容优化Controller
 */
@RestController
@RequestMapping("/api/optimization")
@RequiredArgsConstructor
@Slf4j
public class OptimizationController {
    
    private final ContentOptimizationService optimizationService;
    
    /**
     * 优化文本
     */
    @PostMapping("/optimize")
    public ApiResponse<OptimizationResponse> optimizeText(@RequestBody OptimizationRequest request) {
        return ApiResponse.success("优化成功", optimizationService.optimizeText(request));
    }
    
    /**
     * 检查文本
     */
    @PostMapping("/check")
    public ApiResponse<TextCheckResponse> checkText(@RequestParam String text,
                                                @RequestParam(required = false) Long chapterId) {
        return ApiResponse.success("检查完成", optimizationService.checkText(text, chapterId));
    }
    
    /**
     * 获取优化历史
     */
    @GetMapping("/history/{chapterId}")
    public ApiResponse<List<OptimizationResponse>> getHistory(
            @PathVariable Long chapterId,
            @RequestParam(required = false) String optimizationType) {
        return ApiResponse.success("获取成功", optimizationService.getOptimizationHistory(chapterId, optimizationType));
    }
    
    /**
     * 应用优化
     */
    @PostMapping("/{optimizationId}/apply")
    public ApiResponse<String> applyOptimization(@PathVariable Long optimizationId) {
        optimizationService.applyOptimization(optimizationId);
        return ApiResponse.success("应用成功", null);
    }
    
    /**
     * 评分
     */
    @PostMapping("/{optimizationId}/rate")
    public ApiResponse<String> rateOptimization(@PathVariable Long optimizationId,
                                         @RequestParam Integer rating) {
        optimizationService.rateOptimization(optimizationId, rating);
        return ApiResponse.success("评分成功", null);
    }
    
    /**
     * 获取优化规则
     */
    @GetMapping("/rules")
    public ApiResponse<List<OptimizationRuleResponse>> getRules(
            @RequestParam(required = false) String ruleType) {
        return ApiResponse.success("获取成功", optimizationService.getRules(ruleType));
    }
}
