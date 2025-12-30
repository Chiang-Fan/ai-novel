package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.NovelCreateRequest;
import com.aiwriter.dto.NovelCreationRecommendationResponse;
import com.aiwriter.dto.NovelUpdateRequest;
import com.aiwriter.entity.Novel;
import com.aiwriter.service.NovelService;
import com.aiwriter.service.ai.NovelCreationAiRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小说管理Controller - 强化版
 * 支持大纲/场景必填和AI推荐
 */
@Tag(name = "小说管理", description = "小说的创建、查询、更新、删除")
@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {
    
    private final NovelService novelService;
    private final NovelCreationAiRecommendationService aiRecommendationService;
    
    @Operation(summary = "创建小说", description = "创建一部新小说")
    @PostMapping
    public ApiResponse<Novel> createNovel(@Valid @RequestBody NovelCreateRequest request) {
        Novel novel = novelService.createNovel(request);
        return ApiResponse.success("创建成功", novel);
    }
    
    @Operation(summary = "获取小说列表", description = "获取所有小说列表")
    @GetMapping
    public ApiResponse<List<Novel>> getAllNovels(
            @Parameter(description = "小说状态") @RequestParam(required = false) String status,
            @Parameter(description = "小说类型") @RequestParam(required = false) String genre) {
        
        List<Novel> novels;
        if (status != null) {
            novels = novelService.getNovelsByStatus(status);
        } else if (genre != null) {
            novels = novelService.getNovelsByGenre(genre);
        } else {
            novels = novelService.getAllNovels();
        }
        
        return ApiResponse.success(novels);
    }
    
    @Operation(summary = "获取小说详情", description = "根据ID获取小说详细信息")
    @GetMapping("/{id}")
    public ApiResponse<Novel> getNovel(@PathVariable Long id) {
        Novel novel = novelService.getNovel(id);
        return ApiResponse.success(novel);
    }
    
    @Operation(summary = "更新小说", description = "更新小说信息")
    @PutMapping("/{id}")
    public ApiResponse<Novel> updateNovel(
            @PathVariable Long id,
            @Valid @RequestBody NovelUpdateRequest request) {
        Novel novel = novelService.updateNovel(id, request);
        return ApiResponse.success("更新成功", novel);
    }
    
    @Operation(summary = "删除小说", description = "删除指定小说")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNovel(@PathVariable Long id) {
        novelService.deleteNovel(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @Operation(summary = "搜索小说", description = "根据关键词搜索小说")
    @GetMapping("/search")
    public ApiResponse<List<Novel>> searchNovels(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        List<Novel> novels = novelService.searchNovels(keyword);
        return ApiResponse.success(novels);
    }
    
    @Operation(summary = "获取小说创建推荐", description = "为新小说获取AI智能推荐")
    @PostMapping("/recommendations")
    public ApiResponse<NovelCreationRecommendationResponse> getCreationRecommendations(
            @Valid @RequestBody NovelCreateRequest request) {
        NovelCreationRecommendationResponse recommendations = 
            aiRecommendationService.generateRecommendations(request);
        return ApiResponse.success("推荐生成成功", recommendations);
    }
}
