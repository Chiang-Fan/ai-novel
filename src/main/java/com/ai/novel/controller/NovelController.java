package com.ai.novel.controller;

import com.ai.novel.dto.request.NovelCreateRequest;
import com.ai.novel.dto.request.NovelUpdateRequest;
import com.ai.novel.dto.response.ApiResponse;
import com.ai.novel.dto.response.NovelResponse;
import com.ai.novel.dto.response.ChapterResponse;
import com.ai.novel.entity.Novel;
import com.ai.novel.entity.Chapter;
import com.ai.novel.entity.enums.NovelStatus;
import com.ai.novel.service.NovelService;
import com.ai.novel.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 小说管理控制器
 */
@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {
    
    private final NovelService novelService;
    private final ChapterService chapterService;
    
    /**
     * 创建小说
     */
    @PostMapping
    public ApiResponse<NovelResponse> createNovel(@Valid @RequestBody NovelCreateRequest request) {
        Novel novel = novelService.createNovel(request);
        return ApiResponse.success(convertToResponse(novel));
    }
    
    /**
     * 更新小说
     */
    @PutMapping("/{id}")
    public ApiResponse<NovelResponse> updateNovel(
            @PathVariable Long id,
            @Valid @RequestBody NovelUpdateRequest request) {
        Novel novel = novelService.updateNovel(id, request);
        return ApiResponse.success(convertToResponse(novel));
    }
    
    /**
     * 获取小说详情
     */
    @GetMapping("/{id}")
    public ApiResponse<NovelResponse> getNovel(@PathVariable Long id) {
        Novel novel = novelService.getNovelById(id);
        return ApiResponse.success(convertToResponse(novel));
    }
    
    /**
     * 分页查询小说列表
     */
    @GetMapping
    public ApiResponse<Page<NovelResponse>> listNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) NovelStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        
        Page<NovelResponse> novels;
        if (status != null) {
            novels = novelService.listNovelsByStatus(status, pageable)
                    .map(this::convertToResponse);
        } else {
            novels = novelService.listNovels(pageable)
                    .map(this::convertToResponse);
        }
        
        return ApiResponse.success(novels);
    }
    
    /**
     * 删除小说
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNovel(@PathVariable Long id) {
        novelService.deleteNovel(id);
        return ApiResponse.success("小说删除成功", null);
    }
    
    /**
     * 获取小说的章节列表
     */
    @GetMapping("/{id}/chapters")
    public ApiResponse<Page<ChapterResponse>> getNovelChapters(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "chapterNumber"));
        
        Page<ChapterResponse> chapters = chapterService.listChapters(id, pageable)
                .map(this::convertToChapterResponse);
        
        return ApiResponse.success(chapters);
    }
    
    /**
     * 转换为响应DTO
     */
    private NovelResponse convertToResponse(Novel novel) {
        NovelResponse response = new NovelResponse();
        response.setId(novel.getId());
        response.setTitle(novel.getTitle());
        response.setDescription(novel.getDescription());
        response.setAuthor(novel.getAuthor());
        response.setType(novel.getType());
        response.setWritingStyle(novel.getWritingStyle());
        response.setTargetWordCount(novel.getTargetWordCount());
        response.setCurrentWordCount(novel.getCurrentWordCount());
        response.setChapterCount(novel.getChapterCount());
        response.setStatus(novel.getStatus());
        response.setCreatedAt(novel.getCreatedAt());
        response.setUpdatedAt(novel.getUpdatedAt());
        return response;
    }
    
    /**
     * 转换章节为响应DTO
     */
    private ChapterResponse convertToChapterResponse(Chapter chapter) {
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
