package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.StyleTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 风格迁移Controller
 */
@RestController
@RequestMapping("/api/style")
@RequiredArgsConstructor
@Slf4j
public class StyleTransferController {
    
    private final StyleTransferService styleTransferService;
    
    /**
     * 分析文本风格
     */
    @PostMapping("/analyze")
    public ApiResponse<StyleAnalysisResponse> analyzeStyle(
            @RequestParam Long chapterId,
            @RequestParam String text) {
        return ApiResponse.success("分析完成", 
                styleTransferService.analyzeStyle(chapterId, text));
    }
    
    /**
     * 转换文本风格
     */
    @PostMapping("/convert")
    public ApiResponse<StyleConversionResponse> convertStyle(
            @RequestBody StyleConversionRequest request) {
        return ApiResponse.success("转换成功", 
                styleTransferService.convertStyle(request));
    }
    
    /**
     * 获取所有风格
     */
    @GetMapping("/styles")
    public ApiResponse<List<WritingStyleResponse>> getAllStyles(
            @RequestParam(required = false) String category) {
        return ApiResponse.success("获取成功", 
                styleTransferService.getAllStyles(category));
    }
    
    /**
     * 获取转换历史
     */
    @GetMapping("/history/{chapterId}")
    public ApiResponse<List<StyleConversionResponse>> getHistory(
            @PathVariable Long chapterId) {
        return ApiResponse.success("获取成功", 
                styleTransferService.getConversionHistory(chapterId));
    }
    
    /**
     * 应用转换
     */
    @PostMapping("/{conversionId}/apply")
    public ApiResponse<String> applyConversion(@PathVariable Long conversionId) {
        styleTransferService.applyConversion(conversionId);
        return ApiResponse.success("应用成功", null);
    }
    
    /**
     * 评分
     */
    @PostMapping("/{conversionId}/rate")
    public ApiResponse<String> rateConversion(@PathVariable Long conversionId,
                                              @RequestParam Integer rating) {
        styleTransferService.rateConversion(conversionId, rating);
        return ApiResponse.success("评分成功", null);
    }
}
