package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.entity.Scene;
import com.aiwriter.service.SceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 场景管理Controller（重构版）
 * 
 * 合并了原有的 SceneController 和 SceneManagementController
 * 提供场景的完整生命周期管理功能
 * 
 * @version 2.0
 * @since 2025-12-31
 * @changelog
 *   - 2025-12-31: 合并SceneManagementController，统一路径为/api/scenes
 *   - 2025-12-31: 新增使用记录、变化历史、统计分析功能
 *   - 2025-12-31: 统一响应格式为ApiResponse
 */
@Slf4j
@RestController
@RequestMapping(value = {"/api/scenes", "/api/scene-management"}) // 多路径支持，保持向后兼容
@RequiredArgsConstructor
@Tag(name = "场景管理", description = "场景CRUD、AI推荐、使用跟踪、变化历史")
public class SceneController {
    
    private final SceneService sceneService;
    
    // ========================================
    // 基础CRUD操作
    // ========================================
    
    /**
     * 获取小说的所有场景
     * 
     * @param novelId 小说ID
     * @param type 场景类型过滤（可选）：interior/exterior/fantasy/realistic
     * @param keyword 关键词搜索（可选）：搜索场景名称和描述
     * @return 场景列表
     */
    @Operation(summary = "获取小说场景列表", description = "支持按类型过滤和关键词搜索")
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<SceneResponse>> getScenesByNovel(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId,
            
            @Parameter(description = "场景类型：interior/exterior/fantasy/realistic")
            @RequestParam(required = false) String type,
            
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword) {
        
        log.info("获取小说 {} 的场景列表，类型={}, 关键词={}", novelId, type, keyword);
        List<SceneResponse> scenes = sceneService.getScenesByNovel(novelId, type, keyword);
        return ApiResponse.success(scenes);
    }
    
    /**
     * 获取场景详情
     */
    @Operation(summary = "获取场景详情")
    @GetMapping("/{id}")
    public ApiResponse<SceneResponse> getScene(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取场景详情: id={}", id);
        SceneResponse scene = sceneService.getScene(id);
        return ApiResponse.success(scene);
    }
    
    /**
     * 创建场景
     */
    @Operation(summary = "创建场景", description = "创建新的场景，支持详细属性设置")
    @PostMapping
    public ApiResponse<SceneResponse> createScene(
            @Valid @RequestBody CreateSceneRequest request) {
        
        log.info("创建场景: name={}, type={}", request.getName(), request.getType());
        SceneResponse created = sceneService.createScene(request);
        return ApiResponse.success("创建成功", created);
    }
    
    /**
     * 更新场景
     */
    @Operation(summary = "更新场景")
    @PutMapping("/{id}")
    public ApiResponse<SceneResponse> updateScene(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateSceneRequest request) {
        
        log.info("更新场景: id={}", id);
        SceneResponse updated = sceneService.updateScene(id, request);
        return ApiResponse.success("更新成功", updated);
    }
    
    /**
     * 删除场景
     */
    @Operation(summary = "删除场景", description = "删除场景及其关联的使用记录和变化历史")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteScene(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id) {
        
        log.info("删除场景: id={}", id);
        sceneService.deleteScene(id);
        return ApiResponse.success("删除成功", null);
    }
    
    // ========================================
    // AI推荐功能
    // ========================================
    
    /**
     * AI推荐场景
     * 
     * 基于当前情节、角色、氛围等因素，智能推荐适合的场景
     */
    @Operation(summary = "AI推荐场景", description = "基于情节和氛围智能推荐场景")
    @PostMapping("/recommend")
    public ApiResponse<List<SceneRecommendationResponse>> recommendScenes(
            @Valid @RequestBody SceneRecommendationRequest request) {
        
        log.info("AI推荐场景: novelId={}, context={}", 
                request.getNovelId(), request.getContext());
        
        List<SceneRecommendationResponse> recommendations = 
            sceneService.recommendScenes(request);
        
        return ApiResponse.success(recommendations);
    }
    
    // ========================================
    // 使用记录管理
    // ========================================
    
    /**
     * 记录场景使用
     * 
     * 当场景在章节中被使用时，记录使用信息
     * 用于跟踪场景的出现频率和使用情况
     */
    @Operation(summary = "记录场景使用", description = "记录场景在章节中的使用")
    @PostMapping("/{id}/usages")
    public ApiResponse<SceneUsageResponse> recordSceneUsage(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CreateSceneUsageRequest request) {
        
        log.info("记录场景使用: sceneId={}, chapterId={}", id, request.getChapterId());
        
        // 确保请求中的sceneId与路径参数一致
        request.setSceneId(id);
        
        SceneUsageResponse usage = sceneService.recordSceneUsage(request);
        return ApiResponse.success("记录成功", usage);
    }
    
    /**
     * 获取场景使用历史
     * 
     * 查看场景在哪些章节中被使用过
     */
    @Operation(summary = "获取场景使用历史")
    @GetMapping("/{id}/usages")
    public ApiResponse<List<SceneUsageResponse>> getSceneUsageHistory(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取场景使用历史: sceneId={}", id);
        List<SceneUsageResponse> usages = sceneService.getSceneUsageHistory(id);
        return ApiResponse.success(usages);
    }
    
    /**
     * 删除场景使用记录
     */
    @Operation(summary = "删除场景使用记录")
    @DeleteMapping("/{id}/usages/{usageId}")
    public ApiResponse<Void> deleteSceneUsage(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "使用记录ID", required = true)
            @PathVariable Long usageId) {
        
        log.info("删除场景使用记录: sceneId={}, usageId={}", id, usageId);
        sceneService.deleteSceneUsage(usageId);
        return ApiResponse.success("删除成功", null);
    }
    
    // ========================================
    // 变化历史管理
    // ========================================
    
    /**
     * 记录场景变化
     * 
     * 当场景的重要属性发生变化时（如环境、氛围、时间等），
     * 记录变化信息，用于跟踪场景的演变
     */
    @Operation(summary = "记录场景变化", description = "记录场景随情节发展的变化")
    @PostMapping("/{id}/changes")
    public ApiResponse<SceneChangeResponse> recordSceneChange(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CreateSceneChangeRequest request) {
        
        log.info("记录场景变化: sceneId={}, changeType={}", id, request.getChangeType());
        
        // 确保请求中的sceneId与路径参数一致
        request.setSceneId(id);
        
        SceneChangeResponse change = sceneService.recordSceneChange(request);
        return ApiResponse.success("记录成功", change);
    }
    
    /**
     * 获取场景变化历史
     * 
     * 查看场景的演变过程
     */
    @Operation(summary = "获取场景变化历史")
    @GetMapping("/{id}/changes")
    public ApiResponse<List<SceneChangeResponse>> getSceneChangeHistory(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取场景变化历史: sceneId={}", id);
        List<SceneChangeResponse> changes = sceneService.getSceneChangeHistory(id);
        return ApiResponse.success(changes);
    }
    
    /**
     * 删除场景变化记录
     */
    @Operation(summary = "删除场景变化记录")
    @DeleteMapping("/{id}/changes/{changeId}")
    public ApiResponse<Void> deleteSceneChange(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "变化记录ID", required = true)
            @PathVariable Long changeId) {
        
        log.info("删除场景变化记录: sceneId={}, changeId={}", id, changeId);
        sceneService.deleteSceneChange(changeId);
        return ApiResponse.success("删除成功", null);
    }
    
    // ========================================
    // 统计分析
    // ========================================
    
    /**
     * 获取场景统计信息
     * 
     * 包括：
     * - 使用频率
     * - 出现章节数
     * - 关联角色数
     * - 变化次数
     * - 平均停留时长等
     */
    @Operation(summary = "获取场景统计", description = "获取场景的使用频率、变化趋势等统计数据")
    @GetMapping("/{id}/statistics")
    public ApiResponse<SceneStatisticsResponse> getSceneStatistics(
            @Parameter(description = "场景ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取场景统计: sceneId={}", id);
        SceneStatisticsResponse statistics = sceneService.getSceneStatistics(id);
        return ApiResponse.success(statistics);
    }
    
    /**
     * 批量获取场景统计
     * 
     * 用于分析小说中所有场景的整体使用情况
     */
    @Operation(summary = "批量获取场景统计")
    @GetMapping("/novel/{novelId}/statistics")
    public ApiResponse<List<SceneStatisticsResponse>> getBatchStatistics(
            @Parameter(description = "小说ID", required = true)
            @PathVariable Long novelId) {
        
        log.info("批量获取场景统计: novelId={}", novelId);
        List<SceneStatisticsResponse> statistics = 
            sceneService.getBatchStatistics(novelId);
        return ApiResponse.success(statistics);
    }
}
