package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.entity.Chapter;
import com.aiwriter.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节管理Controller
 */
@Tag(name = "章节管理", description = "章节的创建、查询、更新、删除和AI续写")
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {
    
    private final ChapterService chapterService;
    
    @Operation(summary = "创建章节", description = "手动创建一个章节")
    @PostMapping
    public ApiResponse<Chapter> createChapter(@Valid @RequestBody ChapterCreateRequest request) {
        Chapter chapter = chapterService.createChapter(request);
        return ApiResponse.success("创建成功", chapter);
    }
    
    @Operation(summary = "AI续写章节", description = "使用AI自动续写下一章")
    @PostMapping("/continue")
    public ApiResponse<Chapter> continueChapter(@Valid @RequestBody ChapterContinueRequest request) {
        Chapter chapter = chapterService.continueChapter(request);
        return ApiResponse.success("续写成功", chapter);
    }
    
    @Operation(summary = "获取章节详情", description = "根据ID获取章节详细内容")
    @GetMapping("/{id}")
    public ApiResponse<Chapter> getChapter(@PathVariable Long id) {
        Chapter chapter = chapterService.getChapter(id);
        return ApiResponse.success(chapter);
    }
    
    @Operation(summary = "获取小说的所有章节", description = "获取指定小说的所有章节列表")
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<Chapter>> getChaptersByNovel(@PathVariable Long novelId) {
        List<Chapter> chapters = chapterService.getChaptersByNovel(novelId);
        return ApiResponse.success(chapters);
    }
    
    @Operation(summary = "更新章节", description = "更新章节内容")
    @PutMapping("/{id}")
    public ApiResponse<Chapter> updateChapter(
            @PathVariable Long id,
            @Valid @RequestBody ChapterCreateRequest request) {
        Chapter chapter = chapterService.updateChapter(id, request);
        return ApiResponse.success("更新成功", chapter);
    }
    
    @Operation(summary = "删除章节", description = "删除指定章节")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @Operation(summary = "重新生成章节", description = "重新使用AI生成指定章节")
    @PostMapping("/{id}/regenerate")
    public ApiResponse<Chapter> regenerateChapter(
            @PathVariable Long id,
            @Parameter(description = "续写方向") @RequestParam(required = false) String direction) {
        Chapter chapter = chapterService.regenerateChapter(id, direction);
        return ApiResponse.success("重新生成成功", chapter);
    }
    
    @Operation(
        summary = "智能创建章节", 
        description = "输入章节正文，自动提取标题、摘要、关键词、角色、场景等元数据"
    )
    @PostMapping("/smart")
    public ApiResponse<SmartChapterCreateResponse> createChapterSmart(
            @Valid @RequestBody SmartChapterCreateRequest request) {
        SmartChapterCreateResponse response = chapterService.createChapterSmart(request);
        return ApiResponse.success("智能创建成功", response);
    }
}
