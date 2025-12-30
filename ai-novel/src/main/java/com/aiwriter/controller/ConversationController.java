package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.*;
import com.aiwriter.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI对话控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
@Tag(name = "AI对话", description = "AI写作助手对话接口")
public class ConversationController {
    
    private final ConversationService conversationService;
    
    /**
     * 创建新会话
     */
    @PostMapping("/sessions")
    @Operation(summary = "创建新会话", description = "创建一个新的AI对话会话")
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @RequestBody CreateSessionRequest request) {
        try {
            SessionResponse response = conversationService.createSession(request);
            return ResponseEntity.ok(ApiResponse.success("会话创建成功", response));
        } catch (Exception e) {
            log.error("创建会话失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/messages")
    @Operation(summary = "发送消息", description = "向AI助手发送消息并获取回复")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @RequestBody SendMessageRequest request) {
        try {
            MessageResponse response = conversationService.sendMessage(request);
            return ResponseEntity.ok(ApiResponse.success("消息发送成功", response));
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return ResponseEntity.ok(ApiResponse.error("发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取小说的会话列表
     */
    @GetMapping("/sessions/novel/{novelId}")
    @Operation(summary = "获取会话列表", description = "获取指定小说的所有会话")
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> getSessionsByNovel(
            @PathVariable Long novelId,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<SessionResponse> response = conversationService.getSessionsByNovel(novelId, pageable);
            return ResponseEntity.ok(ApiResponse.success("查询成功", response));
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取会话详情
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "获取会话详情", description = "获取指定会话的详细信息和所有消息")
    public ResponseEntity<ApiResponse<SessionDetailResponse>> getSessionDetail(
            @PathVariable Long sessionId) {
        try {
            SessionDetailResponse response = conversationService.getSessionDetail(sessionId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", response));
        } catch (Exception e) {
            log.error("获取会话详情失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 归档会话
     */
    @PutMapping("/sessions/{sessionId}/archive")
    @Operation(summary = "归档会话", description = "将指定会话标记为已归档")
    public ResponseEntity<ApiResponse<String>> archiveSession(@PathVariable Long sessionId) {
        try {
            conversationService.archiveSession(sessionId);
            return ResponseEntity.ok(ApiResponse.success("归档成功", "success"));
        } catch (Exception e) {
            log.error("归档会话失败", e);
            return ResponseEntity.ok(ApiResponse.error("归档失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定会话及其所有消息")
    public ResponseEntity<ApiResponse<String>> deleteSession(@PathVariable Long sessionId) {
        try {
            conversationService.deleteSession(sessionId);
            return ResponseEntity.ok(ApiResponse.success("删除成功", "success"));
        } catch (Exception e) {
            log.error("删除会话失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics/novel/{novelId}")
    @Operation(summary = "获取统计信息", description = "获取指定小说的对话统计信息")
    public ResponseEntity<ApiResponse<ConversationStatistics>> getStatistics(
            @PathVariable Long novelId) {
        try {
            ConversationStatistics response = conversationService.getStatistics(novelId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", response));
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
}
