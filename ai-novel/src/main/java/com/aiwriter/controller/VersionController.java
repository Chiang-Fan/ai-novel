package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 版本管理控制器
 */
@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class VersionController {
    
    private final VersionService versionService;
    
    /**
     * 创建版本
     */
    @PostMapping("/create")
    public ApiResponse<VersionResponse> createVersion(@RequestBody CreateVersionRequest request) {
        try {
            VersionResponse response = versionService.createVersion(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取版本列表
     */
    @GetMapping("/chapter/{chapterId}")
    public ApiResponse<Page<VersionResponse>> getVersionList(
        @PathVariable Long chapterId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<VersionResponse> response = versionService.getVersionList(chapterId, page, size);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取版本详情
     */
    @GetMapping("/{versionId}")
    public ApiResponse<VersionDetailResponse> getVersionDetail(@PathVariable Long versionId) {
        try {
            VersionDetailResponse response = versionService.getVersionDetail(versionId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 对比两个版本
     */
    @PostMapping("/compare")
    public ApiResponse<CompareVersionResponse> compareVersions(@RequestBody CompareVersionRequest request) {
        try {
            CompareVersionResponse response = versionService.compareVersions(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 回滚到指定版本
     */
    @PostMapping("/{versionId}/rollback")
    public ApiResponse<VersionResponse> rollbackToVersion(@PathVariable Long versionId) {
        try {
            VersionResponse response = versionService.rollbackToVersion(versionId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除版本
     */
    @DeleteMapping("/{versionId}")
    public ApiResponse<Void> deleteVersion(@PathVariable Long versionId) {
        try {
            versionService.deleteVersion(versionId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新版本标签
     */
    @PutMapping("/{versionId}/tag")
    public ApiResponse<VersionResponse> updateVersionTag(
        @PathVariable Long versionId,
        @RequestBody UpdateVersionTagRequest request
    ) {
        try {
            VersionResponse response = versionService.updateVersionTag(
                versionId, 
                request.getVersionTag(), 
                request.getVersionNote()
            );
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取版本统计
     */
    @GetMapping("/statistics/chapter/{chapterId}")
    public ApiResponse<VersionStatistics> getVersionStatistics(@PathVariable Long chapterId) {
        try {
            VersionStatistics response = versionService.getVersionStatistics(chapterId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
