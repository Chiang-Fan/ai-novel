package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.AtmosphereGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 氛围生成控制器
 */
@RestController
@RequestMapping("/api/atmosphere")
@RequiredArgsConstructor
public class AtmosphereController {
    
    private final AtmosphereGenerationService atmosphereService;
    
    /**
     * 生成场景氛围
     */
    @PostMapping("/generate")
    public ResponseEntity<SceneAtmosphereResponse> generateAtmosphere(
            @RequestBody AtmosphereGenerateRequest request) {
        return ResponseEntity.ok(atmosphereService.generateAtmosphere(request));
    }
    
    /**
     * 获取场景的所有氛围
     */
    @GetMapping("/scene/{sceneId}")
    public ResponseEntity<List<SceneAtmosphereResponse>> getSceneAtmospheres(
            @PathVariable Long sceneId) {
        return ResponseEntity.ok(atmosphereService.getSceneAtmospheres(sceneId));
    }
    
    /**
     * 应用氛围
     */
    @PutMapping("/{atmosphereId}/apply")
    public ResponseEntity<Void> applyAtmosphere(@PathVariable Long atmosphereId) {
        atmosphereService.applyAtmosphere(atmosphereId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 评分氛围
     */
    @PutMapping("/{atmosphereId}/rate")
    public ResponseEntity<Void> rateAtmosphere(
            @PathVariable Long atmosphereId,
            @RequestBody Map<String, Integer> request) {
        atmosphereService.rateAtmosphere(atmosphereId, request.get("rating"));
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取氛围匹配建议
     */
    @PostMapping("/match")
    public ResponseEntity<AtmosphereMatchResponse> getAtmosphereMatch(
            @RequestParam Long sceneId,
            @RequestParam(required = false) String plotContext) {
        return ResponseEntity.ok(atmosphereService.getAtmosphereMatch(sceneId, plotContext));
    }
    
    /**
     * 创建氛围模板
     */
    @PostMapping("/templates")
    public ResponseEntity<AtmosphereTemplateResponse> createTemplate(
            @RequestBody AtmosphereTemplateRequest request) {
        return ResponseEntity.ok(atmosphereService.createTemplate(request));
    }
    
    /**
     * 获取所有模板
     */
    @GetMapping("/templates")
    public ResponseEntity<List<AtmosphereTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(atmosphereService.getAllTemplates());
    }
    
    /**
     * 按分类获取模板
     */
    @GetMapping("/templates/category/{category}")
    public ResponseEntity<List<AtmosphereTemplateResponse>> getTemplatesByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(atmosphereService.getTemplatesByCategory(category));
    }
    
    /**
     * 按氛围类型获取模板
     */
    @GetMapping("/templates/type/{atmosphereType}")
    public ResponseEntity<List<AtmosphereTemplateResponse>> getTemplatesByType(
            @PathVariable String atmosphereType) {
        return ResponseEntity.ok(atmosphereService.getTemplatesByType(atmosphereType));
    }
}
