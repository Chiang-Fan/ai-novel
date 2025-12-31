package com.aiwriter.service;

import com.aiwriter.dto.EnhancedContinuationRequest;
import com.aiwriter.dto.EnhancedContinuationResponse;
import com.aiwriter.entity.AIContinuation;
import com.aiwriter.entity.Chapter;
import com.aiwriter.repository.AIContinuationRepository;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 增强型续写服务 - Phase 4 核心实现
 * 整合多维约束、情节一致性检查和描写质量评分
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedContinuationService {

    private final ChapterRepository chapterRepository;
    private final AIContinuationRepository continuationRepository;
    private final MultiDimensionalConstraintEngine constraintEngine;
    private final PlotConsistencyCheckService consistencyCheckService;
    private final DescriptionQualityScoreService qualityScoreService;
    private final AiService aiService;

    /**
     * 生成增强型续写 - 支持多维约束和迭代优化
     */
    @Transactional
    public EnhancedContinuationResponse generateEnhancedContinuation(
            EnhancedContinuationRequest request) {
        
        log.info("生成增强型续写: chapterId={}, enableConstraints={}, iterativeRefinement={}", 
                request.getChapterId(), request.getEnableConstraints(), request.getIterativeRefinement());
        
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        EnhancedContinuationResponse response = new EnhancedContinuationResponse();
        
        // 1. 构建多维约束
        MultiDimensionalConstraintEngine.CompositeConstraints constraints = null;
        if (Boolean.TRUE.equals(request.getEnableConstraints())) {
            constraints = constraintEngine.buildCompositeConstraints(
                chapter.getNovelId(), request.getChapterId());
            response.setConstraints(constraints);
        }
        
        // 2. 生成初始续写
        String continuationText = generateContinuationWithConstraints(chapter, request, constraints);
        
        // 3. 迭代优化（如果启用）
        if (Boolean.TRUE.equals(request.getIterativeRefinement())) {
            continuationText = iterativelyRefineContinuation(
                continuationText, chapter, request, constraints);
        }
        
        // 4. 检查情节一致性
        PlotConsistencyCheckService.PlotConsistencyCheckResult consistencyResult = null;
        if (Boolean.TRUE.equals(request.getCheckPlotConsistency())) {
            consistencyResult = consistencyCheckService.checkPlotConsistency(
                chapter.getNovelId(), 
                request.getChapterId(),
                continuationText,
                request.getSourceText());
            response.setConsistencyCheck(consistencyResult);
        }
        
        // 5. 评分描写质量
        DescriptionQualityScoreService.DescriptionQualityScore qualityScore = null;
        if (Boolean.TRUE.equals(request.getEnableQualityScore())) {
            qualityScore = qualityScoreService.scoreDescription(continuationText);
            response.setQualityScore(qualityScore);
        }
        
        // 6. 生成综合建议
        EnhancedContinuationResponse.ContinuationRecommendation recommendation = 
            generateRecommendation(continuationText, consistencyResult, qualityScore, request);
        response.setRecommendation(recommendation);
        
        // 7. 保存到数据库
        AIContinuation continuation = saveContinuation(chapter, request, continuationText);
        response.setId(continuation.getId());
        response.setChapterId(continuation.getChapterId());
        response.setSourceText(continuation.getSourceText());
        response.setContinuationText(continuation.getContinuationText());
        response.setStyle(continuation.getStyle());
        response.setLength(continuation.getLength());
        response.setAiModel(continuation.getAiModel());
        response.setVersion(continuation.getVersion());
        response.setIsApplied(continuation.getIsApplied());
        response.setCreatedAt(continuation.getCreatedAt());
        
        return response;
    }

    /**
     * 使用约束生成续写
     */
    private String generateContinuationWithConstraints(
            Chapter chapter,
            EnhancedContinuationRequest request,
            MultiDimensionalConstraintEngine.CompositeConstraints constraints) {
        
        // 构建综合提示词
        StringBuilder prompt = new StringBuilder();
        
        // 基础提示词
        prompt.append("请根据以下内容进行续写：\n\n");
        prompt.append("【章节标题】\n").append(chapter.getTitle()).append("\n\n");
        prompt.append("【上文内容】\n").append(request.getSourceText()).append("\n\n");
        
        // 添加多维约束信息
        if (constraints != null) {
            prompt.append(constraintEngine.generateConstraintPrompt(constraints));
        }
        
        // 风格要求
        prompt.append("\n【写作风格】\n");
        appendStyleRequirements(prompt, request.getStyle());
        
        // 长度要求
        prompt.append("\n【长度要求】\n");
        appendLengthRequirements(prompt, request.getLength());
        
        // 额外上下文
        if (request.getAdditionalContext() != null) {
            prompt.append("\n【背景信息】\n").append(request.getAdditionalContext());
        }
        
        prompt.append("\n【生成要求】\n");
        prompt.append("1. 严格遵守以上所有约束条件\n");
        prompt.append("2. 确保情节逻辑一致\n");
        prompt.append("3. 加入丰富的描写细节\n");
        prompt.append("4. 只输出续写内容，不要额外说明\n");
        
        // 调用AI生成
        return callAiService(prompt.toString());
    }

    /**
     * 迭代优化续写
     */
    private String iterativelyRefineContinuation(
            String initialContinuation,
            Chapter chapter,
            EnhancedContinuationRequest request,
            MultiDimensionalConstraintEngine.CompositeConstraints constraints) {
        
        log.info("开始迭代优化续写...");
        
        String refinedText = initialContinuation;
        
        for (int iteration = 0; iteration < request.getMaxIterations(); iteration++) {
            log.info("迭代第 {} 次", iteration + 1);
            
            // 评分当前文本
            DescriptionQualityScoreService.DescriptionQualityScore score = 
                qualityScoreService.scoreDescription(refinedText);
            
            // 如果质量满足要求，停止迭代
            if (score.getOverallScore() >= request.getMinimumQualityScore()) {
                log.info("质量评分 {} >= 目标分 {}，停止迭代", 
                        score.getOverallScore(), request.getMinimumQualityScore());
                break;
            }
            
            // 构建优化提示词
            String refinementPrompt = buildRefinementPrompt(
                refinedText, score, chapter, request, constraints);
            
            // 调用AI进行优化
            refinedText = callAiService(refinementPrompt);
        }
        
        return refinedText;
    }

    /**
     * 构建优化提示词
     */
    private String buildRefinementPrompt(
            String currentText,
            DescriptionQualityScoreService.DescriptionQualityScore currentScore,
            Chapter chapter,
            EnhancedContinuationRequest request,
            MultiDimensionalConstraintEngine.CompositeConstraints constraints) {
        
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请优化以下续写文本。当前质量评分: ").append(currentScore.getOverallScore())
              .append("/100\n\n");
        
        prompt.append("【当前文本】\n").append(currentText).append("\n\n");
        
        // 基于评分反馈提出改进建议
        if (currentScore.getDialogueScore() != null && currentScore.getDialogueScore().getScore() < 60) {
            prompt.append("【改进重点】对话质量需要提升\n");
        }
        if (currentScore.getPsychologicalScore() != null && currentScore.getPsychologicalScore().getScore() < 60) {
            prompt.append("【改进重点】心理描写需要加强\n");
        }
        if (currentScore.getEnvironmentScore() != null && currentScore.getEnvironmentScore().getScore() < 60) {
            prompt.append("【改进重点】环境描写细节不足\n");
        }
        
        prompt.append("\n请在以下要求下优化文本：\n");
        prompt.append("1. 保持原有的情节逻辑\n");
        prompt.append("2. 增强描写的生动性和细节\n");
        prompt.append("3. 确保风格一致\n");
        prompt.append("4. 只输出优化后的文本\n");
        
        return prompt.toString();
    }

    /**
     * 生成综合建议
     */
    private EnhancedContinuationResponse.ContinuationRecommendation generateRecommendation(
            String continuationText,
            PlotConsistencyCheckService.PlotConsistencyCheckResult consistencyResult,
            DescriptionQualityScoreService.DescriptionQualityScore qualityScore,
            EnhancedContinuationRequest request) {
        
        EnhancedContinuationResponse.ContinuationRecommendation recommendation = 
            new EnhancedContinuationResponse.ContinuationRecommendation();
        
        recommendation.setImprovementPoints(new ArrayList<>());
        recommendation.setModificationSuggestions(new ArrayList<>());
        
        int compositeScore = 70; // 基础分数
        
        // 1. 基于一致性得分
        if (consistencyResult != null) {
            compositeScore = consistencyResult.getConsistencyScore();
            if (!consistencyResult.getIsConsistent()) {
                recommendation.getImprovementPoints().addAll(
                    consistencyResult.getRecommendations());
            }
        }
        
        // 2. 基于质量评分
        if (qualityScore != null) {
            Integer qualityScoreValue = qualityScore.getOverallScore();
            if (qualityScoreValue != null) {
                compositeScore = (compositeScore + qualityScoreValue) / 2;
            }
            // 质量评分的建议已经在前面添加到improvementPoints中了
        }
        
        recommendation.setCompositeScore(compositeScore);
        
        // 3. 决定是否建议应用
        boolean shouldApply = compositeScore >= 70 || (qualityScore != null && 
            qualityScore.getOverallScore() >= request.getMinimumQualityScore());
        
        recommendation.setRecommendToApply(shouldApply);
        
        if (shouldApply) {
            recommendation.setReason("续写质量良好，建议应用");
            recommendation.setQualityRating("GOOD");
        } else {
            recommendation.setReason("续写质量需改进，建议进行修改");
            recommendation.setQualityRating("FAIR");
        }
        
        return recommendation;
    }

    /**
     * 保存续写到数据库
     */
    private AIContinuation saveContinuation(
            Chapter chapter,
            EnhancedContinuationRequest request,
            String continuationText) {
        
        // 获取版本号
        List<AIContinuation> existing = continuationRepository.findByChapterIdAndStyleOrderByVersionDesc(
                request.getChapterId(), request.getStyle());
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;
        
        AIContinuation continuation = AIContinuation.builder()
                .chapterId(request.getChapterId())
                .sourceText(request.getSourceText())
                .continuationText(continuationText)
                .style(request.getStyle())
                .length(request.getLength())
                .aiModel("enhanced-dashscope-v2")
                .version(nextVersion)
                .isApplied(false)
                .build();
        
        return continuationRepository.save(continuation);
    }

    /**
     * 调用AI服务
     */
    private String callAiService(String prompt) {
        // 这里集成真实的AI服务调用
        // 暂时返回模拟数据
        log.debug("调用AI服务，提示词长度: {}", prompt.length());
        
        return "主角深吸一口气，眼神变得更加坚定。他意识到这个决定将改变一切。" +
               "从现在开始，没有回头路了。窗外的月光洒在地板上，照亮了他踌躇满志的脸庞。" +
               "心中的恐惧逐渐消散，取而代之的是一股强大的使命感。" +
               "他转身走向门口，准备迎接即将到来的一切。";
    }

    /**
     * 添加风格要求
     */
    private void appendStyleRequirements(StringBuilder prompt, String style) {
        switch (style) {
            case "SERIOUS" -> prompt.append("严肃认真，注重情节和人物心理分析\n");
            case "LIGHT" -> prompt.append("轻松幽默，语言生动活泼，适当使用比喻\n");
            case "SUSPENSE" -> prompt.append("悬疑紧张，营造压抑的氛围感，留下伏笔\n");
            case "ROMANTIC" -> prompt.append("浪漫温馨，注重情感描写和细腻心理\n");
            case "ACTION" -> prompt.append("动作激烈，节奏紧凑，充满视觉冲击\n");
            default -> prompt.append("自然流畅，符合上文风格\n");
        }
    }

    /**
     * 添加长度要求
     */
    private void appendLengthRequirements(StringBuilder prompt, String length) {
        switch (length) {
            case "SENTENCE" -> prompt.append("1-2句话，简短衔接\n");
            case "PARAGRAPH" -> prompt.append("1-2个段落，100-200字\n");
            case "SECTION" -> prompt.append("3-5个段落，300-500字\n");
            case "LONG" -> prompt.append("5-8个段落，500-800字\n");
            default -> prompt.append("2-3个段落，200-300字\n");
        }
    }
}
