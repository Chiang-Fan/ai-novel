package com.aiwriter.service;

import com.aiwriter.dto.ContentAnalysisRequest;
import com.aiwriter.dto.ContentAnalysisResponse;
import com.aiwriter.entity.ContentAnalysis;
import com.aiwriter.entity.Novel;
import com.aiwriter.repository.ContentAnalysisRepository;
import com.aiwriter.repository.NovelRepository;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容分析服务
 * 核心功能：智能分析小说片段，提取角色、场景、风格等信息
 */
@Slf4j
@Service
public class ContentAnalysisService {
    
    @Autowired
    private ContentAnalysisRepository analysisRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private AiService aiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 分析内容片段
     */
    @Transactional
    public ContentAnalysisResponse analyzeContent(ContentAnalysisRequest request) {
        // 验证小说存在
        Novel novel = novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        // 调用AI进行内容分析
        String analysisResult = performAiAnalysis(request.getContent(), novel, request.getDeepAnalysis());
        
        // 解析AI返回的JSON结果
        ContentAnalysis analysis = parseAnalysisResult(analysisResult, request);
        
        // 保存分析结果
        analysis = analysisRepository.save(analysis);
        
        // 转换为响应DTO
        return convertToResponse(analysis);
    }
    
    /**
     * 执行AI分析
     */
    private String performAiAnalysis(String content, Novel novel, Boolean deepAnalysis) {
        // 构建系统提示词
        String systemPrompt = buildAnalysisSystemPrompt();
        
        // 构建用户提示词
        String userPrompt = buildAnalysisUserPrompt(content, novel, deepAnalysis);
        
        // 调用AI
        return aiService.chatJson(systemPrompt, userPrompt);
    }
    
    /**
     * 构建分析系统提示词
     */
    private String buildAnalysisSystemPrompt() {
        return """
            你是一个专业的小说分析专家，擅长从文本中提取结构化信息，特别精通叙事结构和视角分析。
            
            【重要概念】关于主角层级的区分：
            1. **整体主角（Global Protagonist）**：贯穿全书始终的核心人物，是整部小说的真正主角
            2. **视角角色/临时主角（POV Character）**：某个章节或场景中的视角人物，可能是配角
            3. 临时主角的作用：通过不同视角推进主线剧情、揭示关键信息、制造戏剧冲突
            
            你的任务是分析给定的小说片段，并提取以下信息：
            1. 识别整体主角：判断谁是整本书的核心主角（如果能确定）
            2. 识别视角角色：判断当前章节由谁的视角展开（可能不是整体主角）
            3. 确定叙述视角：第一人称、第三人称限知、第三人称全知等
            4. 提取角色信息：列出所有出现的角色及其特征、重要性
            5. 分析场景：描述当前场景的位置、时间、氛围
            6. 检测写作风格：识别文本的写作风格特点（如幽默、严肃、诗意等）
            7. 情节分析：识别当前冲突、情节节奏、情感基调
            
            请以JSON格式返回结果，格式如下：
            {
                "protagonist": "整体主角名称（如果能确定）",
                "viewpoint_character": "当前章节视角角色名称",
                "is_global_protagonist_pov": true/false,
                "narrative_perspective": "第一人称/第三人称限知/第三人称全知",
                "characters": [
                    {
                        "name": "角色名",
                        "role": "整体主角/配角/龙套",
                        "traits": "性格特征",
                        "description": "角色描述",
                        "is_global_protagonist": true/false,
                        "importance_level": 8
                    }
                ],
                "scene": {
                    "description": "场景描述",
                    "location": "地点",
                    "time": "时间",
                    "atmosphere": "氛围"
                },
                "writing_style": {
                    "style": "风格类型",
                    "description": "风格描述",
                    "plot_pace": "节奏（快速/中等/缓慢）"
                },
                "conflict": "当前冲突描述",
                "emotional_tone": "情感基调（正面/负面/中性）",
                "foreshadowing": ["潜在伏笔1", "潜在伏笔2"],
                "completeness_score": 85
            }
            
            【分析要点】：
            - 如果当前章节使用非主角视角，请在viewpoint_character中明确标注
            - 通过出场频率、情节关键性、情感投入等判断角色的importance_level（1-10）
            - 区分"这个人物在当前章节是焦点"和"这个人物是整本书的主角"
            
            确保返回的是有效的JSON格式。
            """;
    }
    
    /**
     * 构建分析用户提示词
     */
    private String buildAnalysisUserPrompt(String content, Novel novel, Boolean deepAnalysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请分析以下小说片段：\n\n");
        prompt.append("=== 小说信息 ===\n");
        prompt.append("标题：").append(novel.getTitle()).append("\n");
        prompt.append("类型：").append(novel.getGenre() != null ? novel.getGenre() : "未知").append("\n");
        if (novel.getWritingStyle() != null) {
            prompt.append("预期风格：").append(novel.getWritingStyle()).append("\n");
        }
        prompt.append("\n");
        
        prompt.append("=== 待分析内容 ===\n");
        // 如果内容太长，只取前2000字
        String analyzedContent = content.length() > 2000 ? content.substring(0, 2000) + "..." : content;
        prompt.append(analyzedContent).append("\n\n");
        
        if (deepAnalysis) {
            prompt.append("请进行深度分析，包括角色关系和潜在伏笔。\n");
        } else {
            prompt.append("请进行基础分析，重点关注主角、场景和风格。\n");
        }
        
        return prompt.toString();
    }
    
    /**
     * 解析AI分析结果
     */
    private ContentAnalysis parseAnalysisResult(String jsonResult, ContentAnalysisRequest request) {
        ContentAnalysis analysis = new ContentAnalysis();
        analysis.setNovelId(request.getNovelId());
        analysis.setChapterId(request.getChapterId());
        
        // 保存内容摘要（最多500字符）
        String snippet = request.getContent().length() > 500 
            ? request.getContent().substring(0, 500) + "..." 
            : request.getContent();
        analysis.setContentSnippet(snippet);
        
        try {
            JsonNode root = objectMapper.readTree(jsonResult);
            
            // 提取整体主角
            if (root.has("protagonist")) {
                analysis.setProtagonistName(root.get("protagonist").asText());
            }
            
            // 提取视角角色（临时主角）
            if (root.has("viewpoint_character")) {
                analysis.setViewpointCharacter(root.get("viewpoint_character").asText());
            }
            
            // 是否使用整体主角视角
            if (root.has("is_global_protagonist_pov")) {
                analysis.setIsGlobalProtagonistPov(root.get("is_global_protagonist_pov").asBoolean());
            }
            
            // 叙述视角
            if (root.has("narrative_perspective")) {
                analysis.setNarrativePerspective(root.get("narrative_perspective").asText());
            }
            
            // 提取角色列表
            if (root.has("characters")) {
                analysis.setExtractedCharacters(root.get("characters").toString());
            }
            
            // 提取场景信息
            if (root.has("scene")) {
                JsonNode scene = root.get("scene");
                if (scene.has("description")) {
                    analysis.setCurrentScene(scene.get("description").asText());
                }
                if (scene.has("location")) {
                    analysis.setSceneLocation(scene.get("location").asText());
                }
                if (scene.has("time")) {
                    analysis.setSceneTime(scene.get("time").asText());
                }
                if (scene.has("atmosphere")) {
                    analysis.setSceneAtmosphere(scene.get("atmosphere").asText());
                }
            }
            
            // 提取写作风格
            if (root.has("writing_style")) {
                JsonNode style = root.get("writing_style");
                if (style.has("style")) {
                    analysis.setWritingStyle(style.get("style").asText());
                }
                if (style.has("description")) {
                    analysis.setStyleDescription(style.get("description").asText());
                }
                if (style.has("plot_pace")) {
                    analysis.setPlotPace(style.get("plot_pace").asText());
                }
            }
            
            // 提取冲突
            if (root.has("conflict")) {
                analysis.setCurrentConflict(root.get("conflict").asText());
            }
            
            // 提取情感基调
            if (root.has("emotional_tone")) {
                analysis.setEmotionalTone(root.get("emotional_tone").asText());
            }
            
            // 提取伏笔
            if (root.has("foreshadowing")) {
                analysis.setPotentialForeshadowing(root.get("foreshadowing").toString());
            }
            
            // 完整性得分
            if (root.has("completeness_score")) {
                analysis.setCompletenessScore(root.get("completeness_score").asInt());
            } else {
                analysis.setCompletenessScore(75); // 默认得分
            }
            
        } catch (Exception e) {
            // 解析失败时使用基础信息
            analysis.setCompletenessScore(50);
            // TODO: log.error("解析AI分析结果失败", e);
        }
        
        return analysis;
    }
    
    /**
     * 转换为响应DTO
     */
    private ContentAnalysisResponse convertToResponse(ContentAnalysis analysis) {
        ContentAnalysisResponse response = new ContentAnalysisResponse();
        response.setId(analysis.getId());
        response.setNovelId(analysis.getNovelId());
        response.setChapterId(analysis.getChapterId());
        response.setContentSnippet(analysis.getContentSnippet());
        response.setProtagonistName(analysis.getProtagonistName());
        response.setViewpointCharacter(analysis.getViewpointCharacter());
        response.setIsGlobalProtagonistPov(analysis.getIsGlobalProtagonistPov());
        response.setNarrativePerspective(analysis.getNarrativePerspective());
        response.setCurrentConflict(analysis.getCurrentConflict());
        response.setEmotionalTone(analysis.getEmotionalTone());
        response.setCompletenessScore(analysis.getCompletenessScore());
        response.setCreatedAt(analysis.getCreatedAt());
        
        // 转换角色列表
        List<ContentAnalysisResponse.ExtractedCharacterDTO> characters = new ArrayList<>();
        for (ContentAnalysis.ExtractedCharacter ec : analysis.getExtractedCharactersList()) {
            ContentAnalysisResponse.ExtractedCharacterDTO dto = 
                new ContentAnalysisResponse.ExtractedCharacterDTO();
            dto.setName(ec.getName());
            dto.setRole(ec.getRole());
            dto.setTraits(ec.getTraits());
            dto.setDescription(ec.getDescription());
            characters.add(dto);
        }
        response.setExtractedCharacters(characters);
        
        // 转换场景信息
        ContentAnalysisResponse.SceneInfoDTO sceneInfo = new ContentAnalysisResponse.SceneInfoDTO();
        sceneInfo.setDescription(analysis.getCurrentScene());
        sceneInfo.setLocation(analysis.getSceneLocation());
        sceneInfo.setTime(analysis.getSceneTime());
        sceneInfo.setAtmosphere(analysis.getSceneAtmosphere());
        response.setSceneInfo(sceneInfo);
        
        // 转换风格信息
        ContentAnalysisResponse.WritingStyleDTO styleInfo = new ContentAnalysisResponse.WritingStyleDTO();
        styleInfo.setStyle(analysis.getWritingStyle());
        styleInfo.setDescription(analysis.getStyleDescription());
        styleInfo.setPlotPace(analysis.getPlotPace());
        response.setStyleInfo(styleInfo);
        
        return response;
    }
    
    /**
     * 获取小说的最新分析
     */
    public ContentAnalysisResponse getLatestAnalysis(Long novelId) {
        return analysisRepository.findLatestByNovelId(novelId)
            .map(this::convertToResponse)
            .orElse(null);
    }
    
    /**
     * 获取小说的所有分析历史
     */
    public List<ContentAnalysisResponse> getAnalysisHistory(Long novelId) {
        return analysisRepository.findByNovelIdOrderByCreatedAtDesc(novelId)
            .stream()
            .map(this::convertToResponse)
            .toList();
    }
    
    /**
     * 获取章节的分析
     */
    public ContentAnalysisResponse getChapterAnalysis(Long chapterId) {
        return analysisRepository.findByChapterId(chapterId)
            .map(this::convertToResponse)
            .orElse(null);
    }
    
    /**
     * 更新分析记录的章节ID
     */
    @Transactional
    public void updateChapterId(Long analysisId, Long chapterId) {
        ContentAnalysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new RuntimeException("分析记录不存在: " + analysisId));
        analysis.setChapterId(chapterId);
        analysisRepository.save(analysis);
        log.info("更新分析记录章节ID: analysisId={}, chapterId={}", analysisId, chapterId);
    }
    
    /**
     * 获取小说的所有分析记录（实体列表）
     */
    public List<ContentAnalysis> getAnalysisByNovel(Long novelId) {
        return analysisRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
    }
}
