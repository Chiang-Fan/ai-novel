package com.aiwriter.service;

import com.aiwriter.entity.Chapter;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.service.ai.AiService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 节奏感知描写服务 - Phase 6
 * 根据情节节奏智能调整描写密度和风格
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RhythmAwareDescriptionService {

    private final ChapterRepository chapterRepository;
    private final AiService aiService;
    private final MultiDimensionalDescriptionEngine descriptionEngine;

    /**
     * 节奏类型枚举
     */
    public enum RhythmType {
        FAST("快节奏", 0.3),
        MODERATE("中等节奏", 0.6),
        SLOW("慢节奏", 1.0),
        CLIMAX("高潮", 0.4),
        TRANSITION("过渡", 0.7),
        RESOLUTION("收尾", 0.8);

        private final String displayName;
        private final double descriptionDensity;

        RhythmType(String displayName, double descriptionDensity) {
            this.displayName = displayName;
            this.descriptionDensity = descriptionDensity;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getDescriptionDensity() {
            return descriptionDensity;
        }
    }

    /**
     * 节奏分析结果
     */
    @Data
    public static class RhythmAnalysis {
        private RhythmType currentRhythm;
        private Double intensity;
        private Double recommendedDescriptionDensity;
        private List<String> recommendedDescriptionTypes;
        private Map<String, Double> descriptionTypeWeights;
        private List<RhythmSuggestion> suggestions;

        public RhythmAnalysis() {
            this.recommendedDescriptionTypes = new ArrayList<>();
            this.descriptionTypeWeights = new HashMap<>();
            this.suggestions = new ArrayList<>();
        }
    }

    /**
     * 节奏建议
     */
    @Data
    public static class RhythmSuggestion {
        private String type;
        private String suggestion;
        private String reason;
        private Integer priority;
    }

    /**
     * 节奏感知描写请求
     */
    @Data
    public static class RhythmAwareDescriptionRequest {
        private Long chapterId;
        private String currentContent;
        private String targetScene;
        private RhythmType overrideRhythm;
        private Integer targetLength;
        private List<String> focusAreas;
    }

    /**
     * 节奏感知描写结果
     */
    @Data
    public static class RhythmAwareDescriptionResult {
        private String generatedDescription;
        private RhythmAnalysis rhythmAnalysis;
        private Map<String, String> descriptionSegments;
        private List<String> appliedTechniques;
        private Double qualityScore;

        public RhythmAwareDescriptionResult() {
            this.descriptionSegments = new HashMap<>();
            this.appliedTechniques = new ArrayList<>();
        }
    }

    /**
     * 分析当前节奏
     */
    public RhythmAnalysis analyzeRhythm(String content) {
        log.info("分析内容节奏");
        RhythmAnalysis analysis = new RhythmAnalysis();

        // 分析节奏类型
        analysis.setCurrentRhythm(detectRhythmType(content));
        analysis.setIntensity(calculateIntensity(content));

        // 计算推荐描写密度
        double baseDensity = analysis.getCurrentRhythm().getDescriptionDensity();
        analysis.setRecommendedDescriptionDensity(baseDensity * analysis.getIntensity());

        // 推荐描写类型
        analysis.setRecommendedDescriptionTypes(recommendDescriptionTypes(analysis.getCurrentRhythm()));
        analysis.setDescriptionTypeWeights(calculateDescriptionWeights(analysis.getCurrentRhythm()));

        // 生成建议
        analysis.setSuggestions(generateRhythmSuggestions(analysis));

        return analysis;
    }

    /**
     * 生成节奏感知描写
     */
    public RhythmAwareDescriptionResult generateRhythmAwareDescription(
            RhythmAwareDescriptionRequest request) {
        
        log.info("生成节奏感知描写: chapterId={}", request.getChapterId());
        RhythmAwareDescriptionResult result = new RhythmAwareDescriptionResult();

        // 1. 分析节奏
        RhythmAnalysis rhythmAnalysis;
        if (request.getOverrideRhythm() != null) {
            rhythmAnalysis = createAnalysisFromRhythm(request.getOverrideRhythm());
        } else {
            rhythmAnalysis = analyzeRhythm(request.getCurrentContent());
        }
        result.setRhythmAnalysis(rhythmAnalysis);

        // 2. 根据节奏生成各类描写
        StringBuilder fullDescription = new StringBuilder();
        Map<String, Double> weights = rhythmAnalysis.getDescriptionTypeWeights();

        for (String descType : rhythmAnalysis.getRecommendedDescriptionTypes()) {
            Double weight = weights.getOrDefault(descType, 0.5);
            if (weight > 0.2) {
                String segment = generateDescriptionSegment(
                        descType, request, rhythmAnalysis, weight);
                result.getDescriptionSegments().put(descType, segment);
                fullDescription.append(segment).append("\n");
            }
        }

        result.setGeneratedDescription(fullDescription.toString().trim());

        // 3. 识别使用的技巧
        result.setAppliedTechniques(identifyAppliedTechniques(
                result.getGeneratedDescription(), rhythmAnalysis));

        // 4. 评估质量
        result.setQualityScore(assessDescriptionQuality(result, rhythmAnalysis));

        return result;
    }

    /**
     * 根据节奏调整描写
     */
    public String adjustDescriptionForRhythm(String originalDescription, RhythmType targetRhythm) {
        log.info("调整描写以适应节奏: {}", targetRhythm);

        String systemPrompt = buildRhythmAdjustmentPrompt(targetRhythm);
        String userPrompt = String.format("""
            请将以下描写调整为%s风格：
            
            原文：
            %s
            
            请保持核心内容不变，调整语言节奏和描写密度。
            """, targetRhythm.getDisplayName(), originalDescription);

        try {
            return aiService.chat(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("调整描写失败", e);
            return originalDescription;
        }
    }

    /**
     * 检测节奏类型
     */
    private RhythmType detectRhythmType(String content) {
        if (content == null || content.isEmpty()) {
            return RhythmType.MODERATE;
        }

        // 计算各种指标
        double actionDensity = calculateActionDensity(content);
        double dialogueDensity = calculateDialogueDensity(content);
        double sentenceLength = calculateAverageSentenceLength(content);
        double exclamationDensity = calculateExclamationDensity(content);

        // 高潮判断
        if (exclamationDensity > 0.05 && actionDensity > 0.1) {
            return RhythmType.CLIMAX;
        }

        // 快节奏判断
        if (sentenceLength < 15 && (actionDensity > 0.08 || dialogueDensity > 0.3)) {
            return RhythmType.FAST;
        }

        // 慢节奏判断
        if (sentenceLength > 30 && actionDensity < 0.03) {
            return RhythmType.SLOW;
        }

        // 过渡判断
        if (content.contains("然而") || content.contains("不过") || content.contains("但是")) {
            return RhythmType.TRANSITION;
        }

        // 收尾判断
        if (content.contains("最终") || content.contains("终于") || content.contains("从此")) {
            return RhythmType.RESOLUTION;
        }

        return RhythmType.MODERATE;
    }

    /**
     * 计算强度
     */
    private Double calculateIntensity(String content) {
        if (content == null || content.isEmpty()) {
            return 0.5;
        }

        double intensity = 0.5;

        // 感叹号增加强度
        intensity += content.chars().filter(c -> c == '！').count() * 0.05;

        // 问号增加强度
        intensity += content.chars().filter(c -> c == '？').count() * 0.03;

        // 省略号降低强度
        intensity -= (content.length() - content.replace("……", "").length()) / 2 * 0.02;

        return Math.max(0.1, Math.min(1.0, intensity));
    }

    /**
     * 推荐描写类型
     */
    private List<String> recommendDescriptionTypes(RhythmType rhythm) {
        return switch (rhythm) {
            case FAST -> List.of("动作描写", "对话描写");
            case CLIMAX -> List.of("动作描写", "情绪描写", "感官描写");
            case SLOW -> List.of("环境描写", "心理描写", "感官描写");
            case TRANSITION -> List.of("环境描写", "心理描写");
            case RESOLUTION -> List.of("心理描写", "情绪描写", "环境描写");
            default -> List.of("环境描写", "动作描写", "心理描写");
        };
    }

    /**
     * 计算描写类型权重
     */
    private Map<String, Double> calculateDescriptionWeights(RhythmType rhythm) {
        Map<String, Double> weights = new HashMap<>();

        switch (rhythm) {
            case FAST:
                weights.put("动作描写", 0.8);
                weights.put("对话描写", 0.7);
                weights.put("环境描写", 0.2);
                weights.put("心理描写", 0.3);
                weights.put("情绪描写", 0.4);
                break;
            case CLIMAX:
                weights.put("动作描写", 0.9);
                weights.put("情绪描写", 0.8);
                weights.put("感官描写", 0.7);
                weights.put("心理描写", 0.5);
                weights.put("环境描写", 0.3);
                break;
            case SLOW:
                weights.put("环境描写", 0.9);
                weights.put("心理描写", 0.8);
                weights.put("感官描写", 0.7);
                weights.put("情绪描写", 0.6);
                weights.put("动作描写", 0.3);
                break;
            case TRANSITION:
                weights.put("环境描写", 0.7);
                weights.put("心理描写", 0.6);
                weights.put("情绪描写", 0.5);
                weights.put("动作描写", 0.4);
                break;
            case RESOLUTION:
                weights.put("心理描写", 0.8);
                weights.put("情绪描写", 0.7);
                weights.put("环境描写", 0.6);
                weights.put("动作描写", 0.3);
                break;
            default:
                weights.put("环境描写", 0.6);
                weights.put("动作描写", 0.6);
                weights.put("心理描写", 0.6);
                weights.put("情绪描写", 0.5);
                weights.put("对话描写", 0.5);
        }

        return weights;
    }

    /**
     * 生成节奏建议
     */
    private List<RhythmSuggestion> generateRhythmSuggestions(RhythmAnalysis analysis) {
        List<RhythmSuggestion> suggestions = new ArrayList<>();

        RhythmSuggestion suggestion = new RhythmSuggestion();
        suggestion.setType("描写密度");
        suggestion.setSuggestion(String.format("建议描写密度：%.0f%%", 
                analysis.getRecommendedDescriptionDensity() * 100));
        suggestion.setReason("基于当前" + analysis.getCurrentRhythm().getDisplayName() + "的节奏特点");
        suggestion.setPriority(1);
        suggestions.add(suggestion);

        if (analysis.getCurrentRhythm() == RhythmType.FAST) {
            RhythmSuggestion fastSuggestion = new RhythmSuggestion();
            fastSuggestion.setType("句式");
            fastSuggestion.setSuggestion("使用短句，避免长篇环境描写");
            fastSuggestion.setReason("快节奏需要保持紧凑感");
            fastSuggestion.setPriority(1);
            suggestions.add(fastSuggestion);
        }

        if (analysis.getCurrentRhythm() == RhythmType.SLOW) {
            RhythmSuggestion slowSuggestion = new RhythmSuggestion();
            slowSuggestion.setType("细节");
            slowSuggestion.setSuggestion("可以增加感官细节和心理活动");
            slowSuggestion.setReason("慢节奏适合细腻描写");
            slowSuggestion.setPriority(2);
            suggestions.add(slowSuggestion);
        }

        return suggestions;
    }

    /**
     * 生成描写片段
     */
    private String generateDescriptionSegment(
            String descType, 
            RhythmAwareDescriptionRequest request,
            RhythmAnalysis rhythmAnalysis,
            Double weight) {

        int segmentLength = (int) (request.getTargetLength() * weight * 0.5);
        
        String systemPrompt = String.format("""
            你是一位专业的小说作家。请生成%s。
            
            节奏要求：%s
            描写密度：%.0f%%
            
            注意：
            - 保持与当前节奏一致
            - 字数约%d字
            """, 
            descType,
            rhythmAnalysis.getCurrentRhythm().getDisplayName(),
            rhythmAnalysis.getRecommendedDescriptionDensity() * 100,
            segmentLength);

        String userPrompt = String.format("""
            场景：%s
            上下文：%s
            
            请生成%s。
            """,
            request.getTargetScene(),
            request.getCurrentContent() != null ? 
                request.getCurrentContent().substring(0, Math.min(500, request.getCurrentContent().length())) : "",
            descType);

        try {
            return aiService.chat(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("生成描写片段失败: {}", descType, e);
            return "";
        }
    }

    /**
     * 构建节奏调整提示词
     */
    private String buildRhythmAdjustmentPrompt(RhythmType rhythm) {
        return switch (rhythm) {
            case FAST -> """
                你是一位擅长快节奏写作的作家。
                调整要求：
                - 使用短句，节奏紧凑
                - 减少环境描写，增加动作
                - 对话简洁有力
                - 保持紧张感
                """;
            case SLOW -> """
                你是一位擅长细腻描写的作家。
                调整要求：
                - 可以使用长句，节奏舒缓
                - 增加环境和感官描写
                - 深入心理活动
                - 营造氛围
                """;
            case CLIMAX -> """
                你是一位擅长高潮场景的作家。
                调整要求：
                - 情感强烈，张力十足
                - 动作描写生动
                - 适当使用短句增强冲击力
                - 感官描写鲜明
                """;
            default -> """
                你是一位专业的小说作家。
                请根据内容特点调整描写节奏。
                """;
        };
    }

    /**
     * 从节奏类型创建分析结果
     */
    private RhythmAnalysis createAnalysisFromRhythm(RhythmType rhythm) {
        RhythmAnalysis analysis = new RhythmAnalysis();
        analysis.setCurrentRhythm(rhythm);
        analysis.setIntensity(0.7);
        analysis.setRecommendedDescriptionDensity(rhythm.getDescriptionDensity());
        analysis.setRecommendedDescriptionTypes(recommendDescriptionTypes(rhythm));
        analysis.setDescriptionTypeWeights(calculateDescriptionWeights(rhythm));
        analysis.setSuggestions(generateRhythmSuggestions(analysis));
        return analysis;
    }

    /**
     * 识别应用的技巧
     */
    private List<String> identifyAppliedTechniques(String text, RhythmAnalysis analysis) {
        List<String> techniques = new ArrayList<>();

        if (text.contains("像") || text.contains("如同")) {
            techniques.add("比喻");
        }
        if (analysis.getCurrentRhythm() == RhythmType.FAST && 
            calculateAverageSentenceLength(text) < 15) {
            techniques.add("短句加速");
        }
        if (text.matches(".*[心想|暗自].*")) {
            techniques.add("内心独白");
        }

        return techniques;
    }

    /**
     * 评估描写质量
     */
    private Double assessDescriptionQuality(
            RhythmAwareDescriptionResult result, 
            RhythmAnalysis analysis) {
        
        double score = 60.0;

        // 技巧使用加分
        score += result.getAppliedTechniques().size() * 5;

        // 描写完整性加分
        score += result.getDescriptionSegments().size() * 5;

        return Math.min(100.0, score);
    }

    // ==================== 辅助计算方法 ====================

    private double calculateActionDensity(String content) {
        String[] actionWords = {"走", "跑", "跳", "打", "踢", "推", "拉", "冲", "扑"};
        int count = 0;
        for (String word : actionWords) {
            count += content.length() - content.replace(word, "").length();
        }
        return (double) count / content.length();
    }

    private double calculateDialogueDensity(String content) {
        int dialogueChars = 0;
        boolean inDialogue = false;
        for (char c : content.toCharArray()) {
            if (c == '"' || c == '"' || c == '「') {
                inDialogue = true;
            } else if (c == '"' || c == '"' || c == '」') {
                inDialogue = false;
            } else if (inDialogue) {
                dialogueChars++;
            }
        }
        return (double) dialogueChars / content.length();
    }

    private double calculateAverageSentenceLength(String content) {
        String[] sentences = content.split("[。！？]");
        if (sentences.length == 0) return 20;
        return Arrays.stream(sentences)
                .mapToInt(String::length)
                .average()
                .orElse(20);
    }

    private double calculateExclamationDensity(String content) {
        long count = content.chars().filter(c -> c == '！').count();
        return (double) count / content.length();
    }
}
