package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.OutlineRecommendationRequest;
import com.aiwriter.dto.OutlineRecommendationResponse;
import com.aiwriter.entity.Outline;
import com.aiwriter.service.OutlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/outlines")
@RequiredArgsConstructor
public class OutlineController {
    
    private final OutlineService outlineService;
    
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<Outline>> getOutlinesByNovel(@PathVariable Long novelId) {
        List<Outline> outlines = outlineService.getOutlinesByNovel(novelId);
        return ApiResponse.success(outlines);
    }
    
    @GetMapping("/novel/{novelId}/root")
    public ApiResponse<List<Outline>> getRootOutlines(@PathVariable Long novelId) {
        List<Outline> outlines = outlineService.getRootOutlines(novelId);
        return ApiResponse.success(outlines);
    }
    
    @GetMapping("/novel/{novelId}/parent/{parentId}")
    public ApiResponse<List<Outline>> getChildOutlines(@PathVariable Long novelId, 
                                                        @PathVariable Long parentId) {
        List<Outline> outlines = outlineService.getChildOutlines(novelId, parentId);
        return ApiResponse.success(outlines);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Outline> getOutline(@PathVariable Long id) {
        Outline outline = outlineService.getOutlineById(id);
        return ApiResponse.success(outline);
    }
    
    @PostMapping
    public ApiResponse<Outline> createOutline(@Valid @RequestBody Outline outline) {
        Outline created = outlineService.createOutline(outline);
        return ApiResponse.success(created);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Outline> updateOutline(@PathVariable Long id, 
                                              @Valid @RequestBody Outline outline) {
        Outline updated = outlineService.updateOutline(id, outline);
        return ApiResponse.success(updated);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOutline(@PathVariable Long id) {
        outlineService.deleteOutline(id);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/recommend")
    public ApiResponse<List<OutlineRecommendationResponse>> recommendOutlines(
            @Valid @RequestBody OutlineRecommendationRequest request) {
        List<OutlineRecommendationResponse> recommendations = 
            outlineService.recommendOutlines(request);
        return ApiResponse.success(recommendations);
    }
}
