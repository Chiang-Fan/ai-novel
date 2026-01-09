package com.aiwriter.service;

import com.aiwriter.dto.NovelCreationRequest;
import com.aiwriter.dto.NovelCreationResponse;
import com.aiwriter.entity.Novel;
import com.aiwriter.entity.Outline;
import com.aiwriter.entity.Character;
import com.aiwriter.entity.WorldSetting;
import com.aiwriter.repository.WorldSettingRepository;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 智能新建小说服务
 * 通过AI与用户交互来完善小说框架
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartNovelCreationService {
    
    private final AiService aiService;
    private final NovelService novelService;
    private final OutlineService outlineService;
    private final CharacterService characterService;
    private final WorldSettingService worldSettingService;
    private final WorldSettingRepository worldSettingRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储用户创建会话的上下文
    private final Map<String, Map<String, Object>> sessionContexts = new HashMap<>();
    
    /**
     * 开始智能创建会话
     */
    @Transactional
    public NovelCreationResponse startCreation(NovelCreationRequest request, String sessionId) {
        // 如果没有提供sessionId，则生成一个
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }
        
        // 初始化会话上下文
        Map<String, Object> context = new HashMap<>();
        context.put("initialIdea", request.getInitialIdea());
        context.put("sessionId", sessionId);
        context.put("currentStage", "IDEA");
        context.put("novelInfo", new HashMap<>());
        context.put("genre", request.getGenre());
        context.put("targetAudience", request.getTargetAudience());
        context.put("writingStyle", request.getWritingStyle());
        
        sessionContexts.put(sessionId, context);
        
        return askForGenre(request.getInitialIdea(), sessionId);
    }
    
    /**
     * 处理用户回复并推进创建流程
     */
    @Transactional
    public NovelCreationResponse processReply(NovelCreationRequest request, String sessionId) {
        Map<String, Object> context = sessionContexts.get(sessionId);
        if (context == null) {
            throw new RuntimeException("会话不存在，请重新开始创建");
        }
        
        String currentStage = (String) context.get("currentStage");
        
        switch (currentStage) {
            case "IDEA":
                return processIdeaReply(request, sessionId);
            case "GENRE":
                return processGenreReply(request, sessionId);
            case "OUTLINE":
                return processOutlineReply(request, sessionId);
            case "CHARACTERS":
                return processCharacterReply(request, sessionId);
            case "WORLD_SETTING":
                return processWorldSettingReply(request, sessionId);
            default:
                return startCreation(request, sessionId);
        }
    }
    
    /**
     * 获取会话状态
     */
    public NovelCreationResponse getSessionStatus(String sessionId) {
        Map<String, Object> context = sessionContexts.get(sessionId);
        if (context == null) {
            throw new RuntimeException("会话不存在");
        }
        
        NovelCreationResponse response = new NovelCreationResponse();
        response.setCurrentStage((String) context.get("currentStage"));
        response.setIsComplete(false);
        response.setContext(context);
        
        // 根据当前阶段提供相应的问题
        String currentStage = (String) context.get("currentStage");
        switch (currentStage) {
            case "IDEA":
                response.setQuestion("请描述您的小说创意或大致方向：");
                break;
            case "GENRE":
                response.setQuestion("请选择或描述您想要的小说类型：");
                break;
            case "OUTLINE":
                response.setQuestion("请确认或调整故事大纲：");
                break;
            case "CHARACTERS":
                response.setQuestion("请确认或调整角色设定：");
                break;
            case "WORLD_SETTING":
                response.setQuestion("请确认或调整世界观设定：");
                break;
            default:
                response.setQuestion("会话进行中...");
                break;
        }
        
        return response;
    }
    
    /**
     * 重置会话
     */
    public void resetSession(String sessionId) {
        sessionContexts.remove(sessionId);
    }
    
    /**
     * 询问小说创意
     */
    private NovelCreationResponse askForIdea(String initialIdea, String sessionId) {
        NovelCreationResponse response = new NovelCreationResponse();
        response.setQuestion("请描述您的小说创意或大致方向：");
        response.setSuggestions(Arrays.asList(
            "奇幻冒险类：如魔法世界、龙与地下城",
            "现代都市：如职场、爱情、悬疑",
            "历史架空：如古代背景、历史人物",
            "科幻未来：如太空探索、人工智能"
        ));
        response.setCurrentStage("IDEA");
        response.setIsComplete(false);
        
        updateContext(sessionId, "currentStage", "IDEA");
        return response;
    }
    
    /**
     * 询问小说类型
     */
    private NovelCreationResponse askForGenre(String initialIdea, String sessionId) {
        String systemPrompt = "你是一位专业的小说策划专家，擅长根据用户提供的创意方向推荐合适的小说类型。";
        String userPrompt = String.format(
            "用户提供的创意方向：%s\n\n请推荐3-5个最适合的小说类型，并为每个类型提供简短的说明。", 
            initialIdea
        );
        
        String aiResponse = aiService.chat(systemPrompt, userPrompt);
        
        NovelCreationResponse response = new NovelCreationResponse();
        response.setQuestion("请选择或描述您想要的小说类型：");
        response.setSuggestions(extractSuggestions(aiResponse));
        response.setCurrentStage("GENRE");
        response.setIsComplete(false);
        
        updateContext(sessionId, "currentStage", "GENRE");
        return response;
    }
    
    /**
     * 询问大纲
     */
    private NovelCreationResponse askForOutline(String sessionId) {
        Map<String, Object> context = sessionContexts.get(sessionId);
        String novelIdea = (String) context.get("initialIdea");
        String genre = (String) context.get("genre");
        
        String systemPrompt = "你是一位专业的小说大纲策划师，擅长为小说创作完整的故事大纲。";
        String userPrompt = String.format(
            "小说创意：%s\n小说类型：%s\n\n请为这部小说提供一个三幕式结构的大纲，包含主要情节点、转折和高潮。", 
            novelIdea, genre
        );
        
        String aiResponse = aiService.chat(systemPrompt, userPrompt);
        
        NovelCreationResponse response = new NovelCreationResponse();
        response.setQuestion("请确认或调整以下故事大纲：");
        response.setSuggestions(extractSuggestions(aiResponse));
        response.setCurrentStage("OUTLINE");
        response.setIsComplete(false);
        
        updateContext(sessionId, "outline", aiResponse);
        updateContext(sessionId, "currentStage", "OUTLINE");
        return response;
    }
    
    /**
     * 询问角色设定
     */
    private NovelCreationResponse askForCharacters(String sessionId) {
        Map<String, Object> context = sessionContexts.get(sessionId);
        String novelIdea = (String) context.get("initialIdea");
        String outline = (String) context.get("outline");
        
        String systemPrompt = "你是一位专业的角色设计师，擅长根据故事背景设计符合情节的角色。";
        String userPrompt = String.format(
            "小说创意：%s\n故事大纲：%s\n\n请推荐3-5个关键角色（包括主角、反派、重要配角），并简要描述他们的性格、背景和作用。", 
            novelIdea, outline
        );
        
        String aiResponse = aiService.chat(systemPrompt, userPrompt);
        
        NovelCreationResponse response = new NovelCreationResponse();
        response.setQuestion("请确认或调整以下角色设定：");
        response.setSuggestions(extractSuggestions(aiResponse));
        response.setCurrentStage("CHARACTERS");
        response.setIsComplete(false);
        
        updateContext(sessionId, "characters", aiResponse);
        updateContext(sessionId, "currentStage", "CHARACTERS");
        return response;
    }
    
    /**
     * 询问世界观设定
     */
    private NovelCreationResponse askForWorldSetting(String sessionId) {
        Map<String, Object> context = sessionContexts.get(sessionId);
        String novelIdea = (String) context.get("initialIdea");
        String genre = (String) context.get("genre");
        String outline = (String) context.get("outline");
        
        String systemPrompt = "你是一位世界观构建专家，擅长为小说创建完整的世界观设定。";
        String userPrompt = String.format(
            "小说创意：%s\n小说类型：%s\n故事大纲：%s\n\n请提供世界观设定，包括时间背景、地点环境、社会结构、特殊规则等。", 
            novelIdea, genre, outline
        );
        
        String aiResponse = aiService.chat(systemPrompt, userPrompt);
        
        NovelCreationResponse response = new NovelCreationResponse();
        response.setQuestion("请确认或调整以下世界观设定：");
        response.setSuggestions(extractSuggestions(aiResponse));
        response.setCurrentStage("WORLD_SETTING");
        response.setIsComplete(false);
        
        updateContext(sessionId, "worldSetting", aiResponse);
        updateContext(sessionId, "currentStage", "WORLD_SETTING");
        return response;
    }
    
    /**
     * 完成小说创建
     */
    private NovelCreationResponse completeNovelCreation(String sessionId) {
        Map<String, Object> context = sessionContexts.get(sessionId);
        
        // 创建小说请求对象
        com.aiwriter.dto.NovelCreateRequest novelCreateRequest = new com.aiwriter.dto.NovelCreateRequest();
        novelCreateRequest.setTitle(generateTitle((String) context.get("initialIdea")));
        novelCreateRequest.setGenre((String) context.get("genre"));
        novelCreateRequest.setDescription((String) context.get("initialIdea"));
        novelCreateRequest.setTargetAudience((String) context.get("targetAudience"));
        novelCreateRequest.setWritingStyle((String) context.get("writingStyle"));
        
        Novel savedNovel = novelService.createNovel(novelCreateRequest);
        
        // 存储大纲信息
        String outlineInfo = (String) context.get("outline");
        if (outlineInfo != null && !outlineInfo.trim().isEmpty()) {
            createOutlinesFromInfo(savedNovel.getId(), outlineInfo);
        }
        
        // 存储角色信息
        String characterInfo = (String) context.get("characters");
        if (characterInfo != null && !characterInfo.trim().isEmpty()) {
            createCharactersFromInfo(savedNovel.getId(), characterInfo);
        }
        
        // 存储世界观信息
        String worldSettingInfo = (String) context.get("worldSetting");
        if (worldSettingInfo != null && !worldSettingInfo.trim().isEmpty()) {
            createWorldSettingFromInfo(savedNovel.getId(), worldSettingInfo);
        }
        
        NovelCreationResponse response = new NovelCreationResponse();
        response.setQuestion("小说创建完成！以下是您的小说信息：");
        response.setSuggestions(Arrays.asList(
            "标题：" + savedNovel.getTitle(),
            "类型：" + savedNovel.getGenre(),
            "简介：" + savedNovel.getDescription()
        ));
        response.setCurrentStage("COMPLETE");
        response.setIsComplete(true);
        response.setNovelId(savedNovel.getId());
        
        // 清理会话
        sessionContexts.remove(sessionId);
        
        return response;
    }
    
    /**
     * 处理创意回复
     */
    private NovelCreationResponse processIdeaReply(NovelCreationRequest request, String sessionId) {
        updateContext(sessionId, "initialIdea", request.getInitialIdea());
        
        return askForGenre(request.getInitialIdea(), sessionId);
    }
    
    /**
     * 处理类型回复
     */
    private NovelCreationResponse processGenreReply(NovelCreationRequest request, String sessionId) {
        updateContext(sessionId, "genre", request.getGenre() != null ? request.getGenre() : request.getInitialIdea());
        if (request.getTargetAudience() != null) {
            updateContext(sessionId, "targetAudience", request.getTargetAudience());
        }
        if (request.getWritingStyle() != null) {
            updateContext(sessionId, "writingStyle", request.getWritingStyle());
        }
        
        return askForOutline(sessionId);
    }
    
    /**
     * 处理大纲回复
     */
    private NovelCreationResponse processOutlineReply(NovelCreationRequest request, String sessionId) {
        updateContext(sessionId, "outline", request.getInitialIdea()); // 用户确认的大纲
        
        return askForCharacters(sessionId);
    }
    
    /**
     * 处理角色回复
     */
    private NovelCreationResponse processCharacterReply(NovelCreationRequest request, String sessionId) {
        updateContext(sessionId, "characters", request.getInitialIdea()); // 用户确认的角色信息
        
        return askForWorldSetting(sessionId);
    }
    
    /**
     * 处理世界观回复
     */
    private NovelCreationResponse processWorldSettingReply(NovelCreationRequest request, String sessionId) {
        updateContext(sessionId, "worldSetting", request.getInitialIdea()); // 用户确认的世界观信息
        
        return completeNovelCreation(sessionId);
    }
    
    /**
     * 更新会话上下文
     */
    private void updateContext(String sessionId, String key, Object value) {
        Map<String, Object> context = sessionContexts.get(sessionId);
        if (context != null) {
            context.put(key, value);
        }
    }
    
    /**
     * 从AI响应中提取建议
     */
    private List<String> extractSuggestions(String aiResponse) {
        // 简单的提取方法，实际可以更复杂
        List<String> suggestions = new ArrayList<>();
        
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && (line.startsWith("-") || line.startsWith("1.") || 
                                   line.startsWith("2.") || line.startsWith("3.") || 
                                   line.startsWith("4.") || line.startsWith("5."))) {
                suggestions.add(line.replaceAll("^[\\-0-9.\\s]+", "").trim());
            }
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add(aiResponse.length() > 100 ? aiResponse.substring(0, 100) + "..." : aiResponse);
        }
        
        return suggestions;
    }
    
    /**
     * 生成标题
     */
    private String generateTitle(String idea) {
        String systemPrompt = "你是一位专业的标题策划师，擅长为小说创作吸引人的标题。";
        String userPrompt = String.format("根据以下创意：%s\n\n请提供一个简洁有吸引力的小说标题。", idea);
        
        String title = aiService.chat(systemPrompt, userPrompt);
        return title.replaceAll("[\n\r\"\"].*", "").trim(); // 清理标题
    }
    
    /**
     * 从AI生成的大纲信息创建大纲记录
     */
    private void createOutlinesFromInfo(Long novelId, String outlineInfo) {
        try {
            // 分割大纲信息并创建大纲记录
            String[] lines = outlineInfo.split("\n");
            int sequenceNumber = 1;
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("**") || line.startsWith("##")) {
                    continue; // 跳过格式标记
                }
                
                // 尝试提取章节标题和概要
                String title = line;
                String summary = "";
                
                // 简单解析格式："标题 - 概要" 或 "标题：概要"
                if (line.contains("-") && line.indexOf("-") > 0) {
                    int dashIndex = line.indexOf("-");
                    title = line.substring(0, dashIndex).trim();
                    summary = line.substring(dashIndex + 1).trim();
                } else if (line.contains("：")) {
                    int colonIndex = line.indexOf("：");
                    title = line.substring(0, colonIndex).trim();
                    summary = line.substring(colonIndex + 1).trim();
                }
                
                if (!title.isEmpty()) {
                    Outline outline = new Outline();
                    outline.setNovelId(novelId);
                    outline.setSequenceNumber(sequenceNumber++);
                    outline.setTitle(title);
                    outline.setSummary(summary);
                    outline.setNodeType("CHAPTER"); // 默认为章节类型
                    outline.setStatus("PLANNED");
                    
                    outlineService.createOutline(outline);
                }
            }
        } catch (Exception e) {
            log.error("创建大纲失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 从AI生成的角色信息创建角色记录
     */
    private void createCharactersFromInfo(Long novelId, String characterInfo) {
        try {
            // 分割角色信息并创建角色记录
            String[] paragraphs = characterInfo.split("\\n\\n|\n\n");
            
            for (String paragraph : paragraphs) {
                paragraph = paragraph.trim();
                if (paragraph.isEmpty()) {
                    continue;
                }
                
                // 解析角色信息
                String name = "";
                String roleType = "SUPPORTING"; // 默认为配角
                String personality = "";
                String background = "";
                String appearance = "";
                String abilities = "";
                
                // 简单解析格式，寻找关键词
                if (paragraph.contains("**身份**")) {
                    int start = paragraph.indexOf("**身份**") + 5;
                    int end = paragraph.indexOf("\n", start);
                    if (end == -1) end = paragraph.length();
                    name = paragraph.substring(start, end).replaceAll(":[^\\w\\u4e00-\\u9fa5]*", "").trim();
                } else if (paragraph.contains("**姓名**")) {
                    int start = paragraph.indexOf("**姓名**") + 5;
                    int end = paragraph.indexOf("\n", start);
                    if (end == -1) end = paragraph.length();
                    name = paragraph.substring(start, end).replaceAll(":[^\\w\\u4e00-\\u9fa5]*", "").trim();
                } else {
                    // 如果没有明确标识，尝试提取第一行作为名字
                    String[] lines = paragraph.split("\n");
                    if (lines.length > 0) {
                        name = lines[0].replaceAll("^[*-\\s]+|\\s*$", "");
                        // 去掉可能的标题标记
                        if (name.startsWith("**") && name.endsWith("**")) {
                            name = name.substring(2, name.length() - 2);
                        }
                    }
                }
                
                if (!name.isEmpty()) {
                    Character character = new Character();
                    character.setNovelId(novelId);
                    character.setName(name);
                    character.setRoleType(roleType);
                    character.setPersonality(personality);
                    character.setBackground(background);
                    character.setAppearance(appearance);
                    character.setAbilities(abilities);
                    character.setImportanceLevel(5); // 默认重要性
                    
                    characterService.createCharacter(character);
                }
            }
        } catch (Exception e) {
            log.error("创建角色失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 从AI生成的世界观信息创建世界观记录
     */
    private void createWorldSettingFromInfo(Long novelId, String worldSettingInfo) {
        try {
            WorldSetting worldSetting = new WorldSetting();
            worldSetting.setNovelId(novelId);
            worldSetting.setName("世界观设定");
            worldSetting.setDescription(worldSettingInfo);
            worldSetting.setCategory("universe"); // 默认类别
            worldSetting.setStatus("published");
            
            // 尝试从信息中提取特定部分
            if (worldSettingInfo.contains("历史") || worldSettingInfo.contains("时间") || worldSettingInfo.contains("年代")) {
                worldSetting.setCategory("history");
            } else if (worldSettingInfo.contains("地理") || worldSettingInfo.contains("地点") || worldSettingInfo.contains("环境")) {
                worldSetting.setCategory("geography");
            } else if (worldSettingInfo.contains("魔法") || worldSettingInfo.contains("魔") || worldSettingInfo.contains("法术")) {
                worldSetting.setCategory("magic");
                worldSetting.setMagicSystemRules(worldSettingInfo);
            } else if (worldSettingInfo.contains("种族") || worldSettingInfo.contains("人物") || worldSettingInfo.contains("民族")) {
                worldSetting.setCategory("race");
            }
            
            WorldSetting savedWorldSetting = worldSettingRepository.save(worldSetting);
        } catch (Exception e) {
            log.error("创建世界观失败: {}", e.getMessage(), e);
        }
    }
}