package com.ai.novel.controller;

import com.ai.novel.dto.request.ChapterContinueRequest;
import com.ai.novel.dto.request.ChapterCreateRequest;
import com.ai.novel.dto.response.ApiResponse;
import com.ai.novel.dto.response.ChapterResponse;
import com.ai.novel.entity.Chapter;
import com.ai.novel.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 章节管理控制器
 */
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {
    
    private final ChapterService chapterService;
    
    /**
     * 创建章节
     */
    @PostMapping
    public ApiResponse<ChapterResponse> createChapter(@Valid @RequestBody ChapterCreateRequest request) {
        Chapter chapter = chapterService.createChapter(request);
        return ApiResponse.success(convertToResponse(chapter));
    }
    
    /**
     * AI续写章节
     */
    @PostMapping("/continue")
    public ApiResponse<ChapterResponse> continueChapter(@Valid @RequestBody ChapterContinueRequest request) {
        Chapter chapter = chapterService.continueChapter(request);
        return ApiResponse.success(convertToResponse(chapter));
    }
    
    /**
     * 获取章节详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ChapterResponse> getChapter(@PathVariable Long id) {
        Chapter chapter = chapterService.getChapterById(id);
        return ApiResponse.success(convertToResponse(chapter));
    }
    
    /**
     * 分页查询小说的章节列表
     */
    @GetMapping("/novel/{novelId}")
    public ApiResponse<Page<ChapterResponse>> listChapters(
            @PathVariable Long novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "chapterNumber"));
        
        Page<ChapterResponse> chapters = chapterService.listChapters(novelId, pageable)
                .map(this::convertToResponse);
        
        return ApiResponse.success(chapters);
    }
    
    /**
     * 更新章节
     */
    @PutMapping("/{id}")
    public ApiResponse<ChapterResponse> updateChapter(
            @PathVariable Long id,
            @Valid @RequestBody ChapterCreateRequest request) {
        Chapter chapter = chapterService.updateChapter(id, request);
        return ApiResponse.success(convertToResponse(chapter));
    }
    
    /**
     * 删除章节
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return ApiResponse.success("章节删除成功", null);
    }
    
    /**
     * 转换为响应DTO
     */
    private ChapterResponse convertToResponse(Chapter chapter) {
        ChapterResponse response = new ChapterResponse();
        response.setId(chapter.getId());
        response.setNovelId(chapter.getNovel().getId());
        response.setChapterNumber(chapter.getChapterNumber());
        response.setTitle(chapter.getTitle());
        response.setContent(chapter.getContent());
        response.setSummary(chapter.getSummary());
        response.setWordCount(chapter.getWordCount());
        response.setStatus(chapter.getStatus());
        
        if (chapter.getScene() != null) {
            response.setSceneId(chapter.getScene().getId());
            response.setSceneName(chapter.getScene().getTitle());
        }
        
        response.setCreatedAt(chapter.getCreatedAt());
        response.setUpdatedAt(chapter.getUpdatedAt());
        return response;
    }
}
