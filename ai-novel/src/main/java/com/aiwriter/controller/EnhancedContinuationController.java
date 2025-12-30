package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.EnhancedContinuationRequest;
import com.aiwriter.dto.EnhancedContinuationResponse;
import com.aiwriter.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 增强型续写控制器 - Phase 4 API 端点
 * 支持多维约束、一致性检查和质量评分
 */
@RestController
@RequestMapping("/api/enhanced-continuation")
@RequiredArgsConstructor
@Slf4j
public class EnhancedContinuationController {

    private final EnhancedContinuationService enhancedContinuationService;
    private final MultiDimensionalConstraintEngine constraintEngine;
    private final PlotConsistencyCheckService consistencyCheckService;
    private final DescriptionQualityScoreService qualityScoreService;

    /**
     * 生成增强型续写 - 支持多维约束和迭代优化
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<EnhancedContinuationResponse>> generateEnhancedContinuation(
            @Valid @RequestBody EnhancedContinuationRequest request) {
        
        log.info("请求增强型续写: chapterId={}", request.getChapterId());
        
        try {
            EnhancedContinuationResponse response = 
                enhancedContinuationService.generateEnhancedContinuation(request);
            return ResponseEntity.ok(ApiResponse.success(response, "续写生成成功"));
        } catch (Exception e) {
            log.error("生成续写失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("续写生成失败: " + e.getMessage()));
        }
    }

    /**
     * 查看多维约束信息
     */
    @GetMapping("/constraints/{novelId}/{chapterId}")
    public ResponseEntity<ApiResponse<MultiDimensionalConstraintEngine.CompositeConstraints>> 
            viewConstraints(
            @PathVariable Long novelId,
            @PathVariable Long chapterId) {
        
        try {
            MultiDimensionalConstraintEngine.CompositeConstraints constraints = 
                constraintEngine.buildCompositeConstraints(novelId, chapterId);
            return ResponseEntity.ok(ApiResponse.success(constraints));
        } catch (Exception e) {
            log.error("获取约束信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取约束信息失败"));
        }
    }

    /**
     * 验证续写内容是否违反约束
     */
    @PostMapping("/validate-constraints")
    public ResponseEntity<ApiResponse<MultiDimensionalConstraintEngine.ConstraintValidationResult>> 
            validateConstraints(
            @RequestParam Long novelId,
            @RequestParam Long chapterId,
            @RequestBody String continuationText) {
        
        try {
            MultiDimensionalConstraintEngine.CompositeConstraints constraints = 
                constraintEngine.buildCompositeConstraints(novelId, chapterId);
            
            MultiDimensionalConstraintEngine.ConstraintValidationResult result = 
                constraintEngine.validateAgainstConstraints(continuationText, constraints);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("约束验证失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("约束验证失败"));
        }
    }

    /**
     * 检查情节一致性
     */
    @PostMapping("/check-consistency")
    public ResponseEntity<ApiResponse<PlotConsistencyCheckService.PlotConsistencyCheckResult>> 
            checkPlotConsistency(
            @RequestParam Long novelId,
            @RequestParam Long chapterId,
            @RequestParam String continuationText,
            @RequestParam String previousContent) {
        
        try {
            PlotConsistencyCheckService.PlotConsistencyCheckResult result = 
                consistencyCheckService.checkPlotConsistency(
                    novelId, chapterId, continuationText, previousContent);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("一致性检查失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("一致性检查失败"));
        }
    }

    /**
     * 对描写进行质量评分
     */
    @PostMapping("/quality-score")
    public ResponseEntity<ApiResponse<DescriptionQualityScoreService.DescriptionQualityScore>> 
            scoreDescription(
            @RequestBody String text) {
        
        try {
            DescriptionQualityScoreService.DescriptionQualityScore score = 
                qualityScoreService.scoreDescription(text);
            
            return ResponseEntity.ok(ApiResponse.success(score));
        } catch (Exception e) {
            log.error("质量评分失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("质量评分失败"));
        }
    }

    /**
     * 检查约束提示词
     * 用于调试，查看生成的约束提示词
     */
    @GetMapping("/debug/constraint-prompt/{novelId}/{chapterId}")
    public ResponseEntity<ApiResponse<String>> debugConstraintPrompt(
            @PathVariable Long novelId,
            @PathVariable Long chapterId) {
        
        try {
            MultiDimensionalConstraintEngine.CompositeConstraints constraints = 
                constraintEngine.buildCompositeConstraints(novelId, chapterId);
            
            String prompt = constraintEngine.generateConstraintPrompt(constraints);
            return ResponseEntity.ok(ApiResponse.success(prompt));
        } catch (Exception e) {
            log.error("获取约束提示词失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取约束提示词失败"));
        }
    }

    /**
     * 批量评估多个续写版本
     */
    @PostMapping("/compare-versions")
    public ResponseEntity<ApiResponse<ComparisonResult>> compareVersions(
            @RequestBody CompareVersionsRequest request) {
        
        try {
            ComparisonResult result = new ComparisonResult();
            result.setVersionScores(new java.util.HashMap<>());
            
            // 对每个版本进行评分
            for (String version : request.getContinuationVersions()) {
                DescriptionQualityScoreService.DescriptionQualityScore score = 
                    qualityScoreService.scoreDescription(version);
                result.getVersionScores().put(version, score.getOverallScore());
            }
            
            // 找到最佳版本
            String bestVersion = result.getVersionScores().entrySet().stream()
                    .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                    .map(java.util.Map.Entry::getKey)
                    .orElse("");
            result.setBestVersion(bestVersion);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("版本比较失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("版本比较失败"));
        }
    }

    /**
     * 版本比较请求
     */
    @lombok.Data
    public static class CompareVersionsRequest {
        private java.util.List<String> continuationVersions;
    }

    /**
     * 版本比较结果
     */
    @lombok.Data
    public static class ComparisonResult {
        private java.util.Map<String, Integer> versionScores;
        private String bestVersion;
    }
}
