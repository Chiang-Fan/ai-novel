package com.aiwriter.service;

import com.aiwriter.entity.Scene;
import com.aiwriter.entity.Character;
import com.aiwriter.repository.SceneRepository;
import com.aiwriter.repository.CharacterRepository;
import com.aiwriter.service.ai.AiService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 多维描写引擎 - Phase 6 核心服务
 * 提供环境描写、心理描写、情绪描写、动作描写的智能生成
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiDimensionalDescriptionEngine {

    private final SceneRepository sceneRepository;
    private final CharacterRepository characterRepository;
    private final AiService aiService;

    /**
     * 描写类型枚举
     */
    public enum DescriptionType {
        ENVIRONMENT("环境描写"),
        PSYCHOLOGICAL("心理描写"),
        EMOTIONAL("情绪描写"),
        ACTION("动作描写"),
        DIALOGUE("对话描写"),
        SENSORY("感官描写");

        private final String displayName;

        DescriptionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 描写风格枚举
     */
    public enum DescriptionStyle {
        CONCISE("简洁"),
        DETAILED("细腻"),
        POETIC("诗意"),
        DRAMATIC("戏剧化"),
        REALISTIC("写实"),
        IMPRESSIONISTIC("印象派");

        private final String displayName;

        DescriptionStyle(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 描写生成请求
     */
    @Data
    public static class DescriptionGenerationRequest {
        private Long novelId;
        private Long sceneId;
        private Long characterId;
        private DescriptionType descriptionType;
        private DescriptionStyle style;
        private String context;
        private String targetMood;
        private Integer targetLength;
        private Double intensity;
        private List<String> keywords;
        private Map<String, Object> additionalParams;
    }

    /**
     * 描写生成结果
     */
    @Data
    public static class DescriptionGenerationResult {
        private String generatedText;
        private DescriptionType type;
        private DescriptionStyle style;
        private Integer actualLength;
        private Double qualityScore;
        private List<String> usedTechniques;
        private List<String> suggestions;
        private Map<String, Object> metadata;

        public DescriptionGenerationResult() {
            this.usedTechniques = new ArrayList<>();
            this.suggestions = new ArrayList<>();
            this.metadata = new HashMap<>();
        }
    }

    /**
     * 环境描写参数
     */
    @Data
    public static class EnvironmentDescriptionParams {
        private String location;
        private String timeOfDay;
        private String weather;
        private String season;
        private String atmosphere;
        private List<String> sensoryDetails;
        private Boolean includeSymbolism;
    }

    /**
     * 心理描写参数
     */
    @Data
    public static class PsychologicalDescriptionParams {
        private String characterName;
        private String currentEmotion;
        private String triggerEvent;
        private String internalConflict;
        private List<String> memories;
        private Boolean showNotTell;
    }

    /**
     * 情绪描写参数
     */
    @Data
    public static class EmotionalDescriptionParams {
        private String emotion;
        private Double intensity;
        private String physicalManifestation;
        private String behaviorChange;
        private Boolean gradualTransition;
        private String previousEmotion;
    }

    /**
     * 动作描写参数
     */
    @Data
    public static class ActionDescriptionParams {
        private String actionType;
        private String characterName;
        private String purpose;
        private String setting;
        private Double speed;
        private Boolean includeReaction;
    }

    /**
     * 生成多维描写
     */
    public DescriptionGenerationResult generateDescription(DescriptionGenerationRequest request) {
        log.info("生成多维描写: type={}, style={}, novelId={}", 
                request.getDescriptionType(), request.getStyle(), request.getNovelId());

        DescriptionGenerationResult result = new DescriptionGenerationResult();
        result.setType(request.getDescriptionType());
        result.setStyle(request.getStyle() != null ? request.getStyle() : DescriptionStyle.DETAILED);

        String prompt = buildDescriptionPrompt(request);
        String systemPrompt = buildSystemPrompt(request);

        try {
            String generated = aiService.chat(systemPrompt, prompt);
            result.setGeneratedText(generated);
            result.setActualLength(generated.length());
            result.setQualityScore(assessQuality(generated, request));
            result.setUsedTechniques(identifyTechniques(generated));
            result.setSuggestions(generateSuggestions(generated, request));
        } catch (Exception e) {
            log.error("描写生成失败", e);
            result.setGeneratedText("");
            result.setQualityScore(0.0);
            result.getSuggestions().add("生成失败，请重试");
        }

        return result;
    }

    /**
     * 生成环境描写
     */
    public DescriptionGenerationResult generateEnvironmentDescription(
            Long novelId, EnvironmentDescriptionParams params, DescriptionStyle style) {
        
        DescriptionGenerationRequest request = new DescriptionGenerationRequest();
        request.setNovelId(novelId);
        request.setDescriptionType(DescriptionType.ENVIRONMENT);
        request.setStyle(style);
        
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("location", params.getLocation());
        additionalParams.put("timeOfDay", params.getTimeOfDay());
        additionalParams.put("weather", params.getWeather());
        additionalParams.put("season", params.getSeason());
        additionalParams.put("atmosphere", params.getAtmosphere());
        additionalParams.put("sensoryDetails", params.getSensoryDetails());
        additionalParams.put("includeSymbolism", params.getIncludeSymbolism());
        request.setAdditionalParams(additionalParams);
        
        return generateDescription(request);
    }

    /**
     * 生成心理描写
     */
    public DescriptionGenerationResult generatePsychologicalDescription(
            Long novelId, Long characterId, PsychologicalDescriptionParams params, DescriptionStyle style) {
        
        DescriptionGenerationRequest request = new DescriptionGenerationRequest();
        request.setNovelId(novelId);
        request.setCharacterId(characterId);
        request.setDescriptionType(DescriptionType.PSYCHOLOGICAL);
        request.setStyle(style);
        
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("characterName", params.getCharacterName());
        additionalParams.put("currentEmotion", params.getCurrentEmotion());
        additionalParams.put("triggerEvent", params.getTriggerEvent());
        additionalParams.put("internalConflict", params.getInternalConflict());
        additionalParams.put("memories", params.getMemories());
        additionalParams.put("showNotTell", params.getShowNotTell());
        request.setAdditionalParams(additionalParams);
        
        return generateDescription(request);
    }

    /**
     * 生成情绪描写
     */
    public DescriptionGenerationResult generateEmotionalDescription(
            Long novelId, Long characterId, EmotionalDescriptionParams params, DescriptionStyle style) {
        
        DescriptionGenerationRequest request = new DescriptionGenerationRequest();
        request.setNovelId(novelId);
        request.setCharacterId(characterId);
        request.setDescriptionType(DescriptionType.EMOTIONAL);
        request.setStyle(style);
        request.setIntensity(params.getIntensity());
        
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("emotion", params.getEmotion());
        additionalParams.put("physicalManifestation", params.getPhysicalManifestation());
        additionalParams.put("behaviorChange", params.getBehaviorChange());
        additionalParams.put("gradualTransition", params.getGradualTransition());
        additionalParams.put("previousEmotion", params.getPreviousEmotion());
        request.setAdditionalParams(additionalParams);
        
        return generateDescription(request);
    }

    /**
     * 生成动作描写
     */
    public DescriptionGenerationResult generateActionDescription(
            Long novelId, Long characterId, ActionDescriptionParams params, DescriptionStyle style) {
        
        DescriptionGenerationRequest request = new DescriptionGenerationRequest();
        request.setNovelId(novelId);
        request.setCharacterId(characterId);
        request.setDescriptionType(DescriptionType.ACTION);
        request.setStyle(style);
        
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("actionType", params.getActionType());
        additionalParams.put("characterName", params.getCharacterName());
        additionalParams.put("purpose", params.getPurpose());
        additionalParams.put("setting", params.getSetting());
        additionalParams.put("speed", params.getSpeed());
        additionalParams.put("includeReaction", params.getIncludeReaction());
        request.setAdditionalParams(additionalParams);
        
        return generateDescription(request);
    }

    /**
     * 构建描写提示词
     */
    private String buildDescriptionPrompt(DescriptionGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            prompt.append("=== 上下文 ===\n").append(request.getContext()).append("\n\n");
        }
        
        prompt.append("=== 描写要求 ===\n");
        prompt.append("描写类型：").append(request.getDescriptionType().getDisplayName()).append("\n");
        prompt.append("描写风格：").append(request.getStyle().getDisplayName()).append("\n");
        
        if (request.getTargetLength() != null) {
            prompt.append("目标字数：约").append(request.getTargetLength()).append("字\n");
        }
        
        if (request.getTargetMood() != null) {
            prompt.append("目标氛围：").append(request.getTargetMood()).append("\n");
        }
        
        if (request.getIntensity() != null) {
            prompt.append("情感强度：").append(String.format("%.0f%%", request.getIntensity() * 100)).append("\n");
        }
        
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            prompt.append("关键词：").append(String.join("、", request.getKeywords())).append("\n");
        }
        
        if (request.getAdditionalParams() != null && !request.getAdditionalParams().isEmpty()) {
            prompt.append("\n=== 详细参数 ===\n");
            for (Map.Entry<String, Object> entry : request.getAdditionalParams().entrySet()) {
                if (entry.getValue() != null) {
                    prompt.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
                }
            }
        }
        
        prompt.append("\n请根据以上要求生成描写内容，直接输出描写文本，不需要额外说明。");
        
        return prompt.toString();
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(DescriptionGenerationRequest request) {
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("你是一位专业的小说写作专家，擅长各种类型的文学描写。\n\n");
        
        switch (request.getDescriptionType()) {
            case ENVIRONMENT:
                systemPrompt.append(ENVIRONMENT_DESCRIPTION_GUIDE);
                break;
            case PSYCHOLOGICAL:
                systemPrompt.append(PSYCHOLOGICAL_DESCRIPTION_GUIDE);
                break;
            case EMOTIONAL:
                systemPrompt.append(EMOTIONAL_DESCRIPTION_GUIDE);
                break;
            case ACTION:
                systemPrompt.append(ACTION_DESCRIPTION_GUIDE);
                break;
            case SENSORY:
                systemPrompt.append(SENSORY_DESCRIPTION_GUIDE);
                break;
            default:
                systemPrompt.append(GENERAL_DESCRIPTION_GUIDE);
        }
        
        systemPrompt.append("\n\n").append(getStyleGuide(request.getStyle()));
        
        return systemPrompt.toString();
    }

    /**
     * 获取风格指南
     */
    private String getStyleGuide(DescriptionStyle style) {
        if (style == null) {
            return "";
        }
        
        return switch (style) {
            case CONCISE -> """
                风格要求：简洁明快
                - 用最少的文字传达最多的信息
                - 避免冗余修饰
                - 动词有力，形容词精准
                - 节奏紧凑，不拖沓
                """;
            case DETAILED -> """
                风格要求：细腻详尽
                - 注重细节描写，层次分明
                - 多角度展现场景或情感
                - 适当使用比喻和象征
                - 给读者充分的想象空间
                """;
            case POETIC -> """
                风格要求：诗意优美
                - 语言富有韵律感
                - 善用意象和象征
                - 注重情景交融
                - 追求语言的美感和意境
                """;
            case DRAMATIC -> """
                风格要求：戏剧化
                - 强调冲突和张力
                - 情感表达强烈
                - 节奏起伏明显
                - 善用对比和反差
                """;
            case REALISTIC -> """
                风格要求：写实
                - 真实可信，贴近生活
                - 细节准确，符合常理
                - 避免过度夸张
                - 注重逻辑性和合理性
                """;
            case IMPRESSIONISTIC -> """
                风格要求：印象派
                - 注重感觉和印象
                - 善用色彩和光影
                - 捕捉瞬间的感受
                - 留白和暗示
                """;
        };
    }

    /**
     * 评估生成质量
     */
    private Double assessQuality(String text, DescriptionGenerationRequest request) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        
        double score = 60.0;
        
        // 长度评估
        int targetLength = request.getTargetLength() != null ? request.getTargetLength() : 200;
        double lengthRatio = (double) text.length() / targetLength;
        if (lengthRatio >= 0.8 && lengthRatio <= 1.2) {
            score += 10;
        } else if (lengthRatio >= 0.5 && lengthRatio <= 1.5) {
            score += 5;
        }
        
        // 关键词覆盖
        if (request.getKeywords() != null) {
            int covered = 0;
            for (String keyword : request.getKeywords()) {
                if (text.contains(keyword)) {
                    covered++;
                }
            }
            score += (double) covered / request.getKeywords().size() * 15;
        }
        
        // 描写技巧使用
        score += countDescriptionTechniques(text) * 3;
        
        return Math.min(100.0, score);
    }

    /**
     * 识别使用的描写技巧
     */
    private List<String> identifyTechniques(String text) {
        List<String> techniques = new ArrayList<>();
        
        if (text.contains("像") || text.contains("如同") || text.contains("仿佛")) {
            techniques.add("比喻");
        }
        if (text.matches(".*[声音响动].*") && text.matches(".*[色彩光影].*")) {
            techniques.add("通感");
        }
        if (text.contains("渐渐") || text.contains("慢慢") || text.contains("逐渐")) {
            techniques.add("渐进描写");
        }
        if (text.matches(".*[心想|暗自|内心|想到].*")) {
            techniques.add("内心独白");
        }
        if (text.contains("却") || text.contains("然而") || text.contains("但是")) {
            techniques.add("对比");
        }
        
        return techniques;
    }

    /**
     * 计算描写技巧数量
     */
    private int countDescriptionTechniques(String text) {
        return identifyTechniques(text).size();
    }

    /**
     * 生成改进建议
     */
    private List<String> generateSuggestions(String text, DescriptionGenerationRequest request) {
        List<String> suggestions = new ArrayList<>();
        
        if (text.length() < 100) {
            suggestions.add("描写较短，可以增加更多细节");
        }
        
        if (!text.contains("像") && !text.contains("如同")) {
            suggestions.add("可以适当使用比喻增强表现力");
        }
        
        if (request.getDescriptionType() == DescriptionType.ENVIRONMENT) {
            if (!text.matches(".*[声音|香气|温度|触感].*")) {
                suggestions.add("建议增加多感官描写（听觉、嗅觉、触觉等）");
            }
        }
        
        if (request.getDescriptionType() == DescriptionType.EMOTIONAL) {
            if (!text.matches(".*[身体|手|眼|脸].*")) {
                suggestions.add("建议通过身体反应展现情绪，而非直接陈述");
            }
        }
        
        return suggestions;
    }

    // ==================== 描写指南常量 ====================

    private static final String ENVIRONMENT_DESCRIPTION_GUIDE = """
        环境描写指南：
        1. 多感官描写：视觉、听觉、嗅觉、触觉、味觉
        2. 远近结合：从整体到局部，从远景到近景
        3. 动静结合：静态景物与动态元素相互映衬
        4. 情景交融：环境与人物情绪相呼应
        5. 时间感：通过光影、温度等体现时间流逝
        6. 象征意义：环境元素可以暗示情节或人物命运
        """;

    private static final String PSYCHOLOGICAL_DESCRIPTION_GUIDE = """
        心理描写指南：
        1. 内心独白：直接展现人物思想活动
        2. 意识流：捕捉零散、跳跃的思绪
        3. 回忆闪回：通过记忆展现内心世界
        4. 心理矛盾：展现内心的冲突与挣扎
        5. 潜意识：暗示深层心理动机
        6. Show not Tell：通过行为暗示心理，而非直接陈述
        """;

    private static final String EMOTIONAL_DESCRIPTION_GUIDE = """
        情绪描写指南：
        1. 身体反应：心跳、呼吸、肌肉紧张等
        2. 面部表情：眼神、嘴角、眉毛等细节
        3. 行为变化：动作的快慢、力度变化
        4. 语言特征：语速、语调、用词变化
        5. 情绪渐变：展现情绪的发展过程
        6. 情绪对比：通过前后对比强化效果
        """;

    private static final String ACTION_DESCRIPTION_GUIDE = """
        动作描写指南：
        1. 动词精准：选择最贴切的动词
        2. 节奏控制：短句加速，长句减速
        3. 细节分解：将大动作分解为小动作
        4. 力量感：体现动作的力度和速度
        5. 连贯性：动作之间的衔接自然
        6. 反应描写：动作引发的反应和结果
        """;

    private static final String SENSORY_DESCRIPTION_GUIDE = """
        感官描写指南：
        1. 视觉：色彩、形状、光影、距离
        2. 听觉：声音的高低、远近、质感
        3. 嗅觉：气味的浓淡、来源、联想
        4. 触觉：温度、质地、压力、疼痛
        5. 味觉：酸甜苦辣咸，以及复合味道
        6. 通感：不同感官之间的转换和融合
        """;

    private static final String GENERAL_DESCRIPTION_GUIDE = """
        通用描写指南：
        1. 具体化：避免抽象，使用具体细节
        2. 独特性：寻找独特的观察角度
        3. 层次感：由表及里，由浅入深
        4. 节奏感：长短句结合，张弛有度
        5. 情感性：融入情感，引发共鸣
        6. 简洁性：删除冗余，保留精华
        """;
}
