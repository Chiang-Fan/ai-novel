package com.aiwriter.service;

import com.aiwriter.dto.ChapterAnalysisDto;
import com.aiwriter.dto.ChapterAnalysisStatistics;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.ChapterAnalysisResult;
import com.aiwriter.entity.Novel;
import com.aiwriter.repository.ChapterAnalysisResultRepository;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.NovelRepository;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 章节深度分析服务
 */
@Slf4j
@Service
public class ChapterAnalyzerService {
    
    @Autowired
    private ChapterAnalysisResultRepository analysisRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private AiService aiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 深度分析章节
     */
    @Transactional
    public ChapterAnalysisDto analyzeChapter(Long chapterId) {
        long startTime = System.currentTimeMillis();
        
        // 获取章节
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new RuntimeException("章节不存在: " + chapterId));
        
        // 获取小说
        Novel novel = novelRepository.findById(chapter.getNovelId())
            .orElseThrow(() -> new RuntimeException("小说不存在: " + chapter.getNovelId()));
        
        // 检查是否已有分析结果
        Optional<ChapterAnalysisResult> existing = analysisRepository.findByChapterId(chapterId);
        if (existing.isPresent()) {
            log.info("章节 {} 已有分析结果，返回已有结果", chapterId);
            return convertToDto(existing.get());
        }
        
        // 调用 AI 进行深度分析
        String analysisJson = performDeepAnalysis(chapter, novel);
        
        // 解析结果
        ChapterAnalysisResult result = parseAnalysisResult(analysisJson, chapter);
        result.setAnalysisDurationMs(System.currentTimeMillis() - startTime);
        
        // 保存
        result = analysisRepository.save(result);
        
        log.info("章节 {} 深度分析完成，耗时 {} ms", chapterId, result.getAnalysisDurationMs());
        
        return convertToDto(result);
    }
    
    /**
     * 执行 AI 深度分析
     */
    private String performDeepAnalysis(Chapter chapter, Novel novel) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(chapter, novel);
        
        return aiService.chatJson(systemPrompt, userPrompt);
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
            你是一个专业的小说分析专家，擅长深度分析章节结构、情节发展、角色弧光和叙事技巧。
            
            你的任务是对给定的章节进行全方位深度分析，包括：
            
            1. **情节点识别**：
               - INCITING_INCIDENT（引发事件）
               - RISING_ACTION（上升动作）
               - MIDPOINT（中点转折）
               - CLIMAX（高潮）
               - FALLING_ACTION（下降动作）
               - RESOLUTION（结局）
               识别每个情节点的位置、重要性和描述
            
            2. **冲突分析**：
               - 类型：INTERNAL（内心冲突）/ EXTERNAL（外部冲突）/ MIXED（混合）
               - 强度：1-10
               - 方向：ESCALATING（升级）/ DE_ESCALATING（降级）/ STABLE（稳定）
            
            3. **主题和母题**：
               - 识别章节中的主题（成长、爱情、复仇等）
               - 主题强度：1-10
               - 主题如何体现
            
            4. **角色弧光**：
               - 每个角色的发展类型：POSITIVE（正向）/ NEGATIVE（负向）/ FLAT（平坦）/ COMPLEX（复杂）
               - 成长评分：1-10
               - 发展描述
            
            5. **主角成长阶段**：
               - SETUP（设定）/ RISING（上升）/ MIDPOINT（中点）/ CLIMAX（高潮）/ RESOLUTION（解决）
            
            6. **节奏分析**：
               - 整体节奏：SLOW（慢）/ MEDIUM（中）/ FAST（快）/ VARIABLE（变化）
               - 动作占比、对话占比、描写占比、内心独白占比（各0-1）
            
            7. **情感曲线**：
               - 章节中不同位置的情感变化
               - 格式：[{position: 0.0, emotion: "平静", intensity: 3}, ...]
               - position: 0-1 表示章节进度
               - intensity: 1-10
            
            8. **叙事技巧**：
               - 使用的技巧：倒叙、插叙、伏笔、悬念、对比、铺垫等
               - 转折点数量
               - 悬念强度：1-10
            
            9. **世界观构建**：
               - 涉及的世界观元素（魔法体系、社会结构、历史背景等）
               - 完整度评分：1-10
            
            10. **质量评估**：
                - 整体质量：1-10
                - 可读性：1-10
                - 创意性：1-10
                - 逻辑连贯性：1-10
            
            11. **改进建议**：
                - 分类：情节/角色/节奏/风格/对话/描写
                - 具体建议
                - 优先级：1-10
            
            请以JSON格式返回结果：
            {
                "plot_points": [
                    {
                        "type": "INCITING_INCIDENT",
                        "description": "...",
                        "position": 0.15,
                        "importance": 8
                    }
                ],
                "conflict_type": "MIXED",
                "conflict_intensity": 7,
                "conflict_direction": "ESCALATING",
                "themes": ["成长", "友情"],
                "theme_intensity": 6,
                "theme_description": "...",
                "character_arcs": [
                    {
                        "name": "主角",
                        "arc_type": "POSITIVE",
                        "development": "...",
                        "growth_score": 7
                    }
                ],
                "protagonist_stage": "RISING",
                "pacing": "FAST",
                "action_ratio": 0.4,
                "dialogue_ratio": 0.3,
                "description_ratio": 0.2,
                "introspection_ratio": 0.1,
                "emotion_curve": [
                    {"position": 0.0, "emotion": "平静", "intensity": 3},
                    {"position": 0.5, "emotion": "紧张", "intensity": 7},
                    {"position": 1.0, "emotion": "释然", "intensity": 5}
                ],
                "emotion_trend": "FLUCTUATING",
                "narrative_techniques": ["伏笔", "悬念", "对比"],
                "turning_points_count": 2,
                "suspense_level": 7,
                "world_building_elements": ["魔法体系", "社会结构"],
                "world_building_score": 6,
                "quality_score": 7,
                "readability_score": 8,
                "creativity_score": 6,
                "coherence_score": 7,
                "improvement_suggestions": [
                    {
                        "category": "节奏",
                        "suggestion": "中段节奏略显拖沓，可以增加动作场景",
                        "priority": 6
                    }
                ]
            }
            
            确保返回有效的JSON格式。
            """;
    }
    
    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(Chapter chapter, Novel novel) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请深度分析以下章节：\n\n");
        prompt.append("=== 小说信息 ===\n");
        prompt.append("标题：").append(novel.getTitle()).append("\n");
        prompt.append("类型：").append(novel.getGenre() != null ? novel.getGenre() : "未知").append("\n");
        prompt.append("\n");
        
        prompt.append("=== 章节信息 ===\n");
        prompt.append("章节号：第").append(chapter.getChapterNumber()).append("章\n");
        prompt.append("标题：").append(chapter.getTitle()).append("\n");
        prompt.append("字数：").append(chapter.getWordCount()).append("\n");
        prompt.append("\n");
        
        prompt.append("=== 章节内容 ===\n");
        // 如果内容太长，取前 3000 字
        String content = chapter.getContent();
        if (content.length() > 3000) {
            prompt.append(content, 0, 3000).append("\n\n[内容过长已截断]\n");
        } else {
            prompt.append(content).append("\n");
        }
        
        prompt.append("\n请进行全方位深度分析，返回JSON格式。\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析 AI 分析结果
     */
    private ChapterAnalysisResult parseAnalysisResult(String jsonResult, Chapter chapter) {
        ChapterAnalysisResult result = new ChapterAnalysisResult();
        result.setNovelId(chapter.getNovelId());
        result.setChapterId(chapter.getId());
        result.setChapterNumber(chapter.getChapterNumber());
        
        try {
            JsonNode root = objectMapper.readTree(jsonResult);
            
            // 情节点
            if (root.has("plot_points")) {
                result.setPlotPoints(root.get("plot_points").toString());
            }
            if (root.has("conflict_type")) {
                result.setConflictType(root.get("conflict_type").asText());
            }
            if (root.has("conflict_intensity")) {
                result.setConflictIntensity(root.get("conflict_intensity").asInt());
            }
            if (root.has("conflict_direction")) {
                result.setConflictDirection(root.get("conflict_direction").asText());
            }
            
            // 主题
            if (root.has("themes")) {
                result.setThemes(root.get("themes").toString());
            }
            if (root.has("theme_intensity")) {
                result.setThemeIntensity(root.get("theme_intensity").asInt());
            }
            if (root.has("theme_description")) {
                result.setThemeDescription(root.get("theme_description").asText());
            }
            
            // 角色弧光
            if (root.has("character_arcs")) {
                result.setCharacterArcs(root.get("character_arcs").toString());
            }
            if (root.has("protagonist_stage")) {
                result.setProtagonistStage(root.get("protagonist_stage").asText());
            }
            
            // 节奏
            if (root.has("pacing")) {
                result.setPacing(root.get("pacing").asText());
            }
            if (root.has("action_ratio")) {
                result.setActionRatio(root.get("action_ratio").asDouble());
            }
            if (root.has("dialogue_ratio")) {
                result.setDialogueRatio(root.get("dialogue_ratio").asDouble());
            }
            if (root.has("description_ratio")) {
                result.setDescriptionRatio(root.get("description_ratio").asDouble());
            }
            if (root.has("introspection_ratio")) {
                result.setIntrospectionRatio(root.get("introspection_ratio").asDouble());
            }
            
            // 情感曲线
            if (root.has("emotion_curve")) {
                result.setEmotionCurve(root.get("emotion_curve").toString());
            }
            if (root.has("emotion_trend")) {
                result.setEmotionTrend(root.get("emotion_trend").asText());
            }
            
            // 叙事技巧
            if (root.has("narrative_techniques")) {
                result.setNarrativeTechniques(root.get("narrative_techniques").toString());
            }
            if (root.has("turning_points_count")) {
                result.setTurningPointsCount(root.get("turning_points_count").asInt());
            }
            if (root.has("suspense_level")) {
                result.setSuspenseLevel(root.get("suspense_level").asInt());
            }
            
            // 世界观
            if (root.has("world_building_elements")) {
                result.setWorldBuildingElements(root.get("world_building_elements").toString());
            }
            if (root.has("world_building_score")) {
                result.setWorldBuildingScore(root.get("world_building_score").asInt());
            }
            
            // 质量评估
            if (root.has("quality_score")) {
                result.setQualityScore(root.get("quality_score").asInt());
            }
            if (root.has("readability_score")) {
                result.setReadabilityScore(root.get("readability_score").asInt());
            }
            if (root.has("creativity_score")) {
                result.setCreativityScore(root.get("creativity_score").asInt());
            }
            if (root.has("coherence_score")) {
                result.setCoherenceScore(root.get("coherence_score").asInt());
            }
            
            // 改进建议
            if (root.has("improvement_suggestions")) {
                result.setImprovementSuggestions(root.get("improvement_suggestions").toString());
            }
            
        } catch (Exception e) {
            log.error("解析分析结果失败", e);
            // 设置默认值
            result.setQualityScore(5);
            result.setReadabilityScore(5);
            result.setCreativityScore(5);
            result.setCoherenceScore(5);
        }
        
        return result;
    }
    
    /**
     * 转换为 DTO
     */
    private ChapterAnalysisDto convertToDto(ChapterAnalysisResult result) {
        ChapterAnalysisDto dto = new ChapterAnalysisDto();
        dto.setId(result.getId());
        dto.setNovelId(result.getNovelId());
        dto.setChapterId(result.getChapterId());
        dto.setChapterNumber(result.getChapterNumber());
        
        // 解析并设置复杂字段
        dto.setPlotPoints(parsePlotPoints(result.getPlotPoints()));
        dto.setThemes(parseThemes(result.getThemes()));
        dto.setCharacterArcs(parseCharacterArcs(result.getCharacterArcs()));
        dto.setEmotionCurve(parseEmotionCurve(result.getEmotionCurve()));
        dto.setNarrativeTechniques(parseNarrativeTechniques(result.getNarrativeTechniques()));
        dto.setWorldBuildingElements(parseWorldBuildingElements(result.getWorldBuildingElements()));
        dto.setImprovementSuggestions(parseImprovementSuggestions(result.getImprovementSuggestions()));
        
        // 简单字段
        dto.setConflictType(result.getConflictType());
        dto.setConflictIntensity(result.getConflictIntensity());
        dto.setConflictDirection(result.getConflictDirection());
        dto.setThemeIntensity(result.getThemeIntensity());
        dto.setThemeDescription(result.getThemeDescription());
        dto.setProtagonistStage(result.getProtagonistStage());
        dto.setPacing(result.getPacing());
        dto.setActionRatio(result.getActionRatio());
        dto.setDialogueRatio(result.getDialogueRatio());
        dto.setDescriptionRatio(result.getDescriptionRatio());
        dto.setIntrospectionRatio(result.getIntrospectionRatio());
        dto.setEmotionTrend(result.getEmotionTrend());
        dto.setTurningPointsCount(result.getTurningPointsCount());
        dto.setSuspenseLevel(result.getSuspenseLevel());
        dto.setWorldBuildingScore(result.getWorldBuildingScore());
        dto.setQualityScore(result.getQualityScore());
        dto.setReadabilityScore(result.getReadabilityScore());
        dto.setCreativityScore(result.getCreativityScore());
        dto.setCoherenceScore(result.getCoherenceScore());
        dto.setAnalysisDurationMs(result.getAnalysisDurationMs());
        dto.setCreatedAt(result.getCreatedAt());
        
        return dto;
    }
    
    // 解析辅助方法
    private List<ChapterAnalysisDto.PlotPointDto> parsePlotPoints(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, ChapterAnalysisDto.PlotPointDto.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<String> parseThemes(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<ChapterAnalysisDto.CharacterArcDto> parseCharacterArcs(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, ChapterAnalysisDto.CharacterArcDto.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<ChapterAnalysisDto.EmotionPointDto> parseEmotionCurve(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, ChapterAnalysisDto.EmotionPointDto.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<String> parseNarrativeTechniques(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<String> parseWorldBuildingElements(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<ChapterAnalysisDto.ImprovementSuggestionDto> parseImprovementSuggestions(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, ChapterAnalysisDto.ImprovementSuggestionDto.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取章节分析
     */
    public ChapterAnalysisDto getChapterAnalysis(Long chapterId) {
        return analysisRepository.findByChapterId(chapterId)
            .map(this::convertToDto)
            .orElse(null);
    }
    
    /**
     * 获取小说所有章节分析
     */
    public List<ChapterAnalysisDto> getNovelAnalyses(Long novelId) {
        return analysisRepository.findByNovelIdOrderByChapterNumberAsc(novelId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取统计数据
     */
    public ChapterAnalysisStatistics getStatistics(Long novelId) {
        List<ChapterAnalysisResult> analyses = analysisRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        
        if (analyses.isEmpty()) {
            return null;
        }
        
        ChapterAnalysisStatistics stats = new ChapterAnalysisStatistics();
        stats.setNovelId(novelId);
        stats.setAnalyzedChapters(analyses.size());
        
        // 总章节数
        Integer maxChapter = chapterRepository.findMaxChapterNumber(novelId).orElse(0);
        stats.setTotalChapters(maxChapter);
        stats.setAnalysisProgress((double) analyses.size() / maxChapter);
        
        // 平均质量
        stats.setAvgQualityScore(avg(analyses, ChapterAnalysisResult::getQualityScore));
        stats.setAvgReadabilityScore(avg(analyses, ChapterAnalysisResult::getReadabilityScore));
        stats.setAvgCreativityScore(avg(analyses, ChapterAnalysisResult::getCreativityScore));
        stats.setAvgCoherenceScore(avg(analyses, ChapterAnalysisResult::getCoherenceScore));
        
        // 节奏分布
        Map<String, Long> pacingCounts = analyses.stream()
            .collect(Collectors.groupingBy(a -> a.getPacing() != null ? a.getPacing() : "UNKNOWN", Collectors.counting()));
        stats.setSlowPacedCount(pacingCounts.getOrDefault("SLOW", 0L).intValue());
        stats.setMediumPacedCount(pacingCounts.getOrDefault("MEDIUM", 0L).intValue());
        stats.setFastPacedCount(pacingCounts.getOrDefault("FAST", 0L).intValue());
        stats.setVariablePacedCount(pacingCounts.getOrDefault("VARIABLE", 0L).intValue());
        
        // 冲突类型
        Map<String, Long> conflictCounts = analyses.stream()
            .filter(a -> a.getConflictType() != null)
            .collect(Collectors.groupingBy(ChapterAnalysisResult::getConflictType, Collectors.counting()));
        stats.setInternalConflictCount(conflictCounts.getOrDefault("INTERNAL", 0L).intValue());
        stats.setExternalConflictCount(conflictCounts.getOrDefault("EXTERNAL", 0L).intValue());
        stats.setMixedConflictCount(conflictCounts.getOrDefault("MIXED", 0L).intValue());
        
        // 质量分布
        stats.setExcellentChapters((int) analyses.stream().filter(a -> a.getQualityScore() != null && a.getQualityScore() >= 8).count());
        stats.setGoodChapters((int) analyses.stream().filter(a -> a.getQualityScore() != null && a.getQualityScore() >= 6 && a.getQualityScore() < 8).count());
        stats.setAverageChapters((int) analyses.stream().filter(a -> a.getQualityScore() != null && a.getQualityScore() >= 4 && a.getQualityScore() < 6).count());
        stats.setPoorChapters((int) analyses.stream().filter(a -> a.getQualityScore() != null && a.getQualityScore() < 4).count());
        
        // 主题统计
        Set<String> allThemes = new HashSet<>();
        for (ChapterAnalysisResult analysis : analyses) {
            allThemes.addAll(analysis.getThemesList());
        }
        stats.setUniqueThemesCount(allThemes.size());
        
        // 叙事技巧
        stats.setAvgTurningPointsPerChapter(avg(analyses, ChapterAnalysisResult::getTurningPointsCount).intValue());
        stats.setAvgSuspenseLevel(avg(analyses, ChapterAnalysisResult::getSuspenseLevel));
        
        // 改进建议
        int totalSuggestions = 0;
        for (ChapterAnalysisResult analysis : analyses) {
            try {
                List<ChapterAnalysisResult.ImprovementSuggestion> suggestions = 
                    objectMapper.readValue(analysis.getImprovementSuggestions(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ChapterAnalysisResult.ImprovementSuggestion.class));
                totalSuggestions += suggestions.size();
            } catch (Exception e) {
                // ignore
            }
        }
        stats.setTotalImprovementSuggestions(totalSuggestions);
        
        return stats;
    }
    
    private Double avg(List<ChapterAnalysisResult> list, java.util.function.Function<ChapterAnalysisResult, Integer> mapper) {
        return list.stream()
            .map(mapper)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }
}
