package com.aiwriter.service;

import com.aiwriter.dto.ContinuationSuggestionResponse;
import com.aiwriter.dto.SuggestionGenerateRequest;
import com.aiwriter.entity.ContentAnalysis;
import com.aiwriter.entity.ContinuationSuggestion;
import com.aiwriter.entity.Novel;
import com.aiwriter.repository.ContentAnalysisRepository;
import com.aiwriter.repository.ContinuationSuggestionRepository;
import com.aiwriter.repository.NovelRepository;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 续写建议服务
 * 核心功能：生成多个续写方向推荐，每个方向包含故事发展和影响分析
 */
@Service
public class ContinuationSuggestionService {
    
    @Autowired
    private ContinuationSuggestionRepository suggestionRepository;
    
    @Autowired
    private ContentAnalysisRepository analysisRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private AiService aiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成续写建议
     */
    @Transactional
    public List<ContinuationSuggestionResponse> generateSuggestions(SuggestionGenerateRequest request) {
        // 验证小说存在
        Novel novel = novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        // 获取最新的内容分析
        ContentAnalysis analysis = null;
        if (request.getAnalysisId() != null) {
            analysis = analysisRepository.findById(request.getAnalysisId()).orElse(null);
        } else {
            analysis = analysisRepository.findLatestByNovelId(request.getNovelId()).orElse(null);
        }
        
        // 调用AI生成续写建议
        String suggestionsJson = performAiSuggestionGeneration(novel, analysis, request.getCount());
        
        // 解析并保存建议
        List<ContinuationSuggestion> suggestions = parseSuggestions(
            suggestionsJson, 
            request.getNovelId(), 
            analysis != null ? analysis.getId() : null,
            request.getExpectedWordCount()
        );
        
        // 批量保存
        suggestions = suggestionRepository.saveAll(suggestions);
        
        // 转换为响应DTO
        return suggestions.stream()
            .map(this::convertToResponse)
            .toList();
    }
    
    /**
     * 执行AI续写建议生成
     */
    private String performAiSuggestionGeneration(Novel novel, ContentAnalysis analysis, int count) {
        String systemPrompt = buildSuggestionSystemPrompt();
        String userPrompt = buildSuggestionUserPrompt(novel, analysis, count);
        
        return aiService.chatJson(systemPrompt, userPrompt);
    }
    
    /**
     * 构建续写建议系统提示词
     */
    private String buildSuggestionSystemPrompt() {
        return """
            你是一个经验丰富的小说策划专家，擅长为故事提供多样化的发展方向。
            
            你的任务是根据当前故事情况，提供多个续写方向建议。每个建议需要包含：
            1. 标题：简洁有吸引力的方向名称
            2. 描述：该方向的基本概要
            3. 故事发展：详细说明这个方向可能的情节展开
            4. 影响：分析这个方向对整体故事的影响
            5. 情节走向：上升（高潮）/下降（铺垫）/转折
            6. 涉及角色：会重点涉及哪些角色
            7. 难度系数：1-5，表示执行这个方向的难度
            8. 优先级：1-10，推荐程度
            
            请以JSON数组格式返回，格式如下：
            [
                {
                    "title": "方向标题",
                    "description": "简要描述",
                    "story_development": "详细的故事发展说明",
                    "impact": "对故事的影响分析",
                    "plot_direction": "上升/下降/转折",
                    "involved_characters": "角色1,角色2",
                    "difficulty_level": 3,
                    "priority": 8
                }
            ]
            
            确保建议多样化，覆盖不同的情节发展可能性。
            """;
    }
    
    /**
     * 构建续写建议用户提示词
     */
    private String buildSuggestionUserPrompt(Novel novel, ContentAnalysis analysis, int count) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请为以下小说生成 ").append(count).append(" 个续写方向建议：\n\n");
        
        prompt.append("=== 小说基本信息 ===\n");
        prompt.append("标题：").append(novel.getTitle()).append("\n");
        prompt.append("类型：").append(novel.getGenre() != null ? novel.getGenre() : "未知").append("\n");
        prompt.append("目标读者：").append(novel.getTargetAudience() != null ? novel.getTargetAudience() : "通用").append("\n");
        if (novel.getWritingStyle() != null) {
            prompt.append("写作风格：").append(novel.getWritingStyle()).append("\n");
        }
        prompt.append("当前章节数：").append(novel.getTotalChapters()).append("\n");
        prompt.append("总字数：").append(novel.getTotalWords()).append("\n\n");
        
        if (analysis != null) {
            prompt.append("=== 当前故事状态 ===\n");
            
            if (analysis.getProtagonistName() != null) {
                prompt.append("主角：").append(analysis.getProtagonistName()).append("\n");
            }
            
            if (analysis.getCurrentScene() != null) {
                prompt.append("当前场景：").append(analysis.getCurrentScene()).append("\n");
            }
            
            if (analysis.getSceneLocation() != null) {
                prompt.append("场景位置：").append(analysis.getSceneLocation()).append("\n");
            }
            
            if (analysis.getSceneAtmosphere() != null) {
                prompt.append("场景氛围：").append(analysis.getSceneAtmosphere()).append("\n");
            }
            
            if (analysis.getCurrentConflict() != null) {
                prompt.append("当前冲突：").append(analysis.getCurrentConflict()).append("\n");
            }
            
            if (analysis.getEmotionalTone() != null) {
                prompt.append("情感基调：").append(analysis.getEmotionalTone()).append("\n");
            }
            
            if (analysis.getPlotPace() != null) {
                prompt.append("情节节奏：").append(analysis.getPlotPace()).append("\n");
            }
            
            // 添加角色信息
            List<ContentAnalysis.ExtractedCharacter> characters = analysis.getExtractedCharactersList();
            if (!characters.isEmpty()) {
                prompt.append("\n已知角色：\n");
                for (ContentAnalysis.ExtractedCharacter character : characters) {
                    prompt.append("- ").append(character.getName());
                    if (character.getRole() != null) {
                        prompt.append("（").append(character.getRole()).append("）");
                    }
                    if (character.getTraits() != null) {
                        prompt.append("：").append(character.getTraits());
                    }
                    prompt.append("\n");
                }
            }
            
            prompt.append("\n内容片段：\n");
            prompt.append(analysis.getContentSnippet()).append("\n");
        }
        
        prompt.append("\n请基于以上信息，提供 ").append(count).append(" 个富有创意且符合故事逻辑的续写方向。");
        prompt.append("每个方向应该是独特的，覆盖不同的情节发展可能性。\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析AI生成的续写建议
     */
    private List<ContinuationSuggestion> parseSuggestions(
            String jsonResult, 
            Long novelId, 
            Long analysisId,
            Integer expectedWordCount) {
        
        List<ContinuationSuggestion> suggestions = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResult);
            
            if (root.isArray()) {
                for (JsonNode node : root) {
                    ContinuationSuggestion suggestion = new ContinuationSuggestion();
                    suggestion.setNovelId(novelId);
                    suggestion.setAnalysisId(analysisId);
                    
                    if (node.has("title")) {
                        suggestion.setTitle(node.get("title").asText());
                    }
                    
                    if (node.has("description")) {
                        suggestion.setDescription(node.get("description").asText());
                    }
                    
                    if (node.has("story_development")) {
                        suggestion.setStoryDevelopment(node.get("story_development").asText());
                    }
                    
                    if (node.has("impact")) {
                        suggestion.setImpact(node.get("impact").asText());
                    }
                    
                    if (node.has("plot_direction")) {
                        suggestion.setPlotDirection(node.get("plot_direction").asText());
                    }
                    
                    if (node.has("involved_characters")) {
                        suggestion.setInvolvedCharacters(node.get("involved_characters").asText());
                    }
                    
                    if (node.has("difficulty_level")) {
                        suggestion.setDifficultyLevel(node.get("difficulty_level").asInt());
                    }
                    
                    if (node.has("priority")) {
                        suggestion.setPriority(node.get("priority").asInt());
                    } else {
                        suggestion.setPriority(5); // 默认优先级
                    }
                    
                    suggestion.setExpectedWordCount(expectedWordCount);
                    suggestion.setIsAdopted(false);
                    
                    suggestions.add(suggestion);
                }
            }
        } catch (Exception e) {
            // TODO: log.error("解析续写建议失败", e);
            throw new RuntimeException("解析续写建议失败: " + e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * 转换为响应DTO
     */
    private ContinuationSuggestionResponse convertToResponse(ContinuationSuggestion suggestion) {
        ContinuationSuggestionResponse response = new ContinuationSuggestionResponse();
        response.setId(suggestion.getId());
        response.setNovelId(suggestion.getNovelId());
        response.setAnalysisId(suggestion.getAnalysisId());
        response.setTitle(suggestion.getTitle());
        response.setDescription(suggestion.getDescription());
        response.setStoryDevelopment(suggestion.getStoryDevelopment());
        response.setImpact(suggestion.getImpact());
        response.setPlotDirection(suggestion.getPlotDirection());
        response.setInvolvedCharacters(suggestion.getInvolvedCharacters());
        response.setExpectedWordCount(suggestion.getExpectedWordCount());
        response.setDifficultyLevel(suggestion.getDifficultyLevel());
        response.setPriority(suggestion.getPriority());
        response.setIsAdopted(suggestion.getIsAdopted());
        response.setCreatedAt(suggestion.getCreatedAt());
        return response;
    }
    
    /**
     * 获取小说的未采用建议
     */
    public List<ContinuationSuggestionResponse> getUnadoptedSuggestions(Long novelId) {
        return suggestionRepository.findByNovelIdAndIsAdoptedFalseOrderByPriorityDesc(novelId)
            .stream()
            .map(this::convertToResponse)
            .toList();
    }
    
    /**
     * 采用某个续写建议
     */
    @Transactional
    public void adoptSuggestion(Long suggestionId, Long generatedChapterId) {
        ContinuationSuggestion suggestion = suggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new RuntimeException("续写建议不存在"));
        
        suggestion.setIsAdopted(true);
        suggestion.setGeneratedChapterId(generatedChapterId);
        suggestionRepository.save(suggestion);
    }
    
    /**
     * 获取建议详情
     */
    public ContinuationSuggestionResponse getSuggestion(Long id) {
        return suggestionRepository.findById(id)
            .map(this::convertToResponse)
            .orElse(null);
    }
}
