package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.RelationshipManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色关系管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/relationships")
@RequiredArgsConstructor
public class RelationshipController {
    
    private final RelationshipManagementService relationshipService;
    
    /**
     * 创建关系
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RelationshipResponse>> createRelationship(
            @RequestBody RelationshipRequest request) {
        try {
            RelationshipResponse response = relationshipService.createRelationship(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("创建关系失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新关系
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RelationshipResponse>> updateRelationship(
            @PathVariable Long id,
            @RequestBody RelationshipRequest request) {
        try {
            RelationshipResponse response = relationshipService.updateRelationship(id, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("更新关系失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除关系
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRelationship(@PathVariable Long id) {
        try {
            relationshipService.deleteRelationship(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("删除关系失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取关系详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RelationshipResponse>> getRelationship(@PathVariable Long id) {
        try {
            RelationshipResponse response = relationshipService.getRelationship(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("获取关系失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取角色的所有关系
     */
    @GetMapping("/character/{characterId}")
    public ResponseEntity<ApiResponse<List<RelationshipResponse>>> getCharacterRelationships(
            @PathVariable Long characterId) {
        try {
            List<RelationshipResponse> responses = relationshipService.getCharacterRelationships(characterId);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取角色关系失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取小说的关系图谱
     */
    @GetMapping("/novel/{novelId}/graph")
    public ResponseEntity<ApiResponse<RelationshipGraphResponse>> getRelationshipGraph(
            @PathVariable Long novelId) {
        try {
            RelationshipGraphResponse response = relationshipService.getRelationshipGraph(novelId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("获取关系图谱失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 添加关系事件
     */
    @PostMapping("/history")
    public ResponseEntity<ApiResponse<RelationshipHistoryResponse>> addRelationshipEvent(
            @RequestBody RelationshipHistoryRequest request) {
        try {
            RelationshipHistoryResponse response = relationshipService.addRelationshipEvent(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("添加关系事件失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取关系历史
     */
    @GetMapping("/{relationshipId}/history")
    public ResponseEntity<ApiResponse<List<RelationshipHistoryResponse>>> getRelationshipHistory(
            @PathVariable Long relationshipId) {
        try {
            List<RelationshipHistoryResponse> responses = 
                relationshipService.getRelationshipHistory(relationshipId);
            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("获取关系历史失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除关系历史记录
     */
    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<ApiResponse<Void>> deleteRelationshipHistory(@PathVariable Long historyId) {
        try {
            relationshipService.deleteRelationshipHistory(historyId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("删除关系历史失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
