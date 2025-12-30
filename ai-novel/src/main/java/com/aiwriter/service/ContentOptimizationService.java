package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.ContentOptimization;
import com.aiwriter.entity.OptimizationRule;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.ContentOptimizationRepository;
import com.aiwriter.repository.OptimizationRuleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容优化服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentOptimizationService {
    
    private final ChapterRepository chapterRepository;
    private final ContentOptimizationRepository optimizationRepository;
    private final OptimizationRuleRepository ruleRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 优化文本
     */
    @Transactional
    public OptimizationResponse optimizeText(OptimizationRequest request) {
        log.info("开始优化文本，类型: {}", request.getOptimizationType());
        
        // 验证章节
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        // 构建优化提示词
        String prompt = buildOptimizationPrompt(request);
        
        // 调用AI进行优化
        String optimizedText = optimizeWithAI(prompt, request.getText());
        
        // 分析变更
        List<OptimizationResponse.Change> changes = analyzeChanges(
                request.getText(), optimizedText, request.getOptimizationType());
        
        // 获取当前版本号
        long count = optimizationRepository.countByChapterId(request.getChapterId());
        int version = (int) (count + 1);
        
        // 保存优化记录
        ContentOptimization optimization = ContentOptimization.builder()
                .chapterId(request.getChapterId())
                .originalText(request.getText())
                .optimizedText(optimizedText)
                .optimizationType(request.getOptimizationType())
                .changesSummary(serializeChanges(changes))
                .promptUsed(prompt)
                .aiModel("DeepSeek")
                .version(version)
                .isApplied(false)
                .build();
        
        optimization = optimizationRepository.save(optimization);
        
        return buildOptimizationResponse(optimization, changes);
    }
    
    /**
     * 检查文本质量
     */
    public TextCheckResponse checkText(String text, Long chapterId) {
        log.info("开始检查文本质量");
        
        // 获取所有启用的规则
        List<OptimizationRule> rules = ruleRepository.findByIsEnabledTrueOrderByUsageCountDesc();
        
        // 应用规则检查
        List<TextCheckResponse.Issue> issues = new ArrayList<>();
        for (OptimizationRule rule : rules) {
            issues.addAll(applyRule(rule, text));
        }
        
        // 计算质量分数
        TextCheckResponse.QualityScore score = calculateQualityScore(text, issues);
        
        return TextCheckResponse.builder()
                .totalIssues(issues.size())
                .issues(issues)
                .qualityScore(score)
                .build();
    }
    
    /**
     * 获取优化历史
     */
    public List<OptimizationResponse> getOptimizationHistory(Long chapterId, String optimizationType) {
        List<ContentOptimization> optimizations;
        
        if (optimizationType != null && !optimizationType.isEmpty()) {
            optimizations = optimizationRepository
                    .findByChapterIdAndOptimizationTypeOrderByCreatedAtDesc(chapterId, optimizationType);
        } else {
            optimizations = optimizationRepository
                    .findByChapterIdOrderByCreatedAtDesc(chapterId);
        }
        
        return optimizations.stream()
                .map(this::toOptimizationResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 应用优化
     */
    @Transactional
    public void applyOptimization(Long optimizationId) {
        ContentOptimization optimization = optimizationRepository.findById(optimizationId)
                .orElseThrow(() -> new RuntimeException("优化记录不存在"));
        
        // 更新章节内容
        Chapter chapter = chapterRepository.findById(optimization.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        String currentContent = chapter.getContent();
        String updatedContent = currentContent.replace(
                optimization.getOriginalText(), 
                optimization.getOptimizedText());
        
        chapter.setContent(updatedContent);
        chapterRepository.save(chapter);
        
        // 标记为已应用
        optimization.setIsApplied(true);
        optimization.setAppliedAt(LocalDateTime.now());
        optimizationRepository.save(optimization);
        
        log.info("已应用优化: {}", optimizationId);
    }
    
    /**
     * 评分
     */
    @Transactional
    public void rateOptimization(Long optimizationId, Integer rating) {
        ContentOptimization optimization = optimizationRepository.findById(optimizationId)
                .orElseThrow(() -> new RuntimeException("优化记录不存在"));
        
        optimization.setRating(rating);
        optimizationRepository.save(optimization);
    }
    
    /**
     * 获取优化规则
     */
    public List<OptimizationRuleResponse> getRules(String ruleType) {
        List<OptimizationRule> rules;
        
        if (ruleType != null && !ruleType.isEmpty()) {
            rules = ruleRepository.findByRuleTypeAndIsEnabledTrueOrderByUsageCountDesc(ruleType);
        } else {
            rules = ruleRepository.findByIsEnabledTrueOrderByUsageCountDesc();
        }
        
        return rules.stream()
                .map(this::toRuleResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 构建优化提示词
     */
    private String buildOptimizationPrompt(OptimizationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请对以下文本进行").append(getOptimizationTypeName(request.getOptimizationType()))
                .append("优化：\n\n");
        prompt.append("原文：\n").append(request.getText()).append("\n\n");
        
        switch (request.getOptimizationType()) {
            case "POLISH":
                prompt.append("要求：\n");
                prompt.append("1. 优化语言表达，使其更流畅自然\n");
                prompt.append("2. 增强文学性和艺术性\n");
                prompt.append("3. 保持原意不变\n");
                break;
            case "GRAMMAR":
                prompt.append("要求：\n");
                prompt.append("1. 修正语法错误\n");
                prompt.append("2. 规范标点符号使用\n");
                prompt.append("3. 统一时态和人称\n");
                break;
            case "DIALOGUE":
                prompt.append("要求：\n");
                prompt.append("1. 优化对话的自然度\n");
                prompt.append("2. 增强人物个性\n");
                prompt.append("3. 改进对话节奏\n");
                break;
            case "DESCRIPTION":
                prompt.append("要求：\n");
                prompt.append("1. 丰富感官描写\n");
                prompt.append("2. 增加细节刻画\n");
                prompt.append("3. 提升画面感\n");
                break;
            case "RHYTHM":
                prompt.append("要求：\n");
                prompt.append("1. 调整句式长短\n");
                prompt.append("2. 优化段落结构\n");
                prompt.append("3. 改善叙事节奏\n");
                break;
        }
        
        if (Boolean.TRUE.equals(request.getIncludeExplanation())) {
            prompt.append("\n请说明主要修改的地方及原因。");
        }
        
        return prompt.toString();
    }
    
    /**
     * 使用AI优化文本
     */
    private String optimizeWithAI(String prompt, String text) {
        // TODO: 集成实际的AI服务
        log.info("AI优化请求: {}", prompt.substring(0, Math.min(100, prompt.length())));
        
        // 模拟优化结果
        return text + "\n\n[AI优化建议：已针对性优化，提升了表达质量和可读性]";
    }
    
    /**
     * 分析变更
     */
    private List<OptimizationResponse.Change> analyzeChanges(
            String original, String optimized, String type) {
        List<OptimizationResponse.Change> changes = new ArrayList<>();
        
        // TODO: 实现真实的文本差异分析
        // 这里返回模拟数据
        changes.add(OptimizationResponse.Change.builder()
                .type("MODIFY")
                .original("原始表达")
                .modified("优化后表达")
                .reason(getOptimizationTypeName(type))
                .position(0)
                .build());
        
        return changes;
    }
    
    /**
     * 应用规则检查
     */
    private List<TextCheckResponse.Issue> applyRule(OptimizationRule rule, String text) {
        List<TextCheckResponse.Issue> issues = new ArrayList<>();
        
        // TODO: 实现真实的规则匹配
        // 这里返回模拟数据用于演示
        
        return issues;
    }
    
    /**
     * 计算质量分数
     */
    private TextCheckResponse.QualityScore calculateQualityScore(
            String text, List<TextCheckResponse.Issue> issues) {
        
        int grammarScore = 85;
        int styleScore = 78;
        int readabilityScore = 82;
        int consistencyScore = 90;
        
        // 根据问题数量调整分数
        int highIssues = (int) issues.stream()
                .filter(i -> "HIGH".equals(i.getSeverity())).count();
        int mediumIssues = (int) issues.stream()
                .filter(i -> "MEDIUM".equals(i.getSeverity())).count();
        
        int penalty = highIssues * 10 + mediumIssues * 5;
        
        int overall = Math.max(0, 
                (grammarScore + styleScore + readabilityScore + consistencyScore) / 4 - penalty);
        
        return TextCheckResponse.QualityScore.builder()
                .overall(overall)
                .grammar(grammarScore)
                .style(styleScore)
                .readability(readabilityScore)
                .consistency(consistencyScore)
                .build();
    }
    
    /**
     * 序列化变更列表
     */
    private String serializeChanges(List<OptimizationResponse.Change> changes) {
        try {
            return objectMapper.writeValueAsString(changes);
        } catch (JsonProcessingException e) {
            log.error("序列化变更失败", e);
            return "[]";
        }
    }
    
    /**
     * 反序列化变更列表
     */
    private List<OptimizationResponse.Change> deserializeChanges(String json) {
        try {
            return objectMapper.readValue(json, 
                    new TypeReference<List<OptimizationResponse.Change>>() {});
        } catch (JsonProcessingException e) {
            log.error("反序列化变更失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 构建优化响应
     */
    private OptimizationResponse buildOptimizationResponse(
            ContentOptimization optimization, List<OptimizationResponse.Change> changes) {
        return OptimizationResponse.builder()
                .id(optimization.getId())
                .chapterId(optimization.getChapterId())
                .originalText(optimization.getOriginalText())
                .optimizedText(optimization.getOptimizedText())
                .optimizationType(optimization.getOptimizationType())
                .changes(changes)
                .version(optimization.getVersion())
                .rating(optimization.getRating())
                .isApplied(optimization.getIsApplied())
                .createdAt(optimization.getCreatedAt())
                .build();
    }
    
    /**
     * 转换为优化响应
     */
    private OptimizationResponse toOptimizationResponse(ContentOptimization optimization) {
        List<OptimizationResponse.Change> changes = 
                deserializeChanges(optimization.getChangesSummary());
        return buildOptimizationResponse(optimization, changes);
    }
    
    /**
     * 转换为规则响应
     */
    private OptimizationRuleResponse toRuleResponse(OptimizationRule rule) {
        return OptimizationRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .ruleType(rule.getRuleType())
                .pattern(rule.getPattern())
                .description(rule.getDescription())
                .suggestion(rule.getSuggestion())
                .severity(rule.getSeverity())
                .isEnabled(rule.getIsEnabled())
                .usageCount(rule.getUsageCount())
                .createdAt(rule.getCreatedAt())
                .build();
    }
    
    /**
     * 获取优化类型名称
     */
    private String getOptimizationTypeName(String type) {
        switch (type) {
            case "POLISH": return "润色";
            case "GRAMMAR": return "语法";
            case "DIALOGUE": return "对话";
            case "DESCRIPTION": return "描写";
            case "RHYTHM": return "节奏";
            default: return "优化";
        }
    }
}
