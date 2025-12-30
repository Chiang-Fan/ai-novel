package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.SceneManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scene-management")
@RequiredArgsConstructor
public class SceneManagementController {
    
    private final SceneManagementService sceneService;
    
    /**
     * 创建场景
     */
    @PostMapping
    public ResponseEntity<SceneResponse> createScene(@RequestBody CreateSceneRequest request) {
        return ResponseEntity.ok(sceneService.createScene(request));
    }
    
    /**
     * 更新场景
     */
    @PutMapping("/{sceneId}")
    public ResponseEntity<SceneResponse> updateScene(
            @PathVariable Long sceneId,
            @RequestBody UpdateSceneRequest request) {
        return ResponseEntity.ok(sceneService.updateScene(sceneId, request));
    }
    
    /**
     * 删除场景
     */
    @DeleteMapping("/{sceneId}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long sceneId) {
        sceneService.deleteScene(sceneId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取场景详情
     */
    @GetMapping("/{sceneId}")
    public ResponseEntity<SceneResponse> getScene(@PathVariable Long sceneId) {
        return ResponseEntity.ok(sceneService.getScene(sceneId));
    }
    
    /**
     * 获取小说的所有场景
     */
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<List<SceneResponse>> getScenesByNovel(
            @PathVariable Long novelId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(sceneService.getScenesByNovel(novelId, type, keyword));
    }
    
    /**
     * 记录场景使用
     */
    @PostMapping("/usages")
    public ResponseEntity<SceneUsageResponse> recordSceneUsage(
            @RequestBody CreateSceneUsageRequest request) {
        return ResponseEntity.ok(sceneService.recordSceneUsage(request));
    }
    
    /**
     * 获取场景使用历史
     */
    @GetMapping("/{sceneId}/usages")
    public ResponseEntity<List<SceneUsageResponse>> getSceneUsageHistory(
            @PathVariable Long sceneId) {
        return ResponseEntity.ok(sceneService.getSceneUsageHistory(sceneId));
    }
    
    /**
     * 删除场景使用记录
     */
    @DeleteMapping("/usages/{usageId}")
    public ResponseEntity<Void> deleteSceneUsage(@PathVariable Long usageId) {
        sceneService.deleteSceneUsage(usageId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 记录场景变化
     */
    @PostMapping("/changes")
    public ResponseEntity<SceneChangeResponse> recordSceneChange(
            @RequestBody CreateSceneChangeRequest request) {
        return ResponseEntity.ok(sceneService.recordSceneChange(request));
    }
    
    /**
     * 获取场景变化历史
     */
    @GetMapping("/{sceneId}/changes")
    public ResponseEntity<List<SceneChangeResponse>> getSceneChangeHistory(
            @PathVariable Long sceneId) {
        return ResponseEntity.ok(sceneService.getSceneChangeHistory(sceneId));
    }
    
    /**
     * 删除场景变化记录
     */
    @DeleteMapping("/changes/{changeId}")
    public ResponseEntity<Void> deleteSceneChange(@PathVariable Long changeId) {
        sceneService.deleteSceneChange(changeId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取场景统计
     */
    @GetMapping("/{sceneId}/statistics")
    public ResponseEntity<SceneStatisticsResponse> getSceneStatistics(
            @PathVariable Long sceneId) {
        return ResponseEntity.ok(sceneService.getSceneStatistics(sceneId));
    }
}
