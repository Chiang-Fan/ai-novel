package com.aiwriter.controller;

import com.aiwriter.dto.AutoSuggestionDto;
import com.aiwriter.dto.SuggestionStatistics;
import com.aiwriter.entity.AutoSuggestion;
import com.aiwriter.service.AutoSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能推荐 API
 */
@Tag(name = "AutoSuggestion", description = "智能推荐管理")
@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class AutoSuggestionController {

    private final AutoSuggestionService suggestionService;

    @Operation(summary = "为小说生成全面推荐")
    @PostMapping("/generate/novel/{novelId}")
    public ResponseEntity<List<AutoSuggestionDto>> generateForNovel(@PathVariable Long novelId) {
        return ResponseEntity.ok(suggestionService.generateSuggestionsForNovel(novelId));
    }

    @Operation(summary = "为章节生成推荐")
    @PostMapping("/generate/chapter/{chapterId}")
    public ResponseEntity<List<AutoSuggestionDto>> generateForChapter(
            @PathVariable Long chapterId,
            @RequestParam Long novelId) {
        return ResponseEntity.ok(suggestionService.generateSuggestionsForChapter(novelId, chapterId));
    }

    @Operation(summary = "获取小说的所有推荐")
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<List<AutoSuggestionDto>> getByNovel(@PathVariable Long novelId) {
        return ResponseEntity.ok(suggestionService.getSuggestionsByNovel(novelId));
    }

    @Operation(summary = "获取活跃推荐")
    @GetMapping("/novel/{novelId}/active")
    public ResponseEntity<List<AutoSuggestionDto>> getActive(@PathVariable Long novelId) {
        return ResponseEntity.ok(suggestionService.getActiveSuggestions(novelId));
    }

    @Operation(summary = "获取指定类型的推荐")
    @GetMapping("/novel/{novelId}/type/{type}")
    public ResponseEntity<List<AutoSuggestionDto>> getByType(
            @PathVariable Long novelId,
            @PathVariable AutoSuggestion.SuggestionType type) {
        return ResponseEntity.ok(suggestionService.getSuggestionsByType(novelId, type));
    }

    @Operation(summary = "获取高优先级推荐")
    @GetMapping("/novel/{novelId}/high-priority")
    public ResponseEntity<List<AutoSuggestionDto>> getHighPriority(
            @PathVariable Long novelId,
            @RequestParam(defaultValue = "7") Integer threshold) {
        return ResponseEntity.ok(suggestionService.getHighPrioritySuggestions(novelId, threshold));
    }

    @Operation(summary = "采纳推荐")
    @PutMapping("/{id}/accept")
    public ResponseEntity<AutoSuggestionDto> accept(
            @PathVariable Long id,
            @RequestBody(required = false) String feedback) {
        return ResponseEntity.ok(suggestionService.acceptSuggestion(id, feedback));
    }

    @Operation(summary = "拒绝推荐")
    @PutMapping("/{id}/reject")
    public ResponseEntity<AutoSuggestionDto> reject(
            @PathVariable Long id,
            @RequestBody(required = false) String reason) {
        return ResponseEntity.ok(suggestionService.rejectSuggestion(id, reason));
    }

    @Operation(summary = "获取推荐统计")
    @GetMapping("/novel/{novelId}/statistics")
    public ResponseEntity<SuggestionStatistics> getStatistics(@PathVariable Long novelId) {
        return ResponseEntity.ok(suggestionService.getStatistics(novelId));
    }

    @Operation(summary = "清理过期推荐")
    @DeleteMapping("/novel/{novelId}/cleanup")
    public ResponseEntity<Integer> cleanup(@PathVariable Long novelId) {
        int count = suggestionService.cleanupExpiredSuggestions(novelId);
        return ResponseEntity.ok(count);
    }
}
