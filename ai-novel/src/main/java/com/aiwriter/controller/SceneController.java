package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.SceneRecommendationRequest;
import com.aiwriter.dto.SceneRecommendationResponse;
import com.aiwriter.entity.Scene;
import com.aiwriter.service.SceneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
public class SceneController {
    
    private final SceneService sceneService;
    
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<Scene>> getScenesByNovel(@PathVariable Long novelId) {
        List<Scene> scenes = sceneService.getScenesByNovel(novelId);
        return ApiResponse.success(scenes);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Scene> getScene(@PathVariable Long id) {
        Scene scene = sceneService.getSceneById(id);
        return ApiResponse.success(scene);
    }
    
    @PostMapping
    public ApiResponse<Scene> createScene(@Valid @RequestBody Scene scene) {
        Scene created = sceneService.createScene(scene);
        return ApiResponse.success(created);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Scene> updateScene(@PathVariable Long id, 
                                          @Valid @RequestBody Scene scene) {
        Scene updated = sceneService.updateScene(id, scene);
        return ApiResponse.success(updated);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/recommend")
    public ApiResponse<List<SceneRecommendationResponse>> recommendScenes(
            @Valid @RequestBody SceneRecommendationRequest request) {
        List<SceneRecommendationResponse> recommendations = 
            sceneService.recommendScenes(request);
        return ApiResponse.success(recommendations);
    }
}
