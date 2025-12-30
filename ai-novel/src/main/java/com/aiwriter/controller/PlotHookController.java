package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.entity.PlotHook;
import com.aiwriter.service.PlotHookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 伏笔管理控制器
 * 提供伏笔的 CRUD、状态管理、AI 检测等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/plot-hooks")
@Tag(name = "伏笔管理", description = "伏笔埋设、触发、解决的完整生命周期管理")
public class PlotHookController {
    
    @Autowired
    private PlotHookService plotHookService;
    
    /**
     * 获取小说的所有伏笔
     */
    @GetMapping("/novel/{novelId}")
    @Operation(summary = "获取小说的所有伏笔", description = "返回指定小说的所有伏笔，按埋设章节排序")
    public ResponseEntity<ApiResponse<List<PlotHookDto>>> getHooksByNovelId(@PathVariable Long novelId) {
        log.info("获取小说 {} 的所有伏笔", novelId);
        List<PlotHookDto> hooks = plotHookService.getHooksByNovelId(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", hooks));
    }
    
    /**
     * 获取待触发的伏笔
     */
    @GetMapping("/novel/{novelId}/pending")
    @Operation(summary = "获取待触发的伏笔", description = "返回状态为 PENDING 或 HINTED 的伏笔")
    public ResponseEntity<ApiResponse<List<PlotHookDto>>> getPendingHooks(@PathVariable Long novelId) {
        log.info("获取小说 {} 的待触发伏笔", novelId);
        List<PlotHookDto> hooks = plotHookService.getPendingHooks(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", hooks));
    }
    
    /**
     * 获取超期未触发的伏笔
     */
    @GetMapping("/novel/{novelId}/overdue")
    @Operation(summary = "获取超期伏笔", description = "返回超过预期章节仍未触发的伏笔")
    public ResponseEntity<ApiResponse<List<PlotHookDto>>> getOverdueHooks(@PathVariable Long novelId) {
        log.info("获取小说 {} 的超期伏笔", novelId);
        List<PlotHookDto> hooks = plotHookService.getOverdueHooks(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", hooks));
    }
    
    /**
     * 按状态获取伏笔
     */
    @GetMapping("/novel/{novelId}/status/{status}")
    @Operation(summary = "按状态获取伏笔", description = "返回指定状态的伏笔")
    public ResponseEntity<ApiResponse<List<PlotHookDto>>> getHooksByStatus(
            @PathVariable Long novelId,
            @PathVariable PlotHook.Status status) {
        log.info("获取小说 {} 状态为 {} 的伏笔", novelId, status);
        List<PlotHookDto> hooks = plotHookService.getHooksByStatus(novelId, status);
        return ResponseEntity.ok(ApiResponse.success("获取成功", hooks));
    }
    
    /**
     * AI 自动检测章节中的伏笔
     */
    @PostMapping("/detect/{novelId}/{chapterNumber}")
    @Operation(summary = "AI 自动检测伏笔", description = "使用 AI 分析章节内容，自动识别潜在伏笔")
    public ResponseEntity<ApiResponse<List<PlotHookDto>>> detectHooks(
            @PathVariable Long novelId,
            @PathVariable Integer chapterNumber) {
        log.info("开始检测小说 {} 第 {} 章的伏笔", novelId, chapterNumber);
        List<PlotHookDto> hooks = plotHookService.detectPlotHooksByChapterNumber(novelId, chapterNumber);
        return ResponseEntity.ok(ApiResponse.success("检测成功", hooks));
    }
    
    /**
     * 创建伏笔
     */
    @PostMapping
    @Operation(summary = "创建伏笔", description = "手动创建一个新的伏笔")
    public ResponseEntity<ApiResponse<PlotHookDto>> createHook(@RequestBody CreatePlotHookRequest request) {
        log.info("创建伏笔: {}", request.getTitle());
        PlotHookDto hook = plotHookService.createHook(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", hook));
    }
    
    /**
     * 更新伏笔
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新伏笔", description = "更新伏笔的基本信息")
    public ResponseEntity<ApiResponse<PlotHookDto>> updateHook(
            @PathVariable Long id,
            @RequestBody UpdatePlotHookRequest request) {
        log.info("更新伏笔: {}", id);
        PlotHookDto hook = plotHookService.updateHook(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", hook));
    }
    
    /**
     * 标记伏笔为已触发
     */
    @PutMapping("/{id}/trigger")
    @Operation(summary = "触发伏笔", description = "标记伏笔已在某章节触发")
    public ResponseEntity<ApiResponse<PlotHookDto>> triggerHook(
            @PathVariable Long id,
            @RequestParam Integer chapterNumber) {
        log.info("触发伏笔 {} 于第 {} 章", id, chapterNumber);
        PlotHookDto hook = plotHookService.triggerHook(id, chapterNumber);
        return ResponseEntity.ok(ApiResponse.success("触发成功", hook));
    }
    
    /**
     * 标记伏笔为已解决
     */
    @PutMapping("/{id}/resolve")
    @Operation(summary = "解决伏笔", description = "标记伏笔已在某章节完全解决")
    public ResponseEntity<ApiResponse<PlotHookDto>> resolveHook(
            @PathVariable Long id,
            @RequestParam Integer chapterNumber,
            @RequestParam(required = false) String resolutionNote) {
        log.info("解决伏笔 {} 于第 {} 章", id, chapterNumber);
        PlotHookDto hook = plotHookService.resolveHook(id, chapterNumber, resolutionNote);
        return ResponseEntity.ok(ApiResponse.success("解决成功", hook));
    }
    
    /**
     * 删除伏笔
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除伏笔", description = "删除指定的伏笔")
    public ResponseEntity<ApiResponse<Void>> deleteHook(@PathVariable Long id) {
        log.info("删除伏笔: {}", id);
        plotHookService.deleteHook(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    /**
     * 获取伏笔统计信息
     */
    @GetMapping("/novel/{novelId}/statistics")
    @Operation(summary = "获取伏笔统计", description = "返回伏笔的统计信息")
    public ResponseEntity<ApiResponse<PlotHookStatistics>> getStatistics(@PathVariable Long novelId) {
        log.info("获取小说 {} 的伏笔统计", novelId);
        PlotHookStatistics stats = plotHookService.getStatistics(novelId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", stats));
    }
}
