package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.WritingStyleResponse;
import com.aiwriter.entity.WritingStyle;
import com.aiwriter.repository.WritingStyleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 写作风格管理Controller
 */
@RestController
@RequestMapping("/api/writing-styles")
@RequiredArgsConstructor
@Slf4j
public class WritingStyleController {
    
    private final WritingStyleRepository styleRepository;
    
    /**
     * 获取风格列表
     */
    @GetMapping
    public ApiResponse<List<WritingStyleResponse>> getStyles(
            @RequestParam(required = false) String category) {
        List<WritingStyle> styles;
        if (category != null && !category.isEmpty()) {
            styles = styleRepository.findByCategoryOrderByUsageCountDesc(category);
        } else {
            styles = styleRepository.findByIsSystemTrueOrderByUsageCountDesc();
        }
        
        List<WritingStyleResponse> responses = styles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("获取成功", responses);
    }
    
    /**
     * 获取风格详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WritingStyleResponse> getStyle(@PathVariable Long id) {
        WritingStyle style = styleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("风格不存在"));
        return ApiResponse.success("获取成功", toResponse(style));
    }
    
    /**
     * 创建风格
     */
    @PostMapping
    public ApiResponse<WritingStyleResponse> createStyle(@RequestBody WritingStyle style) {
        style.setIsSystem(false);
        style.setUsageCount(0);
        WritingStyle saved = styleRepository.save(style);
        return ApiResponse.success("创建成功", toResponse(saved));
    }
    
    /**
     * 更新风格
     */
    @PutMapping("/{id}")
    public ApiResponse<WritingStyleResponse> updateStyle(
            @PathVariable Long id,
            @RequestBody WritingStyle style) {
        WritingStyle existing = styleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("风格不存在"));
        
        existing.setName(style.getName());
        existing.setAuthor(style.getAuthor());
        existing.setDescription(style.getDescription());
        existing.setSampleText(style.getSampleText());
        existing.setStyleFeatures(style.getStyleFeatures());
        existing.setCategory(style.getCategory());
        existing.setLanguageComplexity(style.getLanguageComplexity());
        existing.setSentenceLength(style.getSentenceLength());
        existing.setTone(style.getTone());
        
        WritingStyle updated = styleRepository.save(existing);
        return ApiResponse.success("更新成功", toResponse(updated));
    }
    
    /**
     * 删除风格
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteStyle(@PathVariable Long id) {
        styleRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
    
    /**
     * 转换为响应DTO
     */
    private WritingStyleResponse toResponse(WritingStyle style) {
        return WritingStyleResponse.builder()
                .id(style.getId())
                .name(style.getName())
                .author(style.getAuthor())
                .description(style.getDescription())
                .sampleText(style.getSampleText())
                .category(style.getCategory())
                .languageComplexity(style.getLanguageComplexity())
                .sentenceLength(style.getSentenceLength())
                .tone(style.getTone())
                .usageCount(style.getUsageCount())
                .rating(style.getRating())
                .isSystem(style.getIsSystem())
                .createdAt(style.getCreatedAt())
                .build();
    }
}
