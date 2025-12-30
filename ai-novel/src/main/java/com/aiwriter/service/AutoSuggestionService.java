package com.aiwriter.service;

import com.aiwriter.dto.AutoSuggestionDto;
import com.aiwriter.dto.SuggestionStatistics;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
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
 * 智能推荐服务
 * 
 * 综合分析小说的文风、伏笔、章节分析等数据，生成智能化的写作建议
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoSuggestionService {

    private final AutoSuggestionRepository suggestionRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final PlotHookRepository plotHookRepository;
    private final ChapterAnalysisResultRepository analysisRepository;
    private final WritingStyleRepository writingStyleRepository;
    private final ObjectMapper objectMapper;

    /**
     * 为小说生成全面的智能推荐
     */
    @Transactional
    public List<AutoSuggestionDto> generateSuggestionsForNovel(Long novelId) {
        log.info("开始为小说 {} 生成智能推荐", novelId);
        long startTime = System.currentTimeMillis();

        // 清理过期推荐
        cleanupExpiredSuggestions(novelId);

        List<AutoSuggestion> suggestions = new ArrayList<>();

        // 1. 伏笔相关推荐
        suggestions.addAll(generatePlotHookSuggestions(novelId));

        // 2. 文风相关推荐
        suggestions.addAll(generateStyleSuggestions(novelId));

        // 3. 章节质量相关推荐
        suggestions.addAll(generateQualitySuggestions(novelId));

        // 4. 情节推荐
        suggestions.addAll(generatePlotSuggestions(novelId));

        // 5. 角色发展推荐
        suggestions.addAll(generateCharacterSuggestions(novelId));

        // 6. 节奏调整推荐
        suggestions.addAll(generatePacingSuggestions(novelId));

        // 7. 冲突强化推荐
        suggestions.addAll(generateConflictSuggestions(novelId));

        // 8. 情感曲线推荐
        suggestions.addAll(generateEmotionSuggestions(novelId));

        // 保存所有推荐
        List<AutoSuggestion> saved = suggestionRepository.saveAll(suggestions);

        long duration = System.currentTimeMillis() - startTime;
        log.info("为小说 {} 生成了 {} 条推荐，耗时 {}ms", novelId, saved.size(), duration);

        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 为指定章节生成推荐
     */
    @Transactional
    public List<AutoSuggestionDto> generateSuggestionsForChapter(Long novelId, Long chapterId) {
        log.info("开始为章节 {} 生成智能推荐", chapterId);

        List<AutoSuggestion> suggestions = new ArrayList<>();

        // 获取章节分析结果
        Optional<ChapterAnalysisResult> analysisOpt = analysisRepository.findByChapterId(chapterId);
        if (analysisOpt.isEmpty()) {
            log.warn("章节 {} 没有分析结果", chapterId);
            return Collections.emptyList();
        }

        ChapterAnalysisResult analysis = analysisOpt.get();

        // 1. 质量问题推荐
        if (analysis.getQualityScore() != null && analysis.getQualityScore() < 6) {
            suggestions.add(createQualityImprovementSuggestion(novelId, chapterId, analysis));
        }

        // 2. 节奏问题推荐
        if ("TOO_FAST".equals(analysis.getPacing()) || "TOO_SLOW".equals(analysis.getPacing())) {
            suggestions.add(createPacingAdjustmentSuggestion(novelId, chapterId, analysis));
        }

        // 3. 冲突强度推荐
        if (analysis.getConflictIntensity() != null && analysis.getConflictIntensity() < 5) {
            suggestions.add(createConflictEnhancementSuggestion(novelId, chapterId, analysis));
        }

        // 4. 情感单调推荐
        if ("FLAT".equals(analysis.getEmotionTrend())) {
            suggestions.add(createEmotionEnhancementSuggestion(novelId, chapterId, analysis));
        }

        // 保存推荐
        List<AutoSuggestion> saved = suggestionRepository.saveAll(suggestions);
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 生成伏笔相关推荐
     */
    private List<AutoSuggestion> generatePlotHookSuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        // 获取超期伏笔
        int currentChapter = getCurrentChapterNumber(novelId);
        List<PlotHook> overdueHooks = plotHookRepository.findOverdueHooks(novelId, currentChapter);
        for (PlotHook hook : overdueHooks) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.HOOK);
            suggestion.setTitle("伏笔超期提醒：" + hook.getTitle());
            suggestion.setContent(String.format(
                "伏笔「%s」已经埋设了 %d 章，建议在接下来的章节中触发或解决。\n\n伏笔描述：%s",
                hook.getTitle(),
                getCurrentChapterNumber(novelId) - hook.getPlantedInChapter(),
                hook.getDescription()
            ));
            suggestion.setPriority(Math.min(10, hook.getPriority() + 2));
            suggestion.setRelevanceScore(0.9);
            suggestion.setBasis(toJson(Arrays.asList(
                "伏笔已超期",
                "优先级: " + hook.getPriority(),
                "埋设章节: " + hook.getPlantedInChapter()
            )));
            suggestion.setReferenceType("PLOT_HOOK");
            suggestion.setReferenceId(hook.getId());
            
            AutoSuggestionDto.ExpectedImpact impact = new AutoSuggestionDto.ExpectedImpact();
            impact.setDimension("情节连贯性");
            impact.setDescription("解决超期伏笔，提升故事完整性");
            impact.setScoreChange(2);
            suggestion.setExpectedImpact(toJson(impact));

            suggestions.add(suggestion);
        }

        // 检查待铺垫的高优先级伏笔
        List<PlotHook> pendingHooks = plotHookRepository.findByNovelIdAndStatusOrderByPlantedInChapterAsc(
            novelId, PlotHook.Status.PENDING
        ).stream()
         .filter(h -> h.getPriority() >= 7)
         .collect(Collectors.toList());

        for (PlotHook hook : pendingHooks) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.HOOK);
            suggestion.setTitle("高优先级伏笔待埋设：" + hook.getTitle());
            suggestion.setContent(String.format(
                "高优先级伏笔「%s」尚未埋设，建议在第 %d 章埋设。\n\n伏笔描述：%s\n\n埋设建议：可以通过场景描写、对话暗示或角色回忆等方式自然引入。",
                hook.getTitle(),
                hook.getPlantedInChapter(),
                hook.getDescription()
            ));
            suggestion.setPriority(hook.getPriority());
            suggestion.setRelevanceScore(0.85);
            suggestion.setBasis(toJson(Arrays.asList(
                "高优先级伏笔",
                "优先级: " + hook.getPriority(),
                "计划章节: " + hook.getPlantedInChapter()
            )));
            suggestion.setReferenceType("PLOT_HOOK");
            suggestion.setReferenceId(hook.getId());

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 生成文风相关推荐
     */
    private List<AutoSuggestion> generateStyleSuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        // TODO: 风格检查功能待实现
        return suggestions;
        
        // Optional<WritingStyle> styleOpt = writingStyleRepository.findByNovelId(novelId);
        // if (styleOpt.isEmpty()) {
        //     return suggestions;
        // }
        //
        // WritingStyle style = styleOpt.get();
        //
        // // 检查最近章节的文风偏离
        // List<ChapterAnalysisResult> recentAnalyses = analysisRepository
        //     .findByNovelIdOrderByChapterNumberDesc(novelId).stream()
        //     .limit(3)
        //     .collect(Collectors.toList());
        //
        // // 风格检查逻辑...
        //
        // if (!recentAnalyses.isEmpty()) {
        //     // 检查节奏一致性（先跳过，因为 WritingStyle 没有 pacing 字段）
        //     // TODO: 后续可以根据实际字段调整
        // }
    }

    /**
     * 生成质量相关推荐
     */
    private List<AutoSuggestion> generateQualitySuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        // 获取最近5章的分析结果
        List<ChapterAnalysisResult> recentAnalyses = analysisRepository
            .findByNovelIdOrderByChapterNumberDesc(novelId).stream()
            .limit(5)
            .collect(Collectors.toList());

        if (recentAnalyses.isEmpty()) {
            return suggestions;
        }

        // 计算平均质量分
        double avgQuality = recentAnalyses.stream()
            .filter(a -> a.getQualityScore() != null)
            .mapToInt(ChapterAnalysisResult::getQualityScore)
            .average()
            .orElse(0.0);

        // 如果平均质量低于6分，生成推荐
        if (avgQuality < 6.0) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.QUALITY);
            suggestion.setTitle("整体质量待提升");
            suggestion.setContent(String.format(
                "最近5章的平均质量评分为 %.1f/10，建议从以下方面改进：\n\n" +
                "1. 增强情节张力和冲突\n" +
                "2. 丰富角色描写和对话\n" +
                "3. 优化场景细节和氛围营造\n" +
                "4. 提升语言表达的文学性",
                avgQuality
            ));
            suggestion.setPriority(8);
            suggestion.setRelevanceScore(0.9);
            suggestion.setBasis(toJson(Arrays.asList(
                "平均质量: " + String.format("%.1f", avgQuality),
                "分析章节数: 5",
                "评分标准: 10分制"
            )));

            AutoSuggestionDto.ExpectedImpact impact = new AutoSuggestionDto.ExpectedImpact();
            impact.setDimension("整体质量");
            impact.setDescription("提升章节质量，增强读者体验");
            impact.setScoreChange(2);
            suggestion.setExpectedImpact(toJson(impact));

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 生成情节推荐
     */
    private List<AutoSuggestion> generatePlotSuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        // 获取最近章节
        List<ChapterAnalysisResult> recentAnalyses = analysisRepository
            .findByNovelIdOrderByChapterNumberDesc(novelId).stream()
            .limit(3)
            .collect(Collectors.toList());

        if (recentAnalyses.isEmpty()) {
            return suggestions;
        }

        // 检查是否缺少情节点
        long chaptersWithFewPlotPoints = recentAnalyses.stream()
            .filter(a -> {
                try {
                    List<Map<String, Object>> plotPoints = parseJsonList2(a.getPlotPoints());
                    return plotPoints == null || plotPoints.size() < 2;
                } catch (Exception e) {
                    return true;
                }
            })
            .count();

        if (chaptersWithFewPlotPoints >= 2) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.PLOT);
            suggestion.setTitle("情节点不足");
            suggestion.setContent(
                "检测到最近章节的情节点较少，建议增加关键事件和转折点，以推动故事发展。\n\n" +
                "推荐做法：\n" +
                "1. 引入新的冲突或挑战\n" +
                "2. 揭示重要信息或秘密\n" +
                "3. 制造角色间的矛盾\n" +
                "4. 推进主线或支线情节"
            );
            suggestion.setPriority(7);
            suggestion.setRelevanceScore(0.8);
            suggestion.setBasis(toJson(Arrays.asList(
                "情节点不足章节数: " + chaptersWithFewPlotPoints,
                "分析章节数: 3",
                "建议情节点数: ≥2"
            )));

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 生成角色发展推荐
     */
    private List<AutoSuggestion> generateCharacterSuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        // 获取最近10章
        List<ChapterAnalysisResult> recentAnalyses = analysisRepository
            .findByNovelIdOrderByChapterNumberDesc(novelId).stream()
            .limit(10)
            .collect(Collectors.toList());

        if (recentAnalyses.isEmpty()) {
            return suggestions;
        }

        // 检查角色弧光
        long flatArcCount = recentAnalyses.stream()
            .filter(a -> {
                try {
                    List<Map<String, Object>> arcs = parseJsonList2(a.getCharacterArcs());
                    if (arcs == null) return false;
                    return arcs.stream()
                        .anyMatch(arc -> "FLAT".equals(arc.get("type")));
                } catch (Exception e) {
                    return false;
                }
            })
            .count();

        if (flatArcCount >= 5) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.CHARACTER);
            suggestion.setTitle("角色发展停滞");
            suggestion.setContent(
                "检测到主要角色在最近章节中发展较为平缓，建议加强角色成长和变化。\n\n" +
                "推荐做法：\n" +
                "1. 让角色面对重要抉择\n" +
                "2. 通过事件改变角色观念\n" +
                "3. 深化角色关系和互动\n" +
                "4. 展现角色的内心挣扎"
            );
            suggestion.setPriority(6);
            suggestion.setRelevanceScore(0.75);
            suggestion.setBasis(toJson(Arrays.asList(
                "平缓发展章节数: " + flatArcCount,
                "分析章节数: 10",
                "建议: 增强角色弧光"
            )));

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 生成节奏调整推荐
     */
    private List<AutoSuggestion> generatePacingSuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        List<ChapterAnalysisResult> recentAnalyses = analysisRepository
            .findByNovelIdOrderByChapterNumberDesc(novelId).stream()
            .limit(5)
            .collect(Collectors.toList());

        if (recentAnalyses.size() < 3) {
            return suggestions;
        }

        // 检查节奏单调（连续3章相同节奏）
        boolean monotonousPacing = true;
        String firstPacing = recentAnalyses.get(0).getPacing();
        for (int i = 1; i < Math.min(3, recentAnalyses.size()); i++) {
            if (!firstPacing.equals(recentAnalyses.get(i).getPacing())) {
                monotonousPacing = false;
                break;
            }
        }

        if (monotonousPacing) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.PACING);
            suggestion.setTitle("节奏单调");
            suggestion.setContent(String.format(
                "连续3章节奏保持「%s」，建议适当调整节奏以增加变化感。\n\n" +
                "推荐做法：\n" +
                "1. 快节奏后安排舒缓段落\n" +
                "2. 慢节奏后增加紧张情节\n" +
                "3. 交替使用不同节奏营造起伏",
                firstPacing
            ));
            suggestion.setPriority(5);
            suggestion.setRelevanceScore(0.7);
            suggestion.setBasis(toJson(Arrays.asList(
                "当前节奏: " + firstPacing,
                "连续章节数: 3",
                "建议: 调整节奏变化"
            )));

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 生成冲突强化推荐
     */
    private List<AutoSuggestion> generateConflictSuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        List<ChapterAnalysisResult> recentAnalyses = analysisRepository
            .findByNovelIdOrderByChapterNumberDesc(novelId).stream()
            .limit(5)
            .collect(Collectors.toList());

        if (recentAnalyses.isEmpty()) {
            return suggestions;
        }

        // 计算平均冲突强度
        double avgConflict = recentAnalyses.stream()
            .filter(a -> a.getConflictIntensity() != null)
            .mapToInt(ChapterAnalysisResult::getConflictIntensity)
            .average()
            .orElse(0.0);

        if (avgConflict < 5.0) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.CONFLICT);
            suggestion.setTitle("冲突强度不足");
            suggestion.setContent(String.format(
                "最近章节的平均冲突强度为 %.1f/10，建议增强故事张力。\n\n" +
                "推荐做法：\n" +
                "1. 引入更强的对立力量\n" +
                "2. 提高事件的风险和代价\n" +
                "3. 制造角色间的矛盾冲突\n" +
                "4. 设置更紧迫的时间压力",
                avgConflict
            ));
            suggestion.setPriority(7);
            suggestion.setRelevanceScore(0.8);
            suggestion.setBasis(toJson(Arrays.asList(
                "平均冲突强度: " + String.format("%.1f", avgConflict),
                "分析章节数: 5",
                "建议强度: ≥5.0"
            )));

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 生成情感曲线推荐
     */
    private List<AutoSuggestion> generateEmotionSuggestions(Long novelId) {
        List<AutoSuggestion> suggestions = new ArrayList<>();

        List<ChapterAnalysisResult> recentAnalyses = analysisRepository
            .findByNovelIdOrderByChapterNumberDesc(novelId).stream()
            .limit(5)
            .collect(Collectors.toList());

        if (recentAnalyses.isEmpty()) {
            return suggestions;
        }

        // 检查情感单调
        long flatEmotionCount = recentAnalyses.stream()
            .filter(a -> "FLAT".equals(a.getEmotionTrend()))
            .count();

        if (flatEmotionCount >= 3) {
            AutoSuggestion suggestion = new AutoSuggestion();
            suggestion.setNovelId(novelId);
            suggestion.setType(AutoSuggestion.SuggestionType.EMOTION);
            suggestion.setTitle("情感曲线平坦");
            suggestion.setContent(
                "最近章节的情感变化较为平淡，建议增强情感起伏。\n\n" +
                "推荐做法：\n" +
                "1. 安排情感转折点\n" +
                "2. 增加角色情感冲突\n" +
                "3. 营造情感高潮\n" +
                "4. 运用情感对比"
            );
            suggestion.setPriority(6);
            suggestion.setRelevanceScore(0.75);
            suggestion.setBasis(toJson(Arrays.asList(
                "平坦情感章节数: " + flatEmotionCount,
                "分析章节数: 5",
                "建议: 增强情感起伏"
            )));

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 创建质量改进推荐
     */
    private AutoSuggestion createQualityImprovementSuggestion(
            Long novelId, Long chapterId, ChapterAnalysisResult analysis) {
        
        AutoSuggestion suggestion = new AutoSuggestion();
        suggestion.setNovelId(novelId);
        suggestion.setChapterId(chapterId);
        suggestion.setChapterNumber(analysis.getChapterNumber());
        suggestion.setType(AutoSuggestion.SuggestionType.QUALITY);
        suggestion.setTitle("章节质量待提升");
        suggestion.setContent(String.format(
            "第 %d 章的质量评分为 %d/10，建议重点改进：\n\n" +
            "• 可读性: %d/10\n" +
            "• 创意性: %d/10\n" +
            "• 连贯性: %d/10\n\n" +
            "具体改进建议：\n%s",
            analysis.getChapterNumber(),
            analysis.getQualityScore(),
            analysis.getReadabilityScore(),
            analysis.getCreativityScore(),
            analysis.getCoherenceScore(),
            extractImprovementSuggestions(analysis)
        ));
        suggestion.setPriority(8);
        suggestion.setRelevanceScore(0.9);
        suggestion.setReferenceType("CHAPTER_ANALYSIS");
        suggestion.setReferenceId(analysis.getId());

        return suggestion;
    }

    /**
     * 创建节奏调整推荐
     */
    private AutoSuggestion createPacingAdjustmentSuggestion(
            Long novelId, Long chapterId, ChapterAnalysisResult analysis) {
        
        AutoSuggestion suggestion = new AutoSuggestion();
        suggestion.setNovelId(novelId);
        suggestion.setChapterId(chapterId);
        suggestion.setChapterNumber(analysis.getChapterNumber());
        suggestion.setType(AutoSuggestion.SuggestionType.PACING);
        suggestion.setTitle("节奏调整建议");
        suggestion.setContent(String.format(
            "第 %d 章节奏为「%s」，建议调整：\n\n" +
            "当前占比：\n" +
            "• 动作: %.0f%%\n" +
            "• 对话: %.0f%%\n" +
            "• 描写: %.0f%%\n" +
            "• 独白: %.0f%%\n\n" +
            "%s",
            analysis.getChapterNumber(),
            analysis.getPacing(),
            analysis.getActionRatio() * 100,
            analysis.getDialogueRatio() * 100,
            analysis.getDescriptionRatio() * 100,
            analysis.getIntrospectionRatio() * 100,
            "TOO_FAST".equals(analysis.getPacing()) 
                ? "建议增加场景描写和角色内心活动，放缓节奏"
                : "建议增加动作和对话，加快节奏"
        ));
        suggestion.setPriority(7);
        suggestion.setRelevanceScore(0.85);

        return suggestion;
    }

    /**
     * 创建冲突增强推荐
     */
    private AutoSuggestion createConflictEnhancementSuggestion(
            Long novelId, Long chapterId, ChapterAnalysisResult analysis) {
        
        AutoSuggestion suggestion = new AutoSuggestion();
        suggestion.setNovelId(novelId);
        suggestion.setChapterId(chapterId);
        suggestion.setChapterNumber(analysis.getChapterNumber());
        suggestion.setType(AutoSuggestion.SuggestionType.CONFLICT);
        suggestion.setTitle("冲突强度不足");
        suggestion.setContent(String.format(
            "第 %d 章冲突强度仅为 %d/10，建议增强故事张力。\n\n" +
            "推荐做法：\n" +
            "1. 提高事件的风险和代价\n" +
            "2. 引入更强的对立力量\n" +
            "3. 制造时间压力\n" +
            "4. 增加角色间的矛盾",
            analysis.getChapterNumber(),
            analysis.getConflictIntensity()
        ));
        suggestion.setPriority(7);
        suggestion.setRelevanceScore(0.8);

        return suggestion;
    }

    /**
     * 创建情感增强推荐
     */
    private AutoSuggestion createEmotionEnhancementSuggestion(
            Long novelId, Long chapterId, ChapterAnalysisResult analysis) {
        
        AutoSuggestion suggestion = new AutoSuggestion();
        suggestion.setNovelId(novelId);
        suggestion.setChapterId(chapterId);
        suggestion.setChapterNumber(analysis.getChapterNumber());
        suggestion.setType(AutoSuggestion.SuggestionType.EMOTION);
        suggestion.setTitle("情感曲线平淡");
        suggestion.setContent(String.format(
            "第 %d 章情感变化较为平坦，建议增强情感起伏。\n\n" +
            "推荐做法：\n" +
            "1. 安排情感转折点\n" +
            "2. 增加角色情感冲突\n" +
            "3. 营造情感高潮\n" +
            "4. 运用情感对比",
            analysis.getChapterNumber()
        ));
        suggestion.setPriority(6);
        suggestion.setRelevanceScore(0.75);

        return suggestion;
    }

    /**
     * 获取推荐列表
     */
    public List<AutoSuggestionDto> getSuggestionsByNovel(Long novelId) {
        return suggestionRepository.findByNovelIdOrderByPriorityDescRelevanceScoreDesc(novelId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 获取活跃推荐
     */
    public List<AutoSuggestionDto> getActiveSuggestions(Long novelId) {
        return suggestionRepository.findByNovelIdAndStatusOrderByPriorityDescRelevanceScoreDesc(
            novelId, AutoSuggestion.SuggestionStatus.ACTIVE
        ).stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 获取指定类型的推荐
     */
    public List<AutoSuggestionDto> getSuggestionsByType(
            Long novelId, AutoSuggestion.SuggestionType type) {
        return suggestionRepository.findByNovelIdAndTypeAndStatusOrderByPriorityDescRelevanceScoreDesc(
            novelId, type, AutoSuggestion.SuggestionStatus.ACTIVE
        ).stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 获取高优先级推荐
     */
    public List<AutoSuggestionDto> getHighPrioritySuggestions(Long novelId, Integer threshold) {
        return suggestionRepository.findHighPrioritySuggestions(novelId, threshold)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 采纳推荐
     */
    @Transactional
    public AutoSuggestionDto acceptSuggestion(Long id, String feedback) {
        AutoSuggestion suggestion = suggestionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("推荐不存在"));

        suggestion.setStatus(AutoSuggestion.SuggestionStatus.ACCEPTED);
        suggestion.setAcceptedAt(LocalDateTime.now());
        if (feedback != null) {
            suggestion.setFeedback(feedback);
        }

        return toDto(suggestionRepository.save(suggestion));
    }

    /**
     * 拒绝推荐
     */
    @Transactional
    public AutoSuggestionDto rejectSuggestion(Long id, String reason) {
        AutoSuggestion suggestion = suggestionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("推荐不存在"));

        suggestion.setStatus(AutoSuggestion.SuggestionStatus.REJECTED);
        suggestion.setRejectedAt(LocalDateTime.now());
        if (reason != null) {
            suggestion.setFeedback(reason);
        }

        return toDto(suggestionRepository.save(suggestion));
    }

    /**
     * 获取统计信息
     */
    public SuggestionStatistics getStatistics(Long novelId) {
        SuggestionStatistics stats = new SuggestionStatistics();
        stats.setNovelId(novelId);

        List<AutoSuggestion> all = suggestionRepository.findByNovelIdOrderByPriorityDescRelevanceScoreDesc(novelId);

        stats.setTotalCount(all.size());
        stats.setActiveCount((int) all.stream().filter(s -> s.getStatus() == AutoSuggestion.SuggestionStatus.ACTIVE).count());
        stats.setAcceptedCount((int) all.stream().filter(s -> s.getStatus() == AutoSuggestion.SuggestionStatus.ACCEPTED).count());
        stats.setRejectedCount((int) all.stream().filter(s -> s.getStatus() == AutoSuggestion.SuggestionStatus.REJECTED).count());
        stats.setExpiredCount((int) all.stream().filter(s -> s.getStatus() == AutoSuggestion.SuggestionStatus.EXPIRED).count());

        // 分类统计
        Map<String, Integer> byType = new HashMap<>();
        for (AutoSuggestion.SuggestionType type : AutoSuggestion.SuggestionType.values()) {
            int count = (int) all.stream()
                .filter(s -> s.getType() == type && s.getStatus() == AutoSuggestion.SuggestionStatus.ACTIVE)
                .count();
            byType.put(type.name(), count);
        }
        stats.setCountByType(byType);

        // 优先级分布
        List<AutoSuggestion> active = all.stream()
            .filter(s -> s.getStatus() == AutoSuggestion.SuggestionStatus.ACTIVE)
            .collect(Collectors.toList());

        stats.setHighPriorityCount((int) active.stream().filter(s -> s.getPriority() >= 8).count());
        stats.setMediumPriorityCount((int) active.stream().filter(s -> s.getPriority() >= 5 && s.getPriority() < 8).count());
        stats.setLowPriorityCount((int) active.stream().filter(s -> s.getPriority() < 5).count());

        // 平均指标
        stats.setAvgPriority(active.stream().mapToInt(AutoSuggestion::getPriority).average().orElse(0.0));
        stats.setAvgRelevanceScore(active.stream().mapToDouble(AutoSuggestion::getRelevanceScore).average().orElse(0.0));

        // 采纳率
        int acceptedCount = stats.getAcceptedCount();
        int rejectedCount = stats.getRejectedCount();
        if (acceptedCount + rejectedCount > 0) {
            stats.setAcceptanceRate((double) acceptedCount / (acceptedCount + rejectedCount));
        } else {
            stats.setAcceptanceRate(0.0);
        }

        // 质量评分（综合相关性和采纳率）
        stats.setQualityScore(stats.getAvgRelevanceScore() * 0.5 + stats.getAcceptanceRate() * 0.5);

        return stats;
    }

    /**
     * 清理过期推荐
     */
    @Transactional
    public int cleanupExpiredSuggestions(Long novelId) {
        List<AutoSuggestion> expired = suggestionRepository.findExpiredSuggestions(
            novelId, LocalDateTime.now()
        );

        for (AutoSuggestion suggestion : expired) {
            suggestion.setStatus(AutoSuggestion.SuggestionStatus.EXPIRED);
        }

        suggestionRepository.saveAll(expired);
        return expired.size();
    }

    // ==================== 辅助方法 ====================

    private int getCurrentChapterNumber(Long novelId) {
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId).stream()
            .max((c1, c2) -> Integer.compare(c1.getChapterNumber(), c2.getChapterNumber()))
            .map(Chapter::getChapterNumber)
            .orElse(0);
    }

    private String extractImprovementSuggestions(ChapterAnalysisResult analysis) {
        try {
            List<Map<String, Object>> suggestions = parseJsonList2(analysis.getImprovementSuggestions());
            if (suggestions == null || suggestions.isEmpty()) {
                return "暂无具体建议";
            }
            return suggestions.stream()
                .map(s -> "• " + s.get("suggestion"))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "暂无具体建议";
        }
    }

    private AutoSuggestionDto toDto(AutoSuggestion entity) {
        AutoSuggestionDto dto = new AutoSuggestionDto();
        dto.setId(entity.getId());
        dto.setNovelId(entity.getNovelId());
        dto.setChapterId(entity.getChapterId());
        dto.setChapterNumber(entity.getChapterNumber());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setPriority(entity.getPriority());
        dto.setRelevanceScore(entity.getRelevanceScore());
        dto.setStatus(entity.getStatus());
        dto.setReferenceId(entity.getReferenceId());
        dto.setReferenceType(entity.getReferenceType());
        dto.setFeedback(entity.getFeedback());
        dto.setAcceptedAt(entity.getAcceptedAt());
        dto.setRejectedAt(entity.getRejectedAt());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setCreatedAt(entity.getCreatedAt());

        // 解析 JSON 字段
        dto.setBasis(parseJsonList(entity.getBasis()));
        dto.setExpectedImpact(parseJson(entity.getExpectedImpact(), AutoSuggestionDto.ExpectedImpact.class));

        return dto;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }

    private <T> T parseJson(String json, Class<T> clazz) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> parseJsonList2(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
