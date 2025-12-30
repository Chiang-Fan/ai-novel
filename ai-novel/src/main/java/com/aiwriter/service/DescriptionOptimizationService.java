package com.aiwriter.service;

import com.aiwriter.service.ai.AiService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描写优化服务 - Phase 6
 * 提供AI智能改写和优化提示
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DescriptionOptimizationService {

    private final AiService aiService;
    private final DescriptionQualityScoreService qualityScoreService;

    /**
     * 优化类型枚举
     */
    public enum OptimizationType {
        ENHANCE_DETAIL("增强细节"),
        IMPROVE_RHYTHM("改善节奏"),
        STRENGTHEN_EMOTION("强化情感"),
        ADD_SENSORY("增加感官"),
        REMOVE_REDUNDANCY("去除冗余"),
        IMPROVE_DIALOGUE("优化对话"),
        DEEPEN_PSYCHOLOGY("深化心理"),
        VIVIFY_ACTION("生动动作");

        private final String displayName;

        OptimizationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 优化请求
     */
    @Data
    public static class OptimizationRequest {
        private String originalText;
        private List<OptimizationType> optimizationTypes;
        private String targetStyle;
        private Double targetQualityScore;
        private Boolean preserveLength;
        private Map<String, Object> additionalParams;
    }

    /**
     * 优化结果
     */
    @Data
    public static class OptimizationResult {
        private String optimizedText;
        private String originalText;
        private List<OptimizationChange> changes;
        private Double originalScore;
        private Double optimizedScore;
        private Double improvementPercentage;
        private List<String> appliedOptimizations;
        private List<String> furtherSuggestions;

        public OptimizationResult() {
            this.changes = new ArrayList<>();
            this.appliedOptimizations = new ArrayList<>();
            this.furtherSuggestions = new ArrayList<>();
        }
    }

    /**
     * 优化变更
     */
    @Data
    public static class OptimizationChange {
        private String type;
        private String originalSegment;
        private String optimizedSegment;
        private String reason;
        private Integer startPosition;
        private Integer endPosition;
    }

    /**
     * 问题诊断结果
     */
    @Data
    public static class DiagnosisResult {
        private List<DescriptionIssue> issues;
        private Map<String, Integer> issueCounts;
        private List<OptimizationType> recommendedOptimizations;
        private Double overallHealthScore;

        public DiagnosisResult() {
            this.issues = new ArrayList<>();
            this.issueCounts = new HashMap<>();
            this.recommendedOptimizations = new ArrayList<>();
        }
    }

    /**
     * 描写问题
     */
    @Data
    public static class DescriptionIssue {
        private String type;
        private String description;
        private String segment;
        private Integer position;
        private String suggestion;
        private Integer severity; // 1-严重, 2-中等, 3-轻微
    }

    /**
     * 优化描写
     */
    public OptimizationResult optimizeDescription(OptimizationRequest request) {
        log.info("优化描写: types={}", request.getOptimizationTypes());
        
        OptimizationResult result = new OptimizationResult();
        result.setOriginalText(request.getOriginalText());

        // 1. 评估原始质量
        DescriptionQualityScoreService.DescriptionQualityScore originalQuality = 
                qualityScoreService.scoreDescription(request.getOriginalText());
        result.setOriginalScore(originalQuality.getOverallScore().doubleValue());

        // 2. 构建优化提示词
        String optimizationPrompt = buildOptimizationPrompt(request);

        // 3. 执行AI优化
        try {
            String optimized = aiService.chat(OPTIMIZATION_SYSTEM_PROMPT, optimizationPrompt);
            result.setOptimizedText(optimized);

            // 4. 评估优化后质量
            DescriptionQualityScoreService.DescriptionQualityScore optimizedQuality = 
                    qualityScoreService.scoreDescription(optimized);
            result.setOptimizedScore(optimizedQuality.getOverallScore().doubleValue());

            // 5. 计算改进百分比
            if (result.getOriginalScore() > 0) {
                result.setImprovementPercentage(
                        (result.getOptimizedScore() - result.getOriginalScore()) / result.getOriginalScore() * 100);
            } else {
                result.setImprovementPercentage(0.0);
            }

            // 6. 识别变更
            result.setChanges(identifyChanges(request.getOriginalText(), optimized));

            // 7. 记录应用的优化
            for (OptimizationType type : request.getOptimizationTypes()) {
                result.getAppliedOptimizations().add(type.getDisplayName());
            }

            // 8. 生成进一步建议
            result.setFurtherSuggestions(generateFurtherSuggestions(optimizedQuality));

        } catch (Exception e) {
            log.error("优化描写失败", e);
            result.setOptimizedText(request.getOriginalText());
            result.setOptimizedScore(result.getOriginalScore());
            result.setImprovementPercentage(0.0);
        }

        return result;
    }

    /**
     * 诊断描写问题
     */
    public DiagnosisResult diagnoseDescription(String text) {
        log.info("诊断描写问题");
        DiagnosisResult result = new DiagnosisResult();

        // 1. 检查冗余问题
        checkRedundancyIssues(text, result);

        // 2. 检查节奏问题
        checkRhythmIssues(text, result);

        // 3. 检查描写缺失
        checkMissingDescriptions(text, result);

        // 4. 检查对话问题
        checkDialogueIssues(text, result);

        // 5. 检查情感表达
        checkEmotionIssues(text, result);

        // 6. 统计问题数量
        for (DescriptionIssue issue : result.getIssues()) {
            result.getIssueCounts().merge(issue.getType(), 1, Integer::sum);
        }

        // 7. 推荐优化类型
        result.setRecommendedOptimizations(recommendOptimizations(result));

        // 8. 计算健康度
        result.setOverallHealthScore(calculateHealthScore(result));

        return result;
    }

    /**
     * 智能改写
     */
    public String intelligentRewrite(String text, String instruction) {
        log.info("智能改写: instruction={}", instruction);

        String prompt = String.format("""
            请根据以下指令改写文本：
            
            指令：%s
            
            原文：
            %s
            
            请直接输出改写后的文本，不需要解释。
            """, instruction, text);

        try {
            return aiService.chat(REWRITE_SYSTEM_PROMPT, prompt);
        } catch (Exception e) {
            log.error("智能改写失败", e);
            return text;
        }
    }

    /**
     * 批量优化
     */
    public List<OptimizationResult> batchOptimize(List<String> texts, List<OptimizationType> types) {
        List<OptimizationResult> results = new ArrayList<>();

        for (String text : texts) {
            OptimizationRequest request = new OptimizationRequest();
            request.setOriginalText(text);
            request.setOptimizationTypes(types);
            results.add(optimizeDescription(request));
        }

        return results;
    }

    /**
     * 获取优化建议
     */
    public List<String> getOptimizationSuggestions(String text) {
        DiagnosisResult diagnosis = diagnoseDescription(text);
        List<String> suggestions = new ArrayList<>();

        for (DescriptionIssue issue : diagnosis.getIssues()) {
            if (issue.getSeverity() <= 2) {
                suggestions.add(issue.getSuggestion());
            }
        }

        return suggestions;
    }

    // ==================== 私有方法 ====================

    private String buildOptimizationPrompt(OptimizationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请优化以下描写：\n\n");
        prompt.append("原文：\n").append(request.getOriginalText()).append("\n\n");
        prompt.append("优化要求：\n");

        for (OptimizationType type : request.getOptimizationTypes()) {
            prompt.append("- ").append(type.getDisplayName()).append("\n");
        }

        if (request.getTargetStyle() != null) {
            prompt.append("\n目标风格：").append(request.getTargetStyle());
        }

        if (Boolean.TRUE.equals(request.getPreserveLength())) {
            prompt.append("\n注意：保持与原文相近的字数");
        }

        prompt.append("\n\n请直接输出优化后的文本，不需要解释。");

        return prompt.toString();
    }

    private List<OptimizationChange> identifyChanges(String original, String optimized) {
        List<OptimizationChange> changes = new ArrayList<>();

        // 简化实现：比较长度变化
        if (Math.abs(original.length() - optimized.length()) > 50) {
            OptimizationChange change = new OptimizationChange();
            change.setType("整体调整");
            change.setOriginalSegment(original.substring(0, Math.min(100, original.length())) + "...");
            change.setOptimizedSegment(optimized.substring(0, Math.min(100, optimized.length())) + "...");
            change.setReason("文本结构调整");
            changes.add(change);
        }

        return changes;
    }

    private List<String> generateFurtherSuggestions(
            DescriptionQualityScoreService.DescriptionQualityScore quality) {
        List<String> suggestions = new ArrayList<>();

        if (quality.getDialogueScore() != null && quality.getDialogueScore().getScore() < 70) {
            suggestions.add("对话部分仍有优化空间，可以增加角色特色");
        }

        if (quality.getEnvironmentScore() != null && quality.getEnvironmentScore().getScore() < 70) {
            suggestions.add("环境描写可以增加更多感官细节");
        }

        if (quality.getPsychologicalScore() != null && quality.getPsychologicalScore().getScore() < 70) {
            suggestions.add("心理描写可以更加深入细腻");
        }

        return suggestions;
    }

    private void checkRedundancyIssues(String text, DiagnosisResult result) {
        // 检查重复词汇
        String[] redundantPatterns = {"非常非常", "很很", "十分十分"};
        for (String pattern : redundantPatterns) {
            if (text.contains(pattern)) {
                DescriptionIssue issue = new DescriptionIssue();
                issue.setType("冗余");
                issue.setDescription("发现重复词汇");
                issue.setSegment(pattern);
                issue.setSuggestion("删除重复的修饰词");
                issue.setSeverity(2);
                result.getIssues().add(issue);
            }
        }

        // 检查过度修饰
        Pattern overModified = Pattern.compile("(非常|十分|极其|特别){2,}");
        Matcher matcher = overModified.matcher(text);
        while (matcher.find()) {
            DescriptionIssue issue = new DescriptionIssue();
            issue.setType("冗余");
            issue.setDescription("过度修饰");
            issue.setSegment(matcher.group());
            issue.setPosition(matcher.start());
            issue.setSuggestion("简化修饰词，保留一个即可");
            issue.setSeverity(2);
            result.getIssues().add(issue);
        }
    }

    private void checkRhythmIssues(String text, DiagnosisResult result) {
        String[] sentences = text.split("[。！？]");

        // 检查句子长度一致性
        int shortCount = 0;
        int longCount = 0;
        for (String sentence : sentences) {
            if (sentence.length() < 10) shortCount++;
            if (sentence.length() > 50) longCount++;
        }

        if (shortCount > sentences.length * 0.7) {
            DescriptionIssue issue = new DescriptionIssue();
            issue.setType("节奏");
            issue.setDescription("短句过多，节奏单调");
            issue.setSuggestion("适当增加长句，丰富节奏变化");
            issue.setSeverity(3);
            result.getIssues().add(issue);
        }

        if (longCount > sentences.length * 0.7) {
            DescriptionIssue issue = new DescriptionIssue();
            issue.setType("节奏");
            issue.setDescription("长句过多，阅读疲劳");
            issue.setSuggestion("适当拆分长句，增加短句调节节奏");
            issue.setSeverity(2);
            result.getIssues().add(issue);
        }
    }

    private void checkMissingDescriptions(String text, DiagnosisResult result) {
        // 检查是否缺少感官描写
        boolean hasVisual = text.matches(".*[看|望|眼|色|光].*");
        boolean hasAudio = text.matches(".*[听|声|音|响].*");
        boolean hasSmell = text.matches(".*[闻|香|味|气].*");

        if (!hasVisual && !hasAudio && !hasSmell) {
            DescriptionIssue issue = new DescriptionIssue();
            issue.setType("缺失");
            issue.setDescription("缺少感官描写");
            issue.setSuggestion("增加视觉、听觉或嗅觉等感官描写");
            issue.setSeverity(2);
            result.getIssues().add(issue);
        }

        // 检查是否缺少心理描写
        if (!text.matches(".*[想|觉得|感到|意识|明白].*") && text.length() > 200) {
            DescriptionIssue issue = new DescriptionIssue();
            issue.setType("缺失");
            issue.setDescription("缺少心理描写");
            issue.setSuggestion("增加人物内心活动的描写");
            issue.setSeverity(3);
            result.getIssues().add(issue);
        }
    }

    private void checkDialogueIssues(String text, DiagnosisResult result) {
        // 提取对话
        Pattern dialoguePattern = Pattern.compile("[\\\"「]([^\\\"」]*?)[\\\"」]");
        Matcher matcher = dialoguePattern.matcher(text);

        List<String> dialogues = new ArrayList<>();
        while (matcher.find()) {
            dialogues.add(matcher.group(1));
        }

        // 检查对话是否过短
        for (String dialogue : dialogues) {
            if (dialogue.length() < 3) {
                DescriptionIssue issue = new DescriptionIssue();
                issue.setType("对话");
                issue.setDescription("对话过短");
                issue.setSegment(dialogue);
                issue.setSuggestion("扩展对话内容，增加信息量");
                issue.setSeverity(3);
                result.getIssues().add(issue);
            }
        }

        // 检查对话标签
        if (dialogues.size() > 3 && !text.contains("说道") && !text.contains("问道")) {
            DescriptionIssue issue = new DescriptionIssue();
            issue.setType("对话");
            issue.setDescription("对话缺少说话人标识");
            issue.setSuggestion("增加对话标签，明确说话人");
            issue.setSeverity(2);
            result.getIssues().add(issue);
        }
    }

    private void checkEmotionIssues(String text, DiagnosisResult result) {
        // 检查是否有直接陈述情绪
        String[] directEmotions = {"他很高兴", "她很难过", "他很生气", "她很害怕"};
        for (String emotion : directEmotions) {
            if (text.contains(emotion)) {
                DescriptionIssue issue = new DescriptionIssue();
                issue.setType("情感");
                issue.setDescription("情绪表达过于直接");
                issue.setSegment(emotion);
                issue.setSuggestion("用行为或身体反应展现情绪，而非直接陈述");
                issue.setSeverity(2);
                result.getIssues().add(issue);
            }
        }
    }

    private List<OptimizationType> recommendOptimizations(DiagnosisResult diagnosis) {
        List<OptimizationType> recommendations = new ArrayList<>();

        Map<String, Integer> counts = diagnosis.getIssueCounts();

        if (counts.getOrDefault("冗余", 0) > 0) {
            recommendations.add(OptimizationType.REMOVE_REDUNDANCY);
        }
        if (counts.getOrDefault("节奏", 0) > 0) {
            recommendations.add(OptimizationType.IMPROVE_RHYTHM);
        }
        if (counts.getOrDefault("缺失", 0) > 0) {
            recommendations.add(OptimizationType.ADD_SENSORY);
            recommendations.add(OptimizationType.DEEPEN_PSYCHOLOGY);
        }
        if (counts.getOrDefault("对话", 0) > 0) {
            recommendations.add(OptimizationType.IMPROVE_DIALOGUE);
        }
        if (counts.getOrDefault("情感", 0) > 0) {
            recommendations.add(OptimizationType.STRENGTHEN_EMOTION);
        }

        return recommendations;
    }

    private Double calculateHealthScore(DiagnosisResult diagnosis) {
        double score = 100.0;

        for (DescriptionIssue issue : diagnosis.getIssues()) {
            switch (issue.getSeverity()) {
                case 1 -> score -= 15;
                case 2 -> score -= 8;
                case 3 -> score -= 3;
            }
        }

        return Math.max(0.0, score);
    }

    // ==================== 系统提示词常量 ====================

    private static final String OPTIMIZATION_SYSTEM_PROMPT = """
        你是一位专业的文学编辑，擅长优化小说描写。
        
        优化原则：
        1. 保持原文的核心含义和情感
        2. 提升语言的生动性和感染力
        3. 增强描写的层次感和细节
        4. 改善句子的节奏和韵律
        5. 避免过度修改导致风格偏离
        
        请直接输出优化后的文本，不需要解释修改原因。
        """;

    private static final String REWRITE_SYSTEM_PROMPT = """
        你是一位专业的小说作家，擅长根据指令改写文本。
        
        改写原则：
        1. 准确理解改写指令
        2. 保持原文的核心信息
        3. 根据指令调整风格和表达
        4. 确保改写后的文本流畅自然
        
        请直接输出改写后的文本，不需要解释。
        """;
}
