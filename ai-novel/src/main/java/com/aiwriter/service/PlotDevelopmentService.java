package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 情节发展建议服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlotDevelopmentService {
    
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final PlotSuggestionRepository suggestionRepository;
    private final PlotProjectionRepository projectionRepository;
    private final ConflictTrackingRepository conflictRepository;
    private final PlotHookRepository plotHookRepository;
    private final CharacterRepository characterRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 生成情节建议
     */
    @Transactional
    public List<PlotSuggestionResponse> generateSuggestions(Long novelId) {
        log.info("开始生成情节建议，小说ID: {}", novelId);
        
        // 验证小说
        novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        List<PlotSuggestion> suggestions = new ArrayList<>();
        
        // 1. 分析未解决的伏笔
        suggestions.addAll(analyzePlotHooks(novelId));
        
        // 2. 分析角色发展
        suggestions.addAll(analyzeCharacterArcs(novelId));
        
        // 3. 分析冲突升级
        suggestions.addAll(analyzeConflictEscalation(novelId));
        
        // 4. 分析节奏问题
        suggestions.addAll(analyzePacing(novelId));
        
        // 保存建议
        suggestions = suggestionRepository.saveAll(suggestions);
        
        return suggestions.stream()
                .map(this::toSuggestionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 情节推演
     */
    @Transactional
    public PlotProjectionResponse projectPlot(PlotProjectionRequest request) {
        log.info("开始情节推演，类型: {}", request.getProjectionType());
        
        // 验证数据
        novelRepository.findById(request.getNovelId())
                .orElseThrow(() -> new RuntimeException("小说不存在"));
        Chapter currentChapter = chapterRepository.findById(request.getCurrentChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        // 收集上下文
        String context = buildProjectionContext(request);
        
        // AI推演
        String projectionText = projectWithAI(context, request);
        
        // 提取关键事件
        List<PlotProjectionResponse.KeyEvent> keyEvents = extractKeyEvents(projectionText);
        
        // 分析角色变化
        Map<String, String> characterChanges = analyzeCharacterChanges(request, projectionText);
        
        // 识别情节线
        List<PlotProjectionResponse.PlotThread> plotThreads = identifyPlotThreads(projectionText);
        
        // 计算置信度
        int confidenceScore = calculateProjectionConfidence(request, projectionText);
        
        // 保存推演记录
        PlotProjection projection = PlotProjection.builder()
                .novelId(request.getNovelId())
                .currentChapterId(request.getCurrentChapterId())
                .projectionText(projectionText)
                .projectionType(request.getProjectionType())
                .chaptersAhead(request.getChaptersAhead())
                .confidenceScore(confidenceScore)
                .keyEvents(serializeObject(keyEvents))
                .characterChanges(serializeObject(characterChanges))
                .plotThreads(serializeObject(plotThreads))
                .aiModel("DeepSeek")
                .isAdopted(false)
                .build();
        
        projection = projectionRepository.save(projection);
        
        return buildProjectionResponse(projection, keyEvents, characterChanges, plotThreads);
    }
    
    /**
     * 获取所有建议
     */
    public List<PlotSuggestionResponse> getSuggestions(Long novelId, String status, String type) {
        List<PlotSuggestion> suggestions;
        
        if (status != null && !status.isEmpty()) {
            suggestions = suggestionRepository
                    .findByNovelIdAndStatusOrderByPriorityDescCreatedAtDesc(novelId, status);
        } else if (type != null && !type.isEmpty()) {
            suggestions = suggestionRepository
                    .findByNovelIdAndSuggestionTypeOrderByCreatedAtDesc(novelId, type);
        } else {
            suggestions = suggestionRepository
                    .findByNovelIdOrderByPriorityDescCreatedAtDesc(novelId);
        }
        
        return suggestions.stream()
                .map(this::toSuggestionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取推演历史
     */
    public List<PlotProjectionResponse> getProjections(Long novelId, String type) {
        List<PlotProjection> projections;
        
        if (type != null && !type.isEmpty()) {
            projections = projectionRepository
                    .findByNovelIdAndProjectionTypeOrderByCreatedAtDesc(novelId, type);
        } else {
            projections = projectionRepository
                    .findByNovelIdOrderByCreatedAtDesc(novelId);
        }
        
        return projections.stream()
                .map(this::toProjectionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取冲突列表
     */
    public List<ConflictTrackingResponse> getConflicts(Long novelId, String status) {
        List<ConflictTracking> conflicts;
        
        if (status != null && !status.isEmpty()) {
            conflicts = conflictRepository
                    .findByNovelIdAndStatusOrderByIntensityLevelDesc(novelId, status);
        } else {
            conflicts = conflictRepository
                    .findByNovelIdOrderByIntensityLevelDescCreatedAtDesc(novelId);
        }
        
        return conflicts.stream()
                .map(this::toConflictResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新建议状态
     */
    @Transactional
    public void updateSuggestionStatus(Long suggestionId, String status) {
        PlotSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("建议不存在"));
        
        suggestion.setStatus(status);
        if ("APPLIED".equals(status)) {
            suggestion.setAppliedAt(LocalDateTime.now());
        }
        suggestionRepository.save(suggestion);
    }
    
    /**
     * 采纳推演
     */
    @Transactional
    public void adoptProjection(Long projectionId) {
        PlotProjection projection = projectionRepository.findById(projectionId)
                .orElseThrow(() -> new RuntimeException("推演不存在"));
        
        projection.setIsAdopted(true);
        projection.setAdoptedAt(LocalDateTime.now());
        projectionRepository.save(projection);
    }
    
    // ========== 私有辅助方法 ==========
    
    private List<PlotSuggestion> analyzePlotHooks(Long novelId) {
        List<PlotSuggestion> suggestions = new ArrayList<>();
        
        // 查找未解决的伏笔
        List<PlotHook> pendingHooks = plotHookRepository
                .findByNovelIdAndStatusOrderByPlantedInChapterAsc(novelId, PlotHook.Status.PENDING);
        
        for (PlotHook hook : pendingHooks) {
            suggestions.add(PlotSuggestion.builder()
                    .novelId(novelId)
                    .suggestionType("FORESHADOWING")
                    .title("建议解决伏笔：" + hook.getTitle())
                    .description("该伏笔已埋下，建议在后续章节中展开或揭示")
                    .reasoning("伏笔埋下时间过长，需要适时回收")
                    .priority("HIGH")
                    .impactScore(80)
                    .relatedPlotHookId(hook.getId())
                    .status("PENDING")
                    .build());
        }
        
        return suggestions;
    }
    
    private List<PlotSuggestion> analyzeCharacterArcs(Long novelId) {
        List<PlotSuggestion> suggestions = new ArrayList<>();
        // TODO: 实现角色弧光分析
        return suggestions;
    }
    
    private List<PlotSuggestion> analyzeConflictEscalation(Long novelId) {
        List<PlotSuggestion> suggestions = new ArrayList<>();
        // TODO: 实现冲突升级分析
        return suggestions;
    }
    
    private List<PlotSuggestion> analyzePacing(Long novelId) {
        List<PlotSuggestion> suggestions = new ArrayList<>();
        // TODO: 实现节奏分析
        return suggestions;
    }
    
    private String buildProjectionContext(PlotProjectionRequest request) {
        StringBuilder context = new StringBuilder();
        context.append("当前章节ID: ").append(request.getCurrentChapterId()).append("\n");
        context.append("推演类型: ").append(request.getProjectionType()).append("\n");
        context.append("推演章节数: ").append(request.getChaptersAhead()).append("\n");
        return context.toString();
    }
    
    private String projectWithAI(String context, PlotProjectionRequest request) {
        // TODO: 集成实际AI服务
        return "基于当前情节，接下来可能的发展方向包括：\n1. 主角面临新的挑战\n2. 次要角色的成长\n3. 伏笔的逐步揭示";
    }
    
    private List<PlotProjectionResponse.KeyEvent> extractKeyEvents(String projectionText) {
        List<PlotProjectionResponse.KeyEvent> events = new ArrayList<>();
        // TODO: 实现事件提取
        return events;
    }
    
    private Map<String, String> analyzeCharacterChanges(PlotProjectionRequest request, String projectionText) {
        Map<String, String> changes = new HashMap<>();
        // TODO: 实现角色变化分析
        return changes;
    }
    
    private List<PlotProjectionResponse.PlotThread> identifyPlotThreads(String projectionText) {
        List<PlotProjectionResponse.PlotThread> threads = new ArrayList<>();
        // TODO: 实现情节线识别
        return threads;
    }
    
    private int calculateProjectionConfidence(PlotProjectionRequest request, String projectionText) {
        // TODO: 实现置信度计算
        return 75;
    }
    
    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            return "{}";
        }
    }
    
    private PlotSuggestionResponse toSuggestionResponse(PlotSuggestion suggestion) {
        return PlotSuggestionResponse.builder()
                .id(suggestion.getId())
                .novelId(suggestion.getNovelId())
                .chapterId(suggestion.getChapterId())
                .suggestionType(suggestion.getSuggestionType())
                .title(suggestion.getTitle())
                .description(suggestion.getDescription())
                .reasoning(suggestion.getReasoning())
                .priority(suggestion.getPriority())
                .impactScore(suggestion.getImpactScore())
                .relatedPlotHookId(suggestion.getRelatedPlotHookId())
                .relatedCharacterId(suggestion.getRelatedCharacterId())
                .status(suggestion.getStatus())
                .appliedChapterId(suggestion.getAppliedChapterId())
                .appliedAt(suggestion.getAppliedAt())
                .createdAt(suggestion.getCreatedAt())
                .build();
    }
    
    private PlotProjectionResponse buildProjectionResponse(
            PlotProjection projection,
            List<PlotProjectionResponse.KeyEvent> keyEvents,
            Map<String, String> characterChanges,
            List<PlotProjectionResponse.PlotThread> plotThreads) {
        
        return PlotProjectionResponse.builder()
                .id(projection.getId())
                .novelId(projection.getNovelId())
                .currentChapterId(projection.getCurrentChapterId())
                .projectionText(projection.getProjectionText())
                .projectionType(projection.getProjectionType())
                .chaptersAhead(projection.getChaptersAhead())
                .confidenceScore(projection.getConfidenceScore())
                .keyEvents(keyEvents)
                .characterChanges(characterChanges)
                .plotThreads(plotThreads)
                .isAdopted(projection.getIsAdopted())
                .createdAt(projection.getCreatedAt())
                .build();
    }
    
    private PlotProjectionResponse toProjectionResponse(PlotProjection projection) {
        // 反序列化JSON字段
        List<PlotProjectionResponse.KeyEvent> keyEvents = new ArrayList<>();
        Map<String, String> characterChanges = new HashMap<>();
        List<PlotProjectionResponse.PlotThread> plotThreads = new ArrayList<>();
        
        return buildProjectionResponse(projection, keyEvents, characterChanges, plotThreads);
    }
    
    private ConflictTrackingResponse toConflictResponse(ConflictTracking conflict) {
        return ConflictTrackingResponse.builder()
                .id(conflict.getId())
                .novelId(conflict.getNovelId())
                .conflictType(conflict.getConflictType())
                .title(conflict.getTitle())
                .description(conflict.getDescription())
                .involvedCharacters(new ArrayList<>())
                .intensityLevel(conflict.getIntensityLevel())
                .introducedChapterId(conflict.getIntroducedChapterId())
                .escalationPoints(new ArrayList<>())
                .resolutionChapterId(conflict.getResolutionChapterId())
                .status(conflict.getStatus())
                .resolutionType(conflict.getResolutionType())
                .createdAt(conflict.getCreatedAt())
                .build();
    }
}
