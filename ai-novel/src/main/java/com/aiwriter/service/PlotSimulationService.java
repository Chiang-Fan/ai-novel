package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 情节推演服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlotSimulationService {
    
    private final PlotSimulationRepository simulationRepository;
    private final SimulationBranchRepository branchRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final CharacterRepository characterRepository;
    private final com.aiwriter.service.ai.AiService aiService;
    
    /**
     * 创建模拟
     */
    @Transactional
    public SimulationResponse createSimulation(CreateSimulationRequest request) {
        // 验证小说存在
        novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        // 创建模拟
        PlotSimulation simulation = new PlotSimulation();
        simulation.setNovelId(request.getNovelId());
        simulation.setChapterId(request.getChapterId());
        simulation.setSimulationName(request.getSimulationName());
        simulation.setDescription(request.getDescription());
        simulation.setStartingPoint(request.getStartingPoint());
        simulation.setCurrentState(request.getStartingPoint());
        simulation.setStatus("ACTIVE");
        
        simulation = simulationRepository.save(simulation);
        
        return convertToResponse(simulation);
    }
    
    /**
     * 获取模拟列表
     */
    public List<SimulationResponse> getSimulationList(Long novelId) {
        List<PlotSimulation> simulations = simulationRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        
        return simulations.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取模拟详情
     */
    public SimulationResponse getSimulationDetail(Long simulationId) {
        PlotSimulation simulation = simulationRepository.findById(simulationId)
            .orElseThrow(() -> new RuntimeException("模拟不存在"));
        
        return convertToResponse(simulation);
    }
    
    /**
     * 删除模拟
     */
    @Transactional
    public void deleteSimulation(Long simulationId) {
        PlotSimulation simulation = simulationRepository.findById(simulationId)
            .orElseThrow(() -> new RuntimeException("模拟不存在"));
        
        simulationRepository.delete(simulation);
    }
    
    /**
     * 创建分支
     */
    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        PlotSimulation simulation = simulationRepository.findById(request.getSimulationId())
            .orElseThrow(() -> new RuntimeException("模拟不存在"));
        
        // 计算深度
        int depthLevel = 0;
        if (request.getParentBranchId() != null) {
            SimulationBranch parent = branchRepository.findById(request.getParentBranchId())
                .orElseThrow(() -> new RuntimeException("父分支不存在"));
            depthLevel = parent.getDepthLevel() + 1;
        }
        
        // 创建分支
        SimulationBranch branch = new SimulationBranch();
        branch.setSimulationId(request.getSimulationId());
        branch.setParentBranchId(request.getParentBranchId());
        branch.setBranchName(request.getBranchName());
        branch.setDecisionPoint(request.getDecisionPoint());
        branch.setDecisionContent(request.getDecisionContent());
        branch.setDepthLevel(depthLevel);
        branch.setIsEnding(request.getIsEnding() != null ? request.getIsEnding() : false);
        
        branch = branchRepository.save(branch);
        
        return convertToBranchResponse(branch);
    }
    
    /**
     * 预测分支后果
     */
    @Transactional
    public BranchResponse predictBranchOutcome(Long branchId) {
        SimulationBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new RuntimeException("分支不存在"));
        
        PlotSimulation simulation = simulationRepository.findById(branch.getSimulationId())
            .orElseThrow(() -> new RuntimeException("模拟不存在"));
        
        // 构建上下文
        String context = buildPredictionContext(simulation, branch);
        
        // 调用 AI 预测
        String prediction = predictOutcome(context, branch);
        
        // 解析预测结果
        Map<String, Object> result = parsePrediction(prediction);
        
        // 更新分支
        branch.setPredictedOutcome((String) result.get("outcome"));
        branch.setCharacterImpact((String) result.get("characterImpact"));
        branch.setPlotImpact((String) result.get("plotImpact"));
        branch.setProbabilityScore(new BigDecimal(result.get("probability").toString()));
        branch.setQualityScore(new BigDecimal(result.get("quality").toString()));
        
        branch = branchRepository.save(branch);
        
        return convertToBranchResponse(branch);
    }
    
    /**
     * 获取决策树
     */
    public DecisionTreeResponse getDecisionTree(Long simulationId) {
        PlotSimulation simulation = simulationRepository.findById(simulationId)
            .orElseThrow(() -> new RuntimeException("模拟不存在"));
        
        List<SimulationBranch> branches = branchRepository.findBySimulationIdOrderByDepthLevelAsc(simulationId);
        
        DecisionTreeResponse response = new DecisionTreeResponse();
        response.setSimulation(convertToResponse(simulation));
        
        // 构建节点
        List<DecisionTreeResponse.TreeNode> nodes = new ArrayList<>();
        
        // 添加根节点（起点）
        DecisionTreeResponse.TreeNode rootNode = new DecisionTreeResponse.TreeNode();
        rootNode.setId(0L);
        rootNode.setParentId(null);
        rootNode.setName("起点");
        rootNode.setType("ROOT");
        rootNode.setContent(simulation.getStartingPoint());
        rootNode.setLevel(0);
        nodes.add(rootNode);
        
        // 添加分支节点
        for (SimulationBranch branch : branches) {
            DecisionTreeResponse.TreeNode node = new DecisionTreeResponse.TreeNode();
            node.setId(branch.getId());
            node.setParentId(branch.getParentBranchId() != null ? branch.getParentBranchId() : 0L);
            node.setName(branch.getBranchName());
            node.setType(branch.getIsEnding() ? "ENDING" : "BRANCH");
            node.setContent(branch.getDecisionContent());
            node.setLevel(branch.getDepthLevel() + 1);
            node.setScore(branch.getQualityScore() != null ? branch.getQualityScore().doubleValue() : null);
            nodes.add(node);
        }
        
        response.setNodes(nodes);
        
        // 构建边
        List<DecisionTreeResponse.TreeEdge> edges = new ArrayList<>();
        for (DecisionTreeResponse.TreeNode node : nodes) {
            if (node.getParentId() != null) {
                DecisionTreeResponse.TreeEdge edge = new DecisionTreeResponse.TreeEdge();
                edge.setFrom(node.getParentId());
                edge.setTo(node.getId());
                edge.setLabel(node.getName());
                edges.add(edge);
            }
        }
        
        response.setEdges(edges);
        
        return response;
    }
    
    /**
     * 生成可能的结局
     */
    public EndingAnalysisResponse generateEndings(Long simulationId, int count) {
        PlotSimulation simulation = simulationRepository.findById(simulationId)
            .orElseThrow(() -> new RuntimeException("模拟不存在"));
        
        // 获取所有结局分支
        List<SimulationBranch> endingBranches = branchRepository.findBySimulationIdAndIsEndingTrue(simulationId);
        
        // 如果没有足够的结局分支，使用 AI 生成
        if (endingBranches.size() < count) {
            // 构建上下文
            String context = buildEndingContext(simulation);
            
            // 调用 AI 生成结局
            String aiResponse = generateEndingsWithAI(context, count);
            
            // 解析结局（简化实现）
            return parseEndings(aiResponse);
        }
        
        // 分析现有结局
        return analyzeExistingEndings(endingBranches);
    }
    
    /**
     * 分析结局质量
     */
    public EndingAnalysisResponse analyzeEndings(Long simulationId) {
        List<SimulationBranch> endingBranches = branchRepository.findBySimulationIdAndIsEndingTrue(simulationId);
        
        if (endingBranches.isEmpty()) {
            throw new RuntimeException("没有可分析的结局");
        }
        
        return analyzeExistingEndings(endingBranches);
    }
    
    /**
     * 构建预测上下文
     */
    private String buildPredictionContext(PlotSimulation simulation, SimulationBranch branch) {
        StringBuilder context = new StringBuilder();
        
        context.append("【当前情节状态】\n");
        context.append(simulation.getCurrentState()).append("\n\n");
        
        context.append("【决策点】\n");
        context.append(branch.getDecisionPoint()).append("\n\n");
        
        context.append("【选择的决策】\n");
        context.append(branch.getDecisionContent()).append("\n\n");
        
        // 添加小说背景信息
        novelRepository.findById(simulation.getNovelId()).ifPresent(novel -> {
            context.append("【小说背景】\n");
            context.append("类型：").append(novel.getGenre()).append("\n");
            context.append("简介：").append(novel.getDescription()).append("\n\n");
        });
        
        // 添加角色信息
        List<com.aiwriter.entity.Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(simulation.getNovelId());
        if (!characters.isEmpty()) {
            context.append("【主要角色】\n");
            for (com.aiwriter.entity.Character character : characters) {
                context.append("- ").append(character.getName())
                    .append("（").append(character.getRoleType()).append("）：")
                    .append(character.getPersonality()).append("\n");
            }
        }
        
        return context.toString();
    }
    
    /**
     * 调用 AI 预测后果
     */
    private String predictOutcome(String context, SimulationBranch branch) {
        String prompt = context + "\n\n" +
            "请分析这个决策可能带来的后果，包括：\n" +
            "1. 情节后果（predicted_outcome）\n" +
            "2. 对角色的影响（character_impact）\n" +
            "3. 对整体情节的影响（plot_impact）\n" +
            "4. 可能性评分（probability，0-100）\n" +
            "5. 质量评分（quality，0-100）\n\n" +
            "请以 JSON 格式返回，格式如下：\n" +
            "{\n" +
            "  \"outcome\": \"...\",\n" +
            "  \"characterImpact\": \"...\",\n" +
            "  \"plotImpact\": \"...\",\n" +
            "  \"probability\": 85,\n" +
            "  \"quality\": 78\n" +
            "}";
        
        try {
            return aiService.chat("你是一个专业的小说情节分析师", prompt);
        } catch (Exception e) {
            log.error("AI 预测失败", e);
            return "{\"outcome\":\"预测失败\",\"characterImpact\":\"未知\",\"plotImpact\":\"未知\",\"probability\":50,\"quality\":50}";
        }
    }
    
    /**
     * 解析预测结果
     */
    private Map<String, Object> parsePrediction(String prediction) {
        try {
            // 尝试提取 JSON 部分
            int start = prediction.indexOf("{");
            int end = prediction.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                String jsonStr = prediction.substring(start, end);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonStr, Map.class);
            }
        } catch (Exception e) {
            log.error("解析预测结果失败", e);
        }
        
        // 默认返回
        Map<String, Object> result = new HashMap<>();
        result.put("outcome", "预测结果解析失败");
        result.put("characterImpact", "未知");
        result.put("plotImpact", "未知");
        result.put("probability", 50);
        result.put("quality", 50);
        return result;
    }
    
    /**
     * 构建结局生成上下文
     */
    private String buildEndingContext(PlotSimulation simulation) {
        return "【情节起点】\n" + simulation.getStartingPoint() + "\n\n" +
            "【当前状态】\n" + simulation.getCurrentState();
    }
    
    /**
     * 使用 AI 生成结局
     */
    private String generateEndingsWithAI(String context, int count) {
        String prompt = context + "\n\n" +
            "请为这个故事生成 " + count + " 个不同风格的可能结局。";
        
        try {
            return aiService.chat("你是一个专业的小说创作助手", prompt);
        } catch (Exception e) {
            log.error("AI 生成结局失败", e);
            return "[]";
        }
    }
    
    /**
     * 解析结局
     */
    private EndingAnalysisResponse parseEndings(String aiResponse) {
        EndingAnalysisResponse response = new EndingAnalysisResponse();
        response.setEndings(new ArrayList<>());
        
        // 简化实现：创建默认结局
        EndingAnalysisResponse.EndingOption ending = new EndingAnalysisResponse.EndingOption();
        ending.setEndingName("AI 生成结局");
        ending.setDescription(aiResponse);
        ending.setQualityScore(75.0);
        
        response.getEndings().add(ending);
        response.setRecommendedEnding(ending);
        
        return response;
    }
    
    /**
     * 分析现有结局
     */
    private EndingAnalysisResponse analyzeExistingEndings(List<SimulationBranch> endingBranches) {
        EndingAnalysisResponse response = new EndingAnalysisResponse();
        List<EndingAnalysisResponse.EndingOption> endings = new ArrayList<>();
        
        EndingAnalysisResponse.EndingOption best = null;
        double bestScore = 0;
        
        for (SimulationBranch branch : endingBranches) {
            EndingAnalysisResponse.EndingOption ending = new EndingAnalysisResponse.EndingOption();
            ending.setBranchId(branch.getId());
            ending.setEndingName(branch.getBranchName());
            ending.setDescription(branch.getDecisionContent());
            ending.setQualityScore(branch.getQualityScore() != null ? branch.getQualityScore().doubleValue() : 50.0);
            ending.setCharacterFate(branch.getCharacterImpact());
            ending.setPlotResolution(branch.getPlotImpact());
            
            endings.add(ending);
            
            if (ending.getQualityScore() > bestScore) {
                bestScore = ending.getQualityScore();
                best = ending;
            }
        }
        
        response.setEndings(endings);
        response.setRecommendedEnding(best);
        
        return response;
    }
    
    /**
     * 转换为响应对象
     */
    private SimulationResponse convertToResponse(PlotSimulation simulation) {
        SimulationResponse response = new SimulationResponse();
        response.setId(simulation.getId());
        response.setNovelId(simulation.getNovelId());
        response.setChapterId(simulation.getChapterId());
        response.setSimulationName(simulation.getSimulationName());
        response.setDescription(simulation.getDescription());
        response.setStartingPoint(simulation.getStartingPoint());
        response.setStatus(simulation.getStatus());
        response.setCreatedAt(simulation.getCreatedAt());
        response.setUpdatedAt(simulation.getUpdatedAt());
        
        // 统计分支和结局数量
        List<SimulationBranch> branches = branchRepository.findBySimulationIdOrderByDepthLevelAsc(simulation.getId());
        response.setBranchCount(branches.size());
        response.setEndingCount((int) branches.stream().filter(SimulationBranch::getIsEnding).count());
        
        return response;
    }
    
    /**
     * 转换为分支响应对象
     */
    private BranchResponse convertToBranchResponse(SimulationBranch branch) {
        BranchResponse response = new BranchResponse();
        response.setId(branch.getId());
        response.setSimulationId(branch.getSimulationId());
        response.setParentBranchId(branch.getParentBranchId());
        response.setBranchName(branch.getBranchName());
        response.setDecisionPoint(branch.getDecisionPoint());
        response.setDecisionContent(branch.getDecisionContent());
        response.setPredictedOutcome(branch.getPredictedOutcome());
        response.setCharacterImpact(branch.getCharacterImpact());
        response.setPlotImpact(branch.getPlotImpact());
        response.setProbabilityScore(branch.getProbabilityScore());
        response.setQualityScore(branch.getQualityScore());
        response.setDepthLevel(branch.getDepthLevel());
        response.setIsEnding(branch.getIsEnding());
        response.setCreatedAt(branch.getCreatedAt());
        return response;
    }
}
