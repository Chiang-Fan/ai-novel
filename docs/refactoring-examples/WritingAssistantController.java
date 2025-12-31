package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.WritingAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI写作助手Controller（重构版）
 * 
 * 合并了原有的：
 * - ContinuationController (基础续写)
 * - EnhancedContinuationController (增强续写)
 * - ChapterController.continueChapter (章节续写)
 * 
 * 提供统一的AI写作辅助功能入口
 * 
 * @version 2.0
 * @since 2025-12-31
 * @changelog
 *   - 2025-12-31: 合并3个续写入口，统一为/api/writing-assistant
 *   - 2025-12-31: 支持基础/增强/自动模式切换
 *   - 2025-12-31: 新增约束管理和一致性验证
 */
@Slf4j
@RestController
@RequestMapping("/api/writing-assistant")
@RequiredArgsConstructor
@Tag(name = "AI写作助手", description = "智能续写、约束管理、写作建议的统一入口")
public class WritingAssistantController {
    
    private final WritingAssistantService assistantService;
    
    // ========================================
    // 智能续写
    // ========================================
    
    /**
     * 生成AI续写
     * 
     * 支持三种模式：
     * - basic: 基础续写，快速生成，适合初稿
     * - enhanced: 增强续写，多维约束，质量更高
     * - auto: 自动选择最优模式
     * 
     * @param request 续写请求
     * @return 续写结果，包含生成的文本、质量评分、建议等
     */
    @Operation(
        summary = "智能续写", 
        description = "根据上下文和约束条件，智能生成后续内容。支持基础/增强/自动模式"
    )
    @PostMapping("/continuation")
    public ApiResponse<ContinuationResponse> generateContinuation(
            @Valid @RequestBody ContinuationRequest request) {
        
        log.info("生成续写: chapterId={}, mode={}, wordCount={}", 
                request.getChapterId(), 
                request.getMode(), 
                request.getTargetWordCount());
        
        // 请求验证
        if (request.getMode() == null) {
            request.setMode("auto"); // 默认自动模式
        }
        
        ContinuationResponse response = assistantService.generateContinuation(request);
        
        log.info("续写完成: continuationId={}, actualWords={}, qualityScore={}", 
                response.getId(), 
                response.getGeneratedText().length(), 
                response.getQualityScore());
        
        return ApiResponse.success("续写生成成功", response);
    }
    
    /**
     * 快速续写（简化版）
     * 
     * 提供最简单的续写接口，只需提供章节ID
     * 系统自动选择最优参数
     */
    @Operation(summary = "快速续写", description = "最简续写接口，系统自动优化参数")
    @PostMapping("/continuation/quick")
    public ApiResponse<ContinuationResponse> quickContinue(
            @Parameter(description = "章节ID", required = true)
            @RequestParam Long chapterId,
            
            @Parameter(description = "目标字数，默认2000")
            @RequestParam(defaultValue = "2000") Integer wordCount) {
        
        log.info("快速续写: chapterId={}, wordCount={}", chapterId, wordCount);
        
        ContinuationRequest request = ContinuationRequest.builder()
            .chapterId(chapterId)
            .targetWordCount(wordCount)
            .mode("auto")
            .build();
        
        return ApiResponse.success(assistantService.generateContinuation(request));
    }
    
    /**
     * 批量续写
     * 
     * 一次性为多个章节生成续写
     * 适合快速填充剧情大纲
     */
    @Operation(summary = "批量续写", description = "为多个章节批量生成续写")
    @PostMapping("/continuation/batch")
    public ApiResponse<List<ContinuationResponse>> batchContinue(
            @Valid @RequestBody BatchContinuationRequest request) {
        
        log.info("批量续写: 章节数={}", request.getChapterIds().size());
        List<ContinuationResponse> responses = assistantService.batchContinue(request);
        return ApiResponse.success("批量续写完成", responses);
    }
    
    // ========================================
    // 续写历史
    // ========================================
    
    /**
     * 获取章节的续写历史
     * 
     * 查看该章节之前生成过的所有续写版本
     * 可以进行版本对比和回滚
     */
    @Operation(summary = "获取续写历史")
    @GetMapping("/continuation/chapter/{chapterId}")
    public ApiResponse<List<ContinuationResponse>> getContinuationHistory(
            @Parameter(description = "章节ID", required = true)
            @PathVariable Long chapterId,
            
            @Parameter(description = "模式过滤: basic/enhanced/all")
            @RequestParam(defaultValue = "all") String mode,
            
            @Parameter(description = "状态过滤: pending/applied/rejected")
            @RequestParam(required = false) String status) {
        
        log.info("获取续写历史: chapterId={}, mode={}, status={}", chapterId, mode, status);
        List<ContinuationResponse> history = 
            assistantService.getContinuationHistory(chapterId, mode, status);
        return ApiResponse.success(history);
    }
    
    /**
     * 获取续写详情
     */
    @Operation(summary = "获取续写详情")
    @GetMapping("/continuation/{id}")
    public ApiResponse<ContinuationResponse> getContinuationDetail(
            @Parameter(description = "续写ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取续写详情: id={}", id);
        return ApiResponse.success(assistantService.getContinuationDetail(id));
    }
    
    // ========================================
    // 续写应用
    // ========================================
    
    /**
     * 应用续写到章节
     * 
     * 将生成的续写内容添加到章节正文中
     * 支持直接替换或合并模式
     */
    @Operation(summary = "应用续写", description = "将续写内容添加到章节")
    @PostMapping("/continuation/{id}/apply")
    public ApiResponse<ApplyContinuationResult> applyContinuation(
            @Parameter(description = "续写ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "是否合并到现有内容")
            @RequestParam(defaultValue = "true") boolean merge,
            
            @Parameter(description = "应用前是否重新验证")
            @RequestParam(defaultValue = "false") boolean revalidate) {
        
        log.info("应用续写: id={}, merge={}, revalidate={}", id, merge, revalidate);
        
        ApplyContinuationResult result = assistantService.applyContinuation(id, merge, revalidate);
        
        return ApiResponse.success("续写已应用", result);
    }
    
    /**
     * 批量应用续写
     */
    @Operation(summary = "批量应用续写")
    @PostMapping("/continuation/apply/batch")
    public ApiResponse<BatchApplyResult> batchApply(
            @RequestBody BatchApplyRequest request) {
        
        log.info("批量应用续写: 数量={}", request.getContinuationIds().size());
        BatchApplyResult result = assistantService.batchApply(request);
        return ApiResponse.success(result);
    }
    
    // ========================================
    // 续写评分
    // ========================================
    
    /**
     * 对续写进行评分
     * 
     * 用户可以对生成的续写打分和反馈
     * 系统会根据反馈优化后续生成
     */
    @Operation(summary = "续写评分", description = "对生成的续写进行质量评分和反馈")
    @PostMapping("/continuation/{id}/rate")
    public ApiResponse<Void> rateContinuation(
            @Parameter(description = "续写ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request) {
        
        log.info("续写评分: id={}, rating={}", id, request.getRating());
        
        assistantService.rateContinuation(id, request.getRating(), request.getFeedback());
        
        return ApiResponse.success("评分已提交", null);
    }
    
    // ========================================
    // 约束管理
    // ========================================
    
    /**
     * 获取约束预设
     * 
     * 系统提供多种常见的续写约束模板
     * 用户可以直接使用或在此基础上修改
     */
    @Operation(summary = "获取约束预设", description = "获取系统预定义的续写约束模板")
    @GetMapping("/constraints/presets")
    public ApiResponse<List<ConstraintPreset>> getConstraintPresets(
            @Parameter(description = "约束类型: plot/character/style/rhythm")
            @RequestParam(required = false) String category) {
        
        log.info("获取约束预设: category={}", category);
        List<ConstraintPreset> presets = assistantService.getConstraintPresets(category);
        return ApiResponse.success(presets);
    }
    
    /**
     * 验证约束条件
     * 
     * 在续写前，验证设定的约束条件是否合理
     * 避免冲突的约束导致无法生成
     */
    @Operation(summary = "验证约束", description = "检查约束条件的合理性和一致性")
    @PostMapping("/constraints/validate")
    public ApiResponse<ConstraintValidationResult> validateConstraints(
            @Valid @RequestBody ConstraintValidationRequest request) {
        
        log.info("验证约束: 约束数量={}", request.getConstraints().size());
        
        ConstraintValidationResult result = assistantService.validateConstraints(request);
        
        if (!result.isValid()) {
            log.warn("约束验证失败: {}", result.getErrors());
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 保存自定义约束
     */
    @Operation(summary = "保存自定义约束")
    @PostMapping("/constraints/custom")
    public ApiResponse<ConstraintPreset> saveCustomConstraint(
            @Valid @RequestBody SaveConstraintRequest request) {
        
        log.info("保存自定义约束: name={}", request.getName());
        ConstraintPreset saved = assistantService.saveCustomConstraint(request);
        return ApiResponse.success("保存成功", saved);
    }
    
    // ========================================
    // 续写分析
    // ========================================
    
    /**
     * 分析续写质量
     * 
     * 深度分析生成的续写：
     * - 连贯性
     * - 角色一致性
     * - 情节合理性
     * - 风格匹配度
     * - 语言质量
     */
    @Operation(summary = "续写质量分析", description = "深度分析续写的各维度质量")
    @PostMapping("/continuation/{id}/analyze")
    public ApiResponse<ContinuationAnalysisResult> analyzeContinuation(
            @Parameter(description = "续写ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "分析维度，逗号分隔")
            @RequestParam(required = false) String dimensions) {
        
        log.info("分析续写: id={}, dimensions={}", id, dimensions);
        
        ContinuationAnalysisResult result = assistantService.analyzeContinuation(id, dimensions);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 比较多个续写版本
     * 
     * 对比不同续写的优劣，帮助选择最佳版本
     */
    @Operation(summary = "续写对比", description = "对比多个续写版本的质量差异")
    @PostMapping("/continuation/compare")
    public ApiResponse<ContinuationComparisonResult> compareContinuations(
            @Valid @RequestBody ComparisonRequest request) {
        
        log.info("对比续写: 数量={}", request.getContinuationIds().size());
        
        ContinuationComparisonResult result = 
            assistantService.compareContinuations(request.getContinuationIds());
        
        return ApiResponse.success(result);
    }
    
    // ========================================
    // 写作建议
    // ========================================
    
    /**
     * 生成写作建议
     * 
     * 基于当前章节内容，提供写作方向建议
     * 包括情节发展、角色动作、场景转换等
     */
    @Operation(summary = "生成写作建议", description = "智能分析并提供写作方向建议")
    @PostMapping("/suggestions")
    public ApiResponse<List<WritingSuggestion>> generateSuggestions(
            @Valid @RequestBody SuggestionRequest request) {
        
        log.info("生成写作建议: chapterId={}, type={}", 
                request.getChapterId(), request.getSuggestionType());
        
        List<WritingSuggestion> suggestions = 
            assistantService.generateSuggestions(request);
        
        return ApiResponse.success("建议生成成功", suggestions);
    }
    
    /**
     * 获取章节建议历史
     */
    @Operation(summary = "获取章节建议")
    @GetMapping("/suggestions/chapter/{chapterId}")
    public ApiResponse<List<WritingSuggestion>> getChapterSuggestions(
            @Parameter(description = "章节ID", required = true)
            @PathVariable Long chapterId,
            
            @Parameter(description = "建议类别: plot/character/scene/style")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "状态: pending/accepted/rejected")
            @RequestParam(required = false) String status) {
        
        log.info("获取章节建议: chapterId={}, category={}, status={}", 
                chapterId, category, status);
        
        List<WritingSuggestion> suggestions = 
            assistantService.getChapterSuggestions(chapterId, category, status);
        
        return ApiResponse.success(suggestions);
    }
    
    /**
     * 更新建议状态
     */
    @Operation(summary = "更新建议状态", description = "接受或拒绝写作建议")
    @PutMapping("/suggestions/{id}/status")
    public ApiResponse<Void> updateSuggestionStatus(
            @Parameter(description = "建议ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        
        log.info("更新建议状态: id={}, status={}", id, request.getStatus());
        
        assistantService.updateSuggestionStatus(id, request.getStatus());
        
        return ApiResponse.success("状态已更新", null);
    }
    
    /**
     * 应用建议到正文
     */
    @Operation(summary = "应用建议", description = "将建议的内容应用到章节")
    @PostMapping("/suggestions/{id}/apply")
    public ApiResponse<Void> applySuggestion(
            @Parameter(description = "建议ID", required = true)
            @PathVariable Long id) {
        
        log.info("应用建议: id={}", id);
        assistantService.applySuggestion(id);
        return ApiResponse.success("建议已应用", null);
    }
    
    // ========================================
    // 统计分析
    // ========================================
    
    /**
     * 获取写作助手使用统计
     */
    @Operation(summary = "使用统计")
    @GetMapping("/statistics/novel/{novelId}")
    public ApiResponse<AssistantStatistics> getStatistics(
            @PathVariable Long novelId) {
        
        log.info("获取写作助手统计: novelId={}", novelId);
        AssistantStatistics statistics = assistantService.getStatistics(novelId);
        return ApiResponse.success(statistics);
    }
}
