package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI图片生成Controller
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationController {
    
    private final ImageGenerationService imageService;
    
    /**
     * 生成图片
     */
    @PostMapping("/generate")
    public ApiResponse<List<GeneratedImageResponse>> generateImage(@RequestBody ImageGenerationRequest request) {
        return ApiResponse.success("生成成功", imageService.generateImage(request));
    }
    
    /**
     * 获取图片列表
     */
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<GeneratedImageResponse>> getImages(
            @PathVariable Long novelId,
            @RequestParam(required = false) String imageType) {
        return ApiResponse.success("获取成功", imageService.getImages(novelId, imageType));
    }
    
    /**
     * 采用图片
     */
    @PostMapping("/{imageId}/adopt")
    public ApiResponse<String> adoptImage(@PathVariable Long imageId) {
        imageService.adoptImage(imageId);
        return ApiResponse.success("采用成功", null);
    }
    
    /**
     * 获取模板列表
     */
    @GetMapping("/templates")
    public ApiResponse<List<ImageTemplateResponse>> getTemplates(
            @RequestParam(required = false) String category) {
        return ApiResponse.success("获取成功", imageService.getTemplates(category));
    }
}
