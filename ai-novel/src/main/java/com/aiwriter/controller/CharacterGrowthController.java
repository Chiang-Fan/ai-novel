package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.CharacterGrowthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/character-growth")
@RequiredArgsConstructor
public class CharacterGrowthController {
    
    private final CharacterGrowthService growthService;
    
    /**
     * 创建成长记录
     */
    @PostMapping("/records")
    public ResponseEntity<GrowthRecordResponse> createGrowthRecord(
            @RequestBody CreateGrowthRecordRequest request) {
        return ResponseEntity.ok(growthService.createGrowthRecord(request));
    }
    
    /**
     * 获取角色成长轨迹
     */
    @GetMapping("/records/character/{characterId}")
    public ResponseEntity<List<GrowthRecordResponse>> getGrowthTrajectory(
            @PathVariable Long characterId) {
        return ResponseEntity.ok(growthService.getGrowthTrajectory(characterId));
    }
    
    /**
     * 删除成长记录
     */
    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<Void> deleteGrowthRecord(@PathVariable Long recordId) {
        growthService.deleteGrowthRecord(recordId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 添加里程碑
     */
    @PostMapping("/milestones")
    public ResponseEntity<MilestoneResponse> createMilestone(
            @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity.ok(growthService.createMilestone(request));
    }
    
    /**
     * 获取角色里程碑列表
     */
    @GetMapping("/milestones/character/{characterId}")
    public ResponseEntity<List<MilestoneResponse>> getMilestones(
            @PathVariable Long characterId) {
        return ResponseEntity.ok(growthService.getMilestones(characterId));
    }
    
    /**
     * 删除里程碑
     */
    @DeleteMapping("/milestones/{milestoneId}")
    public ResponseEntity<Void> deleteMilestone(@PathVariable Long milestoneId) {
        growthService.deleteMilestone(milestoneId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 生成成长分析
     */
    @PostMapping("/analyze")
    public ResponseEntity<GrowthAnalysisResponse> generateAnalysis(
            @RequestBody GrowthAnalysisRequest request) {
        return ResponseEntity.ok(growthService.generateAnalysis(request));
    }
    
    /**
     * 对比不同时期
     */
    @PostMapping("/compare")
    public ResponseEntity<GrowthComparisonResponse> compareGrowth(
            @RequestBody GrowthComparisonRequest request) {
        return ResponseEntity.ok(growthService.compareGrowth(request));
    }
}
