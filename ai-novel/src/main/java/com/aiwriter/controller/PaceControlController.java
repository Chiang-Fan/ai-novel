package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.PaceControlFeedbackService;
import com.aiwriter.service.SceneRhythmAnalysisService;
import com.aiwriter.service.SceneWordCountManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 节奏控制API控制器
 * 
 * 功能模块:
 * 1. /pace-control/word-count/* - 字数控制
 * 2. /pace-control/rhythm/* - 节奏分析
 * 3. /pace-control/feedback/* - 实时反馈
 */
@Slf4j
@RestController
@RequestMapping("/api/pace-control")
@RequiredArgsConstructor
@Tag(name = "PaceControl", description = "节奏控制管理API")
public class PaceControlController {
    
    private final SceneWordCountManagementService wordCountService;
    private final SceneRhythmAnalysisService rhythmService;
    private final PaceControlFeedbackService feedbackService;
    
    // ==================== 字数控制模块 ====================
    
    @GetMapping("/word-count/scene/{sceneId}")
    @Operation(summary = "获取场景字数分析")
    public ResponseEntity<ApiResponse<SceneWordCountAnalysis>> getSceneWordCountAnalysis(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long sceneId) {
        log.info("获取场景字数分析: sceneId={}", sceneId);
        SceneWordCountAnalysis analysis = wordCountService.getSceneWordCountAnalysis(sceneId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", analysis));
    }
    
    @GetMapping("/word-count/novel/{novelId}")
    @Operation(summary = "获取小说所有场景的字数分析")
    public ResponseEntity<ApiResponse<List<SceneWordCountAnalysis>>> getNovelSceneWordAnalysis(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        log.info("获取小说场景字数分析: novelId={}", novelId);
        List<SceneWordCountAnalysis> analyses = wordCountService.getNovelSceneWordAnalysis(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", analyses));
    }
    
    @PostMapping("/word-count/target")
    @Operation(summary = "设置场景字数目标")
    public ResponseEntity<ApiResponse<SceneWordCountTarget>> setSceneWordCountTarget(
            @RequestParam Long sceneId,
            @RequestParam Integer minWords,
            @RequestParam Integer maxWords) {
        log.info("设置场景字数目标: sceneId={}, min={}, max={}", sceneId, minWords, maxWords);
        SceneWordCountTarget target = wordCountService.setSceneWordCountTarget(sceneId, minWords, maxWords);
        return ResponseEntity.ok(ApiResponse.success("设置成功", target));
    }
    
    @PostMapping("/word-count/validate")
    @Operation(summary = "验证章节字数是否符合目标")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateChapterWordCount(
            @RequestParam Long sceneId,
            @RequestParam Long chapterId,
            @RequestParam Integer targetMinWords,
            @RequestParam Integer targetMaxWords) {
        log.info("验证章节字数: sceneId={}, chapterId={}", sceneId, chapterId);
        Map<String, Object> result = wordCountService.validateChapterWordCount(
            sceneId, chapterId, targetMinWords, targetMaxWords);
        return ResponseEntity.ok(ApiResponse.success("验证成功", result));
    }
    
    @GetMapping("/word-count/summary/{novelId}")
    @Operation(summary = "获取小说字数统计汇总")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNovelWordCountSummary(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        log.info("获取小说字数统计: novelId={}", novelId);
        Map<String, Object> summary = wordCountService.getNovelWordCountSummary(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", summary));
    }
    
    @GetMapping("/word-count/optimization-suggestions/{novelId}")
    @Operation(summary = "获取字数优化建议")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWordCountOptimizationSuggestions(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        log.info("获取字数优化建议: novelId={}", novelId);
        List<Map<String, Object>> suggestions = wordCountService.getWordCountOptimizationSuggestions(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", suggestions));
    }
    
    // ==================== 节奏分析模块 ====================
    
    @GetMapping("/rhythm/scene/{sceneId}")
    @Operation(summary = "分析单个场景的节奏")
    public ResponseEntity<ApiResponse<SceneRhythmAnalysis>> analyzeSceneRhythm(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long sceneId) {
        log.info("分析场景节奏: sceneId={}", sceneId);
        SceneRhythmAnalysis analysis = rhythmService.analyzeSceneRhythm(sceneId);
        return ResponseEntity.ok(ApiResponse.success("分析成功", analysis));
    }
    
    @GetMapping("/rhythm/novel/{novelId}")
    @Operation(summary = "获取小说整体节奏分析")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeNovelRhythm(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        log.info("分析小说节奏: novelId={}", novelId);
        Map<String, Object> analysis = rhythmService.analyzeNovelRhythm(novelId);
        return ResponseEntity.ok(ApiResponse.success("分析成功", analysis));
    }
    
    @GetMapping("/rhythm/recommendations/{novelId}")
    @Operation(summary = "获取节奏优化建议")
    public ResponseEntity<ApiResponse<List<RhythmRecommendation>>> getRhythmRecommendations(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        log.info("获取节奏优化建议: novelId={}", novelId);
        List<RhythmRecommendation> recommendations = rhythmService.getRhythmRecommendations(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", recommendations));
    }
    
    // ==================== 实时反馈模块 ====================
    
    @GetMapping("/feedback/chapter/{chapterId}")
    @Operation(summary = "获取章节的实时反馈")
    public ResponseEntity<ApiResponse<PaceFeedback>> getChapterFeedback(
            @Parameter(description = "章节ID", required = true)
            @PathVariable Long chapterId,
            @RequestParam Long novelId) {
        log.info("获取章节反馈: chapterId={}, novelId={}", chapterId, novelId);
        PaceFeedback feedback = feedbackService.getChapterFeedback(chapterId, novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", feedback));
    }
    
    @GetMapping("/feedback/novel/{novelId}")
    @Operation(summary = "获取小说整体反馈")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNovelPaceFeedback(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        log.info("获取小说反馈: novelId={}", novelId);
        Map<String, Object> feedback = feedbackService.getNovelPaceFeedback(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", feedback));
    }
    
    @PostMapping("/feedback/report")
    @Operation(summary = "生成节奏控制报告")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generatePaceControlReport(
            @RequestParam Long novelId) {
        log.info("生成节奏控制报告: novelId={}", novelId);
        Map<String, Object> report = feedbackService.generatePaceControlReport(novelId);
        return ResponseEntity.ok(ApiResponse.success("生成成功", report));
    }
    
    // ==================== 综合模块 ====================
    
    @GetMapping("/comprehensive/{novelId}")
    @Operation(summary = "获取综合节奏控制分析")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComprehensiveAnalysis(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        log.info("获取综合分析: novelId={}", novelId);
        
        Map<String, Object> comprehensive = new java.util.HashMap<>();
        comprehensive.put("wordCountSummary", wordCountService.getNovelWordCountSummary(novelId));
        comprehensive.put("rhythmAnalysis", rhythmService.analyzeNovelRhythm(novelId));
        comprehensive.put("novelFeedback", feedbackService.getNovelPaceFeedback(novelId));
        
        return ResponseEntity.ok(ApiResponse.success("获取成功", comprehensive));
    }
}
