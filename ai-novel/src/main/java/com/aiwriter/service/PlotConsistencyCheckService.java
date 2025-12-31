package com.aiwriter.service;

import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 情节一致性检查服务
 * 核心功能：检查续写内容与已有情节的逻辑一致性和冲突检测
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlotConsistencyCheckService {

    private final ChapterRepository chapterRepository;
    private final OutlineRepository outlineRepository;
    private final CharacterRepository characterRepository;
    private final SceneRepository sceneRepository;
    private final NovelRepository novelRepository;
    private final ContentAnalysisService contentAnalysisService;
    private final CharacterGrowthService characterGrowthService;

    /**
     * 完整的情节一致性检查结果
     */
    @Data
    public static class PlotConsistencyCheckResult {
        // 总体一致性得分 (0-100)
        private Integer consistencyScore;
        
        // 是否通过检查
        private Boolean isConsistent;
        
        // 冲突列表
        private List<PlotConflict> conflicts;
        
        // 逻辑问题
        private List<LogicalIssue> logicalIssues;
        
        // 风格一致性
        private StyleConsistency styleConsistency;
        
        // 角色行为一致性
        private List<CharacterConsistencyCheck> characterConsistency;
        
        // 时间线检查
        private TimelineCheck timelineCheck;
        
        // 详细建议
        private List<String> recommendations;
        
        public PlotConsistencyCheckResult() {
            this.conflicts = new ArrayList<>();
            this.logicalIssues = new ArrayList<>();
            this.characterConsistency = new ArrayList<>();
            this.recommendations = new ArrayList<>();
        }
    }

    /**
     * 情节冲突
     */
    @Data
    public static class PlotConflict {
        private String type; // CONTRADICTION(矛盾), REPETITION(重复), TIMELINE_ERROR(时间错误)
        private String description;
        private Integer severity; // 1-严重, 2-中等, 3-轻微
        private String previousContent;
        private String newContent;
    }

    /**
     * 逻辑问题
     */
    @Data
    public static class LogicalIssue {
        private String issue;
        private String explanation;
        private Integer severity;
    }

    /**
     * 风格一致性
     */
    @Data
    public static class StyleConsistency {
        private Boolean isConsistent;
        private String previousStyle;
        private String detectedStyle;
        private String issue;
        private Double similarityScore; // 0-1
    }

    /**
     * 角色一致性检查
     */
    @Data
    public static class CharacterConsistencyCheck {
        private Long characterId;
        private String characterName;
        private Boolean isConsistent;
        private List<String> issues;
        private List<String> recommendations;
    }

    /**
     * 时间线检查
     */
    @Data
    public static class TimelineCheck {
        private Boolean isValid;
        private List<TimelineConflict> conflicts;
        private String timelineDescription;
    }

    /**
     * 时间冲突
     */
    @Data
    public static class TimelineConflict {
        private String event1;
        private String event2;
        private String conflict;
    }

    /**
     * 执行完整的情节一致性检查
     */
    @Transactional(readOnly = true)
    public PlotConsistencyCheckResult checkPlotConsistency(
            Long novelId,
            Long chapterId,
            String continuationText,
            String previousContent) {
        
        log.info("执行情节一致性检查: novelId={}, chapterId={}", novelId, chapterId);
        
        PlotConsistencyCheckResult result = new PlotConsistencyCheckResult();
        
        // 1. 检查情节冲突
        List<PlotConflict> conflicts = detectPlotConflicts(
            novelId, chapterId, continuationText, previousContent);
        result.setConflicts(conflicts);
        
        // 2. 检查逻辑问题
        List<LogicalIssue> logicalIssues = detectLogicalIssues(continuationText, previousContent);
        result.setLogicalIssues(logicalIssues);
        
        // 3. 检查风格一致性
        StyleConsistency styleCheck = checkStyleConsistency(novelId, chapterId, continuationText);
        result.setStyleConsistency(styleCheck);
        
        // 4. 检查角色行为一致性
        List<CharacterConsistencyCheck> characterChecks = checkCharacterConsistency(
            novelId, chapterId, continuationText, previousContent);
        result.setCharacterConsistency(characterChecks);
        
        // 5. 检查时间线
        TimelineCheck timelineCheck = checkTimeline(novelId, continuationText);
        result.setTimelineCheck(timelineCheck);
        
        // 6. 计算一致性得分
        Integer score = calculateConsistencyScore(result);
        result.setConsistencyScore(score);
        result.setIsConsistent(score >= 70);
        
        // 7. 生成建议
        result.setRecommendations(generateRecommendations(result));
        
        return result;
    }

    /**
     * 检测情节冲突
     */
    private List<PlotConflict> detectPlotConflicts(
            Long novelId,
            Long chapterId,
            String continuationText,
            String previousContent) {
        
        List<PlotConflict> conflicts = new ArrayList<>();
        
        // 1. 检查矛盾（相反的陈述）
        conflicts.addAll(detectContradictions(continuationText, previousContent));
        
        // 2. 检查重复
        conflicts.addAll(detectRepetitions(continuationText, previousContent));
        
        // 3. 检查与大纲的冲突
        conflicts.addAll(checkOutlineConsistency(novelId, chapterId, continuationText));
        
        return conflicts;
    }

    /**
     * 检测矛盾
     */
    private List<PlotConflict> detectContradictions(String newText, String previousText) {
        List<PlotConflict> contradictions = new ArrayList<>();
        
        // 简化的矛盾检测：查找"不是"、"没有"等否定词后面的陈述
        Pattern negationPattern = Pattern.compile("(不是|没有|从未|不能)(.*?)([。，！？])");
        
        var newNegations = extractNegations(newText);
        var prevAffirmations = extractAffirmations(previousText);
        
        // 检查新文本中的否定是否与之前的肯定相矛盾
        for (String negation : newNegations) {
            for (String affirmation : prevAffirmations) {
                if (isSameConcept(negation, affirmation)) {
                    PlotConflict conflict = new PlotConflict();
                    conflict.setType("CONTRADICTION");
                    conflict.setDescription("检测到矛盾: 新内容否定了之前的事实");
                    conflict.setSeverity(1);
                    conflict.setPreviousContent(affirmation);
                    conflict.setNewContent(negation);
                    contradictions.add(conflict);
                }
            }
        }
        
        return contradictions;
    }

    /**
     * 检测重复
     */
    private List<PlotConflict> detectRepetitions(String newText, String previousText) {
        List<PlotConflict> repetitions = new ArrayList<>();
        
        // 分句处理
        String[] newSentences = newText.split("[。！？]");
        String[] prevSentences = previousText.split("[。！？]");
        
        for (String newSent : newSentences) {
            for (String prevSent : prevSentences) {
                double similarity = calculateSimilarity(newSent, prevSent);
                if (similarity > 0.7) {
                    PlotConflict conflict = new PlotConflict();
                    conflict.setType("REPETITION");
                    conflict.setDescription("检测到重复内容");
                    conflict.setSeverity(3);
                    conflict.setPreviousContent(prevSent);
                    conflict.setNewContent(newSent);
                    repetitions.add(conflict);
                }
            }
        }
        
        return repetitions;
    }

    /**
     * 检查大纲一致性
     */
    private List<PlotConflict> checkOutlineConsistency(
            Long novelId,
            Long chapterId,
            String continuationText) {
        
        List<PlotConflict> conflicts = new ArrayList<>();
        
        List<Outline> outlines = outlineRepository.findByNovelIdOrderBySequenceNumberAsc(novelId);
        
        for (Outline outline : outlines) {
            // 如果大纲明确说不应该发生某事，但续写中发生了
            if (outline.getSummary() != null && outline.getSummary().contains("不能") 
                && continuationText.contains(extractAfterKeyword(outline.getSummary(), "不能"))) {
                
                PlotConflict conflict = new PlotConflict();
                conflict.setType("OUTLINE_CONFLICT");
                conflict.setDescription("续写与大纲冲突");
                conflict.setSeverity(1);
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }

    /**
     * 检测逻辑问题
     */
    private List<LogicalIssue> detectLogicalIssues(String newText, String previousText) {
        List<LogicalIssue> issues = new ArrayList<>();
        
        // 1. 检查因果关系
        issues.addAll(checkCausalityIssues(newText, previousText));
        
        // 2. 检查时间逻辑
        issues.addAll(checkTemporalLogic(newText, previousText));
        
        // 3. 检查角色能力
        issues.addAll(checkCharacterCapabilities(newText));
        
        return issues;
    }

    /**
     * 检查因果关系问题
     */
    private List<LogicalIssue> checkCausalityIssues(String newText, String previousText) {
        List<LogicalIssue> issues = new ArrayList<>();
        
        // 简化检查：如果结果出现而原因未提及
        if (newText.contains("突然") && !previousText.contains("预兆")) {
            LogicalIssue issue = new LogicalIssue();
            issue.setIssue("突然的事件缺乏前因");
            issue.setExplanation("建议补充必要的铺垫");
            issue.setSeverity(2);
            issues.add(issue);
        }
        
        return issues;
    }

    /**
     * 检查时间逻辑
     */
    private List<LogicalIssue> checkTemporalLogic(String newText, String previousText) {
        List<LogicalIssue> issues = new ArrayList<>();
        
        // 检查时间词的一致性
        List<String> newTimeMarkers = extractTimeMarkers(newText);
        List<String> prevTimeMarkers = extractTimeMarkers(previousText);
        
        // 简单检查：后来的时间不能早于之前的时间
        // 这需要更复杂的时间解析逻辑
        
        return issues;
    }

    /**
     * 检查角色能力
     */
    private List<LogicalIssue> checkCharacterCapabilities(String newText) {
        List<LogicalIssue> issues = new ArrayList<>();
        
        // 检查是否有角色执行了不符合其能力的行动
        // 例如：盲人角色"看到"了某物
        
        if (newText.contains("盲人") && (newText.contains("看到") || newText.contains("看见"))) {
            LogicalIssue issue = new LogicalIssue();
            issue.setIssue("角色行动与能力不符");
            issue.setExplanation("盲人角色不应该'看到'任何东西");
            issue.setSeverity(1);
            issues.add(issue);
        }
        
        return issues;
    }

    /**
     * 检查风格一致性
     */
    private StyleConsistency checkStyleConsistency(
            Long novelId,
            Long chapterId,
            String continuationText) {
        
        StyleConsistency consistency = new StyleConsistency();
        
        // 获取章节的小说
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        Novel novel = novelRepository.findById(chapter.getNovelId())
                .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        String expectedStyle = novel.getWritingStyle();
        
        // 分析新文本的风格
        String detectedStyle = analyzeTextStyle(continuationText);
        
        consistency.setPreviousStyle(expectedStyle);
        consistency.setDetectedStyle(detectedStyle);
        
        // 计算相似度
        Double similarity = calculateStyleSimilarity(expectedStyle, detectedStyle);
        consistency.setSimilarityScore(similarity);
        
        consistency.setIsConsistent(similarity >= 0.7);
        
        if (!consistency.getIsConsistent()) {
            consistency.setIssue("检测到风格不一致");
        }
        
        return consistency;
    }

    /**
     * 检查角色行为一致性
     */
    private List<CharacterConsistencyCheck> checkCharacterConsistency(
            Long novelId,
            Long chapterId,
            String continuationText,
            String previousContent) {
        
        List<CharacterConsistencyCheck> checks = new ArrayList<>();
        
        List<com.aiwriter.entity.Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
        
        for (com.aiwriter.entity.Character character : characters) {
            CharacterConsistencyCheck check = new CharacterConsistencyCheck();
            check.setCharacterId(character.getId());
            check.setCharacterName(character.getName());
            check.setIssues(new ArrayList<>());
            check.setRecommendations(new ArrayList<>());
            
            // 检查角色性格一致性
            if (continuationText.contains(character.getName())) {
                // 检查角色是否做出了与性格不符的事
                if (character.getPersonality().contains("胆小") && 
                    continuationText.contains(character.getName() + "勇敢")) {
                    check.getIssues().add("角色行动与性格设定不符");
                    check.setIsConsistent(false);
                }
            }
            
            checks.add(check);
        }
        
        return checks;
    }

    /**
     * 检查时间线
     */
    private TimelineCheck checkTimeline(Long novelId, String continuationText) {
        TimelineCheck check = new TimelineCheck();
        check.setConflicts(new ArrayList<>());
        check.setIsValid(true);
        
        // 提取文本中的时间标记
        List<String> timeMarkers = extractTimeMarkers(continuationText);
        check.setTimelineDescription(String.join(" -> ", timeMarkers));
        
        return check;
    }

    /**
     * 计算一致性得分
     */
    private Integer calculateConsistencyScore(PlotConsistencyCheckResult result) {
        int score = 100;
        
        // 每个严重冲突 -20 分
        for (PlotConflict conflict : result.getConflicts()) {
            if (conflict.getSeverity() == 1) {
                score -= 20;
            } else if (conflict.getSeverity() == 2) {
                score -= 10;
            } else {
                score -= 5;
            }
        }
        
        // 每个逻辑问题 -5 分
        score -= result.getLogicalIssues().size() * 5;
        
        // 风格不一致 -10 分
        if (result.getStyleConsistency() != null && !result.getStyleConsistency().getIsConsistent()) {
            score -= 10;
        }
        
        // 时间线错误 -15 分
        if (result.getTimelineCheck() != null && !result.getTimelineCheck().getIsValid()) {
            score -= 15;
        }
        
        return Math.max(0, score);
    }

    /**
     * 生成建议
     */
    private List<String> generateRecommendations(PlotConsistencyCheckResult result) {
        List<String> recommendations = new ArrayList<>();
        
        // 基于冲突生成建议
        for (PlotConflict conflict : result.getConflicts()) {
            if ("CONTRADICTION".equals(conflict.getType())) {
                recommendations.add("建议修改矛盾内容，确保逻辑一致");
            } else if ("REPETITION".equals(conflict.getType())) {
                recommendations.add("建议删除重复内容，避免冗余");
            }
        }
        
        // 基于逻辑问题生成建议
        if (!result.getLogicalIssues().isEmpty()) {
            recommendations.add("建议检查逻辑流程，补充必要的转折");
        }
        
        // 基于风格生成建议
        if (result.getStyleConsistency() != null && !result.getStyleConsistency().getIsConsistent()) {
            recommendations.add("建议调整写作风格，与整体作品保持一致");
        }
        
        return recommendations;
    }

    /**
     * 工具方法：提取否定陈述
     */
    private List<String> extractNegations(String text) {
        List<String> negations = new ArrayList<>();
        Pattern pattern = Pattern.compile("(不是|没有|从未)(.*?)([。，！？])");
        var matcher = pattern.matcher(text);
        while (matcher.find()) {
            negations.add(matcher.group(2));
        }
        return negations;
    }

    /**
     * 工具方法：提取肯定陈述
     */
    private List<String> extractAffirmations(String text) {
        List<String> affirmations = new ArrayList<>();
        // 简化实现
        String[] sentences = text.split("[。！？]");
        for (String sentence : sentences) {
            if (!sentence.contains("不") && !sentence.contains("没")) {
                affirmations.add(sentence.trim());
            }
        }
        return affirmations;
    }

    /**
     * 工具方法：判断两个概念是否相同
     */
    private Boolean isSameConcept(String concept1, String concept2) {
        return calculateSimilarity(concept1, concept2) > 0.6;
    }

    /**
     * 工具方法：计算文本相似度（简单的编辑距离）
     */
    private Double calculateSimilarity(String text1, String text2) {
        if (text1.isEmpty() && text2.isEmpty()) return 1.0;
        if (text1.isEmpty() || text2.isEmpty()) return 0.0;
        
        int len1 = text1.length();
        int len2 = text2.length();
        int maxLen = Math.max(len1, len2);
        
        int distance = levenshteinDistance(text1, text2);
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * 工具方法：计算编辑距离
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), 
                                   dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    /**
     * 工具方法：分析文本风格
     */
    private String analyzeTextStyle(String text) {
        if (text.contains("？") || text.contains("！")) {
            return "DYNAMIC";
        } else if (text.contains("然而") || text.contains("但是")) {
            return "ANALYTICAL";
        } else {
            return "NEUTRAL";
        }
    }

    /**
     * 工具方法：计算风格相似度
     */
    private Double calculateStyleSimilarity(String style1, String style2) {
        if (style1 == null || style2 == null) return 0.5;
        return style1.equals(style2) ? 1.0 : 0.5;
    }

    /**
     * 工具方法：提取时间标记
     */
    private List<String> extractTimeMarkers(String text) {
        List<String> markers = new ArrayList<>();
        String[] timeWords = {"早上", "中午", "下午", "晚上", "第二天", "后来", "突然", "立刻"};
        for (String word : timeWords) {
            if (text.contains(word)) {
                markers.add(word);
            }
        }
        return markers;
    }

    /**
     * 工具方法：提取关键字后的内容
     */
    private String extractAfterKeyword(String text, String keyword) {
        int index = text.indexOf(keyword);
        if (index != -1) {
            return text.substring(index + keyword.length());
        }
        return "";
    }
}
