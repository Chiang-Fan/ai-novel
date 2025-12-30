package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.SmartContinuationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能续写控制器
 */
@RestController
@RequestMapping("/api/continuation")
@RequiredArgsConstructor
public class ContinuationController {
    
    private final SmartContinuationService continuationService;
    
    /**
     * 生成续写
     */
    @PostMapping("/generate")
    public ResponseEntity<ContinuationResponse> generateContinuation(
            @RequestBody ContinuationRequest request) {
        return ResponseEntity.ok(continuationService.generateContinuation(request));
    }
    
    /**
     * 获取章节的所有续写
     */
    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<List<ContinuationResponse>> getChapterContinuations(
            @PathVariable Long chapterId) {
        return ResponseEntity.ok(continuationService.getChapterContinuations(chapterId));
    }
    
    /**
     * 应用续写
     */
    @PutMapping("/{continuationId}/apply")
    public ResponseEntity<Void> applyContinuation(@PathVariable Long continuationId) {
        continuationService.applyContinuation(continuationId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 评分续写
     */
    @PutMapping("/{continuationId}/rate")
    public ResponseEntity<Void> rateContinuation(
            @PathVariable Long continuationId,
            @RequestBody Map<String, Integer> request) {
        continuationService.rateContinuation(continuationId, request.get("rating"));
        return ResponseEntity.ok().build();
    }
    
    /**
     * 生成写作建议
     */
    @PostMapping("/suggestions")
    public ResponseEntity<List<SuggestionResponse>> generateSuggestions(
            @RequestBody SuggestionRequest request) {
        return ResponseEntity.ok(continuationService.generateSuggestions(request));
    }
    
    /**
     * 获取章节建议
     */
    @GetMapping("/suggestions/chapter/{chapterId}")
    public ResponseEntity<List<SuggestionResponse>> getChapterSuggestions(
            @PathVariable Long chapterId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(continuationService.getChapterSuggestions(chapterId, status));
    }
    
    /**
     * 更新建议状态
     */
    @PutMapping("/suggestions/{suggestionId}/status")
    public ResponseEntity<Void> updateSuggestionStatus(
            @PathVariable Long suggestionId,
            @RequestBody Map<String, String> request) {
        continuationService.updateSuggestionStatus(suggestionId, request.get("status"));
        return ResponseEntity.ok().build();
    }
}
