package com.aiwriter.controller;

import com.aiwriter.dto.NovelCreationRequest;
import com.aiwriter.dto.NovelCreationResponse;
import com.aiwriter.service.SmartNovelCreationService;
import com.aiwriter.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 智能新建小说控制器
 * 提供AI驱动的交互式小说创建流程
 */
@Tag(name = "智能小说创建", description = "AI驱动的交互式小说创建流程")
@RestController
@RequestMapping("/api/smart-novel-creation")
@RequiredArgsConstructor
public class SmartNovelCreationController {
    
    private final SmartNovelCreationService smartNovelCreationService;
    
    /**
     * 开始智能创建会话
     */
    @Operation(summary = "开始智能创建", description = "启动AI驱动的小说创建会话")
    @PostMapping("/start")
    public ApiResponse<NovelCreationResponse> startCreation(
            @Valid @RequestBody NovelCreationRequest request,
            @RequestParam(required = false) String sessionId) {
        
        // 如果没有提供sessionId，则生成一个
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }
        
        NovelCreationResponse response = smartNovelCreationService.startCreation(request, sessionId);
        // 将sessionId也放入response的context中，便于前端使用
        if (response.getContext() == null) {
            response.setContext(new java.util.HashMap<>());
        }
        response.getContext().put("sessionId", sessionId);
        return ApiResponse.success("创建会话成功", response);
    }
    
    /**
     * 处理用户回复
     */
    @Operation(summary = "处理用户回复", description = "处理用户对AI问题的回复，推进创建流程")
    @PostMapping("/reply")
    public ApiResponse<NovelCreationResponse> processReply(
            @Valid @RequestBody NovelCreationRequest request,
            @RequestParam String sessionId) {
        
        NovelCreationResponse response = smartNovelCreationService.processReply(request, sessionId);
        return ApiResponse.success("处理成功", response);
    }
    
    /**
     * 获取当前会话状态
     */
    @Operation(summary = "获取会话状态", description = "获取当前创建会话的状态")
    @GetMapping("/session/{sessionId}")
    public ApiResponse<NovelCreationResponse> getSessionStatus(@PathVariable String sessionId) {
        NovelCreationResponse response = smartNovelCreationService.getSessionStatus(sessionId);
        return ApiResponse.success("获取成功", response);
    }
    
    /**
     * 重置创建会话
     */
    @Operation(summary = "重置会话", description = "重置创建会话，重新开始")
    @PostMapping("/reset/{sessionId}")
    public ApiResponse<String> resetSession(@PathVariable String sessionId) {
        smartNovelCreationService.resetSession(sessionId);
        return ApiResponse.success("会话已重置");
    }
}