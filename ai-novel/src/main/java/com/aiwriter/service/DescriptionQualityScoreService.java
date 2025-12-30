package com.aiwriter.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 描写质量评分服务
 * 核心功能：对对话、心理描写、环境描写、情绪描写等进行自然度和细节度的量化评分
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DescriptionQualityScoreService {

    /**
     * 完整的描写质量评分
     */
    @Data
    public static class DescriptionQualityScore {
        // 总体质量得分 (0-100)
        private Integer overallScore;
        
        // 自然度评分 (0-100)
        private Integer naturalness;
        
        // 细节度评分 (0-100)
        private Integer detailLevel;
        
        // 对话质量 (0-100)
        private DialogueQualityScore dialogueScore;
        
        // 心理描写质量 (0-100)
        private PsychologicalDescriptionScore psychologicalScore;
        
        // 环境描写质量 (0-100)
        private EnvironmentDescriptionScore environmentScore;
        
        // 情绪描写质量 (0-100)
        private EmotionalDescriptionScore emotionalScore;
        
        // 动作描写质量 (0-100)
        private ActionDescriptionScore actionScore;
        
        // 详细反馈
        private List<ScoreFeedback> feedbacks;
        
        // 改进建议
        private List<ImprovementSuggestion> suggestions;
        
        public DescriptionQualityScore() {
            this.feedbacks = new ArrayList<>();
            this.suggestions = new ArrayList<>();
        }
    }

    /**
     * 对话质量评分
     */
    @Data
    public static class DialogueQualityScore {
        // 整体对话分数
        private Integer score;
        
        // 对话自然度 (0-100)
        private Integer naturalness;
        
        // 角色区分度 (0-100) - 不同角色说话方式是否不同
        private Integer characterDistinction;
        
        // 信息传达效率 (0-100)
        private Integer informationDensity;
        
        // 问题列表
        private List<String> issues;
        
        public DialogueQualityScore() {
            this.issues = new ArrayList<>();
        }
    }

    /**
     * 心理描写评分
     */
    @Data
    public static class PsychologicalDescriptionScore {
        // 整体心理描写分数
        private Integer score;
        
        // 心理活动的细致程度
        private Integer refinement;
        
        // 心理变化的连贯性
        private Integer coherence;
        
        // 心理活动与行为的因果关系
        private Integer causality;
        
        // 问题列表
        private List<String> issues;
        
        public PsychologicalDescriptionScore() {
            this.issues = new ArrayList<>();
        }
    }

    /**
     * 环境描写评分
     */
    @Data
    public static class EnvironmentDescriptionScore {
        // 整体环境描写分数
        private Integer score;
        
        // 视觉细节的丰富度
        private Integer visualDetail;
        
        // 其他感官描写的丰富度
        private Integer sensoryDetail;
        
        // 环境与情绪的呼应度
        private Integer moodAlignment;
        
        // 问题列表
        private List<String> issues;
        
        public EnvironmentDescriptionScore() {
            this.issues = new ArrayList<>();
        }
    }

    /**
     * 情绪描写评分
     */
    @Data
    public static class EmotionalDescriptionScore {
        // 整体情绪描写分数
        private Integer score;
        
        // 情绪的真实性
        private Integer authenticity;
        
        // 情绪的渐进性（是否自然过渡）
        private Integer progression;
        
        // 情绪的强度表现
        private Integer intensity;
        
        // 问题列表
        private List<String> issues;
        
        public EmotionalDescriptionScore() {
            this.issues = new ArrayList<>();
        }
    }

    /**
     * 动作描写评分
     */
    @Data
    public static class ActionDescriptionScore {
        // 整体动作描写分数
        private Integer score;
        
        // 动作的生动性
        private Integer vividness;
        
        // 动作的逻辑性
        private Integer logic;
        
        // 动作的具体性
        private Integer specificity;
        
        // 问题列表
        private List<String> issues;
        
        public ActionDescriptionScore() {
            this.issues = new ArrayList<>();
        }
    }

    /**
     * 评分反馈
     */
    @Data
    public static class ScoreFeedback {
        private String category; // 对话/心理/环境/情绪/动作
        private String feedback;
        private Integer score;
    }

    /**
     * 改进建议
     */
    @Data
    public static class ImprovementSuggestion {
        private String category;
        private String suggestion;
        private String example;
        private Integer priority; // 1-高, 2-中, 3-低
    }

    /**
     * 评分文本内容
     */
    public DescriptionQualityScore scoreDescription(String text) {
        log.info("对文本进行描写质量评分");
        
        DescriptionQualityScore score = new DescriptionQualityScore();
        
        // 1. 评分对话质量
        score.setDialogueScore(scoreDialogueQuality(text));
        
        // 2. 评分心理描写
        score.setPsychologicalScore(scorePsychologicalDescription(text));
        
        // 3. 评分环境描写
        score.setEnvironmentScore(scoreEnvironmentDescription(text));
        
        // 4. 评分情绪描写
        score.setEmotionalScore(scoreEmotionalDescription(text));
        
        // 5. 评分动作描写
        score.setActionScore(scoreActionDescription(text));
        
        // 6. 计算自然度
        score.setNaturalness(calculateNaturalness(text));
        
        // 7. 计算细节度
        score.setDetailLevel(calculateDetailLevel(text));
        
        // 8. 计算综合评分
        Integer overall = calculateOverallScore(score);
        score.setOverallScore(overall);
        
        // 9. 生成反馈
        score.setFeedbacks(generateFeedbacks(score));
        
        // 10. 生成建议
        score.setSuggestions(generateSuggestions(score));
        
        return score;
    }

    /**
     * 评分对话质量
     */
    private DialogueQualityScore scoreDialogueQuality(String text) {
        DialogueQualityScore score = new DialogueQualityScore();
        
        // 提取所有对话
        List<String> dialogues = extractDialogues(text);
        
        if (dialogues.isEmpty()) {
            score.setScore(0);
            score.setNaturalness(0);
            score.setCharacterDistinction(0);
            score.setInformationDensity(0);
            score.getIssues().add("文本中没有发现对话");
            return score;
        }
        
        // 评估对话自然度
        score.setNaturalness(assessDialogueNaturalness(dialogues));
        
        // 评估角色区分
        score.setCharacterDistinction(assessCharacterDistinction(dialogues));
        
        // 评估信息密度
        score.setInformationDensity(assessInformationDensity(dialogues));
        
        // 检查常见问题
        checkDialogueIssues(dialogues, score);
        
        // 计算总分
        score.setScore((score.getNaturalness() + score.getCharacterDistinction() + score.getInformationDensity()) / 3);
        
        return score;
    }

    /**
     * 评分心理描写
     */
    private PsychologicalDescriptionScore scorePsychologicalDescription(String text) {
        PsychologicalDescriptionScore score = new PsychologicalDescriptionScore();
        
        // 检测心理描写的关键词
        String[] psychologicalKeywords = {"想到", "意识到", "感受到", "明白", "突然想起", 
                                         "心想", "暗自", "忽然", "却发现", "却意识到"};
        
        int psychologicalCount = 0;
        for (String keyword : psychologicalKeywords) {
            psychologicalCount += countOccurrences(text, keyword);
        }
        
        if (psychologicalCount == 0) {
            score.setScore(20);
            score.setRefinement(0);
            score.getIssues().add("缺少心理描写，建议增加人物内心活动");
            return score;
        }
        
        // 评估心理活动的细致程度
        score.setRefinement(assessPsychologicalRefinement(text));
        
        // 评估连贯性
        score.setCoherence(assessPsychologicalCoherence(text));
        
        // 评估因果关系
        score.setCausality(assessPsychologicalCausality(text));
        
        // 计算总分
        score.setScore((score.getRefinement() + score.getCoherence() + score.getCausality()) / 3);
        
        return score;
    }

    /**
     * 评分环境描写
     */
    private EnvironmentDescriptionScore scoreEnvironmentDescription(String text) {
        EnvironmentDescriptionScore score = new EnvironmentDescriptionScore();
        
        // 视觉细节关键词
        String[] visualKeywords = {"看", "望", "眼", "色彩", "光", "影", "形状", "大小"};
        int visualCount = 0;
        for (String keyword : visualKeywords) {
            visualCount += countOccurrences(text, keyword);
        }
        score.setVisualDetail(Math.min(100, visualCount * 5));
        
        // 其他感官关键词
        String[] sensoryKeywords = {"声音", "听", "音", "香", "味", "温", "冷", "触"};
        int sensoryCount = 0;
        for (String keyword : sensoryKeywords) {
            sensoryCount += countOccurrences(text, keyword);
        }
        score.setSensoryDetail(Math.min(100, sensoryCount * 8));
        
        // 环境与情绪的呼应
        score.setMoodAlignment(assessMoodEnvironmentAlignment(text));
        
        // 计算总分
        score.setScore((score.getVisualDetail() + score.getSensoryDetail() + score.getMoodAlignment()) / 3);
        
        return score;
    }

    /**
     * 评分情绪描写
     */
    private EmotionalDescriptionScore scoreEmotionalDescription(String text) {
        EmotionalDescriptionScore score = new EmotionalDescriptionScore();
        
        // 情绪关键词
        String[] emotionKeywords = {"开心", "悲伤", "愤怒", "恐惧", "惊讶", "厌恶", 
                                   "高兴", "难过", "生气", "害怕", "震惊"};
        
        int emotionCount = 0;
        for (String keyword : emotionKeywords) {
            emotionCount += countOccurrences(text, keyword);
        }
        
        // 真实性评分
        score.setAuthenticity(Math.min(100, 50 + emotionCount * 5));
        
        // 渐进性评分
        score.setProgression(assessEmotionalProgression(text));
        
        // 强度评分
        score.setIntensity(assessEmotionalIntensity(text));
        
        // 计算总分
        score.setScore((score.getAuthenticity() + score.getProgression() + score.getIntensity()) / 3);
        
        return score;
    }

    /**
     * 评分动作描写
     */
    private ActionDescriptionScore scoreActionDescription(String text) {
        ActionDescriptionScore score = new ActionDescriptionScore();
        
        // 动作动词关键词
        String[] actionKeywords = {"走", "跑", "跳", "转身", "抬起", "放下", "推", "拉", 
                                  "挥", "摔", "砸", "冲", "跨"};
        
        int actionCount = 0;
        for (String keyword : actionKeywords) {
            actionCount += countOccurrences(text, keyword);
        }
        
        // 生动性
        score.setVividness(Math.min(100, 30 + actionCount * 4));
        
        // 逻辑性
        score.setLogic(assessActionLogic(text));
        
        // 具体性
        score.setSpecificity(assessActionSpecificity(text));
        
        // 计算总分
        score.setScore((score.getVividness() + score.getLogic() + score.getSpecificity()) / 3);
        
        return score;
    }

    /**
     * 计算自然度
     */
    private Integer calculateNaturalness(String text) {
        // 自然度由多个因素组成
        int naturalness = 50;
        
        // 检查是否有冗余词汇
        naturalness -= countRedundantWords(text) * 2;
        
        // 检查是否有不自然的表达
        naturalness -= countUnnaturalPhrases(text) * 3;
        
        // 检查是否有生动的动词
        naturalness += countVividVerbs(text) * 2;
        
        return Math.max(0, Math.min(100, naturalness));
    }

    /**
     * 计算细节度
     */
    private Integer calculateDetailLevel(String text) {
        int detailLevel = 0;
        
        // 计算形容词的数量
        String[] adjectives = {"美丽", "丑陋", "温暖", "寒冷", "巨大", "微小", "柔软", "坚硬"};
        for (String adj : adjectives) {
            detailLevel += countOccurrences(text, adj) * 3;
        }
        
        // 计算数字和具体描述
        Pattern numberPattern = Pattern.compile("\\d+");
        detailLevel += (int) numberPattern.matcher(text).results().count() * 5;
        
        return Math.min(100, detailLevel);
    }

    /**
     * 计算综合评分
     */
    private Integer calculateOverallScore(DescriptionQualityScore score) {
        int total = 0;
        int count = 0;
        
        if (score.getDialogueScore() != null && score.getDialogueScore().getScore() > 0) {
            total += score.getDialogueScore().getScore();
            count++;
        }
        if (score.getPsychologicalScore() != null && score.getPsychologicalScore().getScore() > 0) {
            total += score.getPsychologicalScore().getScore();
            count++;
        }
        if (score.getEnvironmentScore() != null && score.getEnvironmentScore().getScore() > 0) {
            total += score.getEnvironmentScore().getScore();
            count++;
        }
        if (score.getEmotionalScore() != null && score.getEmotionalScore().getScore() > 0) {
            total += score.getEmotionalScore().getScore();
            count++;
        }
        if (score.getActionScore() != null && score.getActionScore().getScore() > 0) {
            total += score.getActionScore().getScore();
            count++;
        }
        
        if (count > 0) {
            total += score.getNaturalness() + score.getDetailLevel();
            return (total / (count + 2));
        }
        
        return 50;
    }

    /**
     * 生成反馈
     */
    private List<ScoreFeedback> generateFeedbacks(DescriptionQualityScore score) {
        List<ScoreFeedback> feedbacks = new ArrayList<>();
        
        if (score.getDialogueScore() != null) {
            feedbacks.add(createFeedback("对话", score.getDialogueScore().getScore()));
        }
        if (score.getPsychologicalScore() != null) {
            feedbacks.add(createFeedback("心理描写", score.getPsychologicalScore().getScore()));
        }
        if (score.getEnvironmentScore() != null) {
            feedbacks.add(createFeedback("环境描写", score.getEnvironmentScore().getScore()));
        }
        if (score.getEmotionalScore() != null) {
            feedbacks.add(createFeedback("情绪描写", score.getEmotionalScore().getScore()));
        }
        if (score.getActionScore() != null) {
            feedbacks.add(createFeedback("动作描写", score.getActionScore().getScore()));
        }
        
        return feedbacks;
    }

    /**
     * 生成建议
     */
    private List<ImprovementSuggestion> generateSuggestions(DescriptionQualityScore score) {
        List<ImprovementSuggestion> suggestions = new ArrayList<>();
        
        if (score.getDialogueScore() != null && score.getDialogueScore().getScore() < 60) {
            suggestions.add(createSuggestion("对话", 
                "对话质量待改进，建议增加角色特色和信息密度", 
                "「我很高兴」改为「我终于等到这一刻了」", 1));
        }
        
        if (score.getPsychologicalScore() != null && score.getPsychologicalScore().getScore() < 60) {
            suggestions.add(createSuggestion("心理", 
                "心理描写较少，建议增加角色内心活动", 
                "添加「他想起了母亲的话，心中涌起一股力量」", 2));
        }
        
        if (score.getEnvironmentScore() != null && score.getEnvironmentScore().getScore() < 60) {
            suggestions.add(createSuggestion("环境", 
                "环境描写缺乏细节，建议增加感官描写", 
                "添加「空气中飘来茶叶的香气」", 2));
        }
        
        return suggestions;
    }

    /**
     * 工具方法
     */
    private List<String> extractDialogues(String text) {
        Pattern pattern = Pattern.compile("[\"「]([^\"」]*?)[\"」]");
        var matcher = pattern.matcher(text);
        List<String> dialogues = new ArrayList<>();
        while (matcher.find()) {
            dialogues.add(matcher.group(1));
        }
        return dialogues;
    }

    private Integer assessDialogueNaturalness(List<String> dialogues) {
        int naturalness = 50;
        for (String dialogue : dialogues) {
            if (dialogue.length() > 50) naturalness += 5; // 长对话通常更自然
            if (dialogue.contains("呃") || dialogue.contains("嗯")) naturalness += 3;
        }
        return Math.min(100, naturalness);
    }

    private Integer assessCharacterDistinction(List<String> dialogues) {
        return Math.min(100, 50 + dialogues.size() * 5);
    }

    private Integer assessInformationDensity(List<String> dialogues) {
        int density = 50;
        for (String dialogue : dialogues) {
            if (dialogue.contains("、") || dialogue.contains("，")) density += 5;
        }
        return Math.min(100, density);
    }

    private void checkDialogueIssues(List<String> dialogues, DialogueQualityScore score) {
        // 检查重复对话
        Set<String> seen = new HashSet<>();
        for (String dialogue : dialogues) {
            if (seen.contains(dialogue)) {
                score.getIssues().add("检测到重复对话");
                break;
            }
            seen.add(dialogue);
        }
    }

    private Integer assessPsychologicalRefinement(String text) {
        return Math.min(100, 40 + countOccurrences(text, "想") * 3);
    }

    private Integer assessPsychologicalCoherence(String text) {
        return 65; // 简化实现
    }

    private Integer assessPsychologicalCausality(String text) {
        int causality = 50;
        if (text.contains("因为") && text.contains("所以")) causality += 20;
        if (text.contains("于是") || text.contains("因此")) causality += 10;
        return Math.min(100, causality);
    }

    private Integer assessMoodEnvironmentAlignment(String text) {
        return 60; // 简化实现
    }

    private Integer assessEmotionalProgression(String text) {
        return Math.min(100, 50 + countOccurrences(text, "渐渐") * 5);
    }

    private Integer assessEmotionalIntensity(String text) {
        int intensity = 50;
        intensity += countOccurrences(text, "！") * 10;
        intensity += countOccurrences(text, "？") * 5;
        return Math.min(100, intensity);
    }

    private Integer assessActionLogic(String text) {
        return 70; // 简化实现
    }

    private Integer assessActionSpecificity(String text) {
        return Math.min(100, 40 + countOccurrences(text, "地") * 4);
    }

    private Integer countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }

    private Integer countRedundantWords(String text) {
        int count = 0;
        String[] redundantWords = {"非常非常", "很很", "极其极其"};
        for (String word : redundantWords) {
            count += countOccurrences(text, word);
        }
        return count;
    }

    private Integer countUnnaturalPhrases(String text) {
        return 0; // 简化实现
    }

    private Integer countVividVerbs(String text) {
        String[] vividVerbs = {"扑", "冲", "窜", "飙", "疾"};
        int count = 0;
        for (String verb : vividVerbs) {
            count += countOccurrences(text, verb);
        }
        return count;
    }

    private ScoreFeedback createFeedback(String category, Integer score) {
        ScoreFeedback feedback = new ScoreFeedback();
        feedback.setCategory(category);
        feedback.setScore(score);
        if (score >= 80) {
            feedback.setFeedback("优秀！" + category + "质量很高");
        } else if (score >= 60) {
            feedback.setFeedback("良好。" + category + "质量不错，还有改进空间");
        } else {
            feedback.setFeedback("待改进。" + category + "质量需要加强");
        }
        return feedback;
    }

    private ImprovementSuggestion createSuggestion(String category, String suggestion, String example, Integer priority) {
        ImprovementSuggestion s = new ImprovementSuggestion();
        s.setCategory(category);
        s.setSuggestion(suggestion);
        s.setExample(example);
        s.setPriority(priority);
        return s;
    }
}
