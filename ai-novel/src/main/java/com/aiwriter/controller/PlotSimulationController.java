package com.aiwriter.controller;

import com.aiwriter.dto.*;
import com.aiwriter.service.PlotSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 情节推演控制器
 */
@RestController
@RequestMapping("/api/plot-simulation")
@RequiredArgsConstructor
public class PlotSimulationController {
    
    private final PlotSimulationService simulationService;
    
    /**
     * 创建模拟
     */
    @PostMapping("/create")
    public ApiResponse<SimulationResponse> createSimulation(@RequestBody CreateSimulationRequest request) {
        try {
            SimulationResponse response = simulationService.createSimulation(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取模拟列表
     */
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<SimulationResponse>> getSimulationList(@PathVariable Long novelId) {
        try {
            List<SimulationResponse> response = simulationService.getSimulationList(novelId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取模拟详情
     */
    @GetMapping("/{simulationId}")
    public ApiResponse<SimulationResponse> getSimulationDetail(@PathVariable Long simulationId) {
        try {
            SimulationResponse response = simulationService.getSimulationDetail(simulationId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除模拟
     */
    @DeleteMapping("/{simulationId}")
    public ApiResponse<Void> deleteSimulation(@PathVariable Long simulationId) {
        try {
            simulationService.deleteSimulation(simulationId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 创建分支
     */
    @PostMapping("/{simulationId}/branch")
    public ApiResponse<BranchResponse> createBranch(@RequestBody CreateBranchRequest request) {
        try {
            BranchResponse response = simulationService.createBranch(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 预测分支后果
     */
    @PostMapping("/branch/{branchId}/predict")
    public ApiResponse<BranchResponse> predictBranchOutcome(@PathVariable Long branchId) {
        try {
            BranchResponse response = simulationService.predictBranchOutcome(branchId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取决策树
     */
    @GetMapping("/{simulationId}/tree")
    public ApiResponse<DecisionTreeResponse> getDecisionTree(@PathVariable Long simulationId) {
        try {
            DecisionTreeResponse response = simulationService.getDecisionTree(simulationId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 生成可能结局
     */
    @PostMapping("/{simulationId}/endings")
    public ApiResponse<EndingAnalysisResponse> generateEndings(
        @PathVariable Long simulationId,
        @RequestParam(defaultValue = "3") int count
    ) {
        try {
            EndingAnalysisResponse response = simulationService.generateEndings(simulationId, count);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 分析结局质量
     */
    @PostMapping("/endings/analyze")
    public ApiResponse<EndingAnalysisResponse> analyzeEndings(@RequestParam Long simulationId) {
        try {
            EndingAnalysisResponse response = simulationService.analyzeEndings(simulationId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
