package com.aiwriter.service;

import com.aiwriter.dto.SceneRhythmAnalysis;
import com.aiwriter.dto.RhythmRecommendation;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.Scene;
import com.aiwriter.entity.SceneUsage;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.SceneRepository;
import com.aiwriter.repository.SceneUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 场景节奏分析服务
 * 
 * 功能:
 * 1. 分析场景出现频率和间隔
 * 2. 检测节奏模式（加速/减速/稳定）
 * 3. 提供节奏优化建议
 * 4. 分析场景与章节节奏的关联
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneRhythmAnalysisService {
    
    private final SceneRepository sceneRepository;
    private final SceneUsageRepository sceneUsageRepository;
    private final ChapterRepository chapterRepository;
    private final SceneService sceneService;
    
    /**
     * 分析单个场景的节奏
     */
    @Transactional(readOnly = true)
    public SceneRhythmAnalysis analyzeSceneRhythm(Long sceneId) {
        try {
            Scene scene = sceneService.getSceneById(sceneId);
            List<SceneUsage> usages = sceneUsageRepository.findBySceneIdOrderByUsageTimeDesc(sceneId);
            
            if (usages.isEmpty()) {
                return buildEmptyRhythmAnalysis(scene);
            }
            
            // 计算出现间隔
            List<Integer> intervals = calculateIntervals(usages);
            
            // 分析章节字数模式
            List<Integer> chapterWordCounts = new ArrayList<>();
            for (SceneUsage usage : usages) {
                Chapter chapter = chapterRepository.findById(usage.getChapterId()).orElse(null);
                if (chapter != null) {
                    chapterWordCounts.add(chapter.getWordCount());
                }
            }
            
            // 检测节奏模式
            String rhythmPattern = detectRhythmPattern(intervals, chapterWordCounts);
            
            // 计算节奏评分
            double rhythmScore = calculateRhythmScore(intervals, chapterWordCounts);
            
            SceneRhythmAnalysis analysis = new SceneRhythmAnalysis();
            analysis.setSceneId(sceneId);
            analysis.setSceneName(scene.getName());
            analysis.setSceneType(scene.getSceneType());
            analysis.setTotalAppearances((long) usages.size());
            analysis.setAverageInterval(intervals.isEmpty() ? 0 : 
                (int) Math.round((double) intervals.stream().mapToInt(Integer::intValue).sum() / intervals.size()));
            analysis.setMinInterval(intervals.isEmpty() ? 0 : intervals.stream().mapToInt(Integer::intValue).min().orElse(0));
            analysis.setMaxInterval(intervals.isEmpty() ? 0 : intervals.stream().mapToInt(Integer::intValue).max().orElse(0));
            analysis.setRhythmPattern(rhythmPattern);
            analysis.setRhythmScore(Math.round(rhythmScore * 100.0) / 100.0);
            analysis.setAverageChapterLength(chapterWordCounts.isEmpty() ? 0 : 
                (int) Math.round((double) chapterWordCounts.stream().mapToInt(Integer::intValue).sum() / chapterWordCounts.size()));
            analysis.setFirstAppearanceChapter(chapterRepository.findById(usages.get(0).getChapterId())
                .map(Chapter::getChapterNumber).orElse(0));
            analysis.setLastAppearanceChapter(chapterRepository.findById(usages.get(usages.size() - 1).getChapterId())
                .map(Chapter::getChapterNumber).orElse(0));
            analysis.setLastUsageTime(usages.get(usages.size() - 1).getUsageTime());
            analysis.setDaysSinceLastUsage(calculateDaysSinceLastUsage(usages.get(usages.size() - 1).getUsageTime()));
            analysis.setIntervalDistribution(calculateIntervalDistribution(intervals));
            
            log.info("场景节奏分析完成: sceneId={}, pattern={}, score={}", sceneId, rhythmPattern, analysis.getRhythmScore());
            
            return analysis;
        } catch (Exception e) {
            log.error("分析场景节奏失败: sceneId={}", sceneId, e);
            throw new RuntimeException("分析场景节奏失败", e);
        }
    }
    
    /**
     * 获取小说整体节奏分析
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeNovelRhythm(Long novelId) {
        try {
            List<Scene> scenes = sceneRepository.findByNovelIdOrderByImportanceScoreDesc(novelId);
            List<SceneRhythmAnalysis> sceneAnalyses = new ArrayList<>();
            
            for (Scene scene : scenes) {
                List<SceneUsage> usages = sceneUsageRepository.findBySceneIdOrderByUsageTimeAsc(scene.getId());
                if (!usages.isEmpty()) {
                    sceneAnalyses.add(analyzeSceneRhythm(scene.getId()));
                }
            }
            
            Map<String, Object> novelRhythm = new HashMap<>();
            novelRhythm.put("novelId", novelId);
            novelRhythm.put("totalScenes", sceneAnalyses.size());
            
            // 计算平均节奏评分
            double avgRhythmScore = sceneAnalyses.stream()
                .mapToDouble(SceneRhythmAnalysis::getRhythmScore)
                .average()
                .orElse(0.0);
            novelRhythm.put("averageRhythmScore", Math.round(avgRhythmScore * 100.0) / 100.0);
            
            // 统计节奏模式分布
            Map<String, Long> patternDistribution = sceneAnalyses.stream()
                .collect(Collectors.groupingBy(SceneRhythmAnalysis::getRhythmPattern, Collectors.counting()));
            novelRhythm.put("rhythmPatternDistribution", patternDistribution);
            
            // 统计高频场景（出现超过3次）
            List<SceneRhythmAnalysis> frequentScenes = sceneAnalyses.stream()
                .filter(a -> a.getTotalAppearances() > 3)
                .sorted(Comparator.comparingLong(SceneRhythmAnalysis::getTotalAppearances).reversed())
                .collect(Collectors.toList());
            novelRhythm.put("frequentScenes", frequentScenes);
            
            // 统计低频场景（出现1-2次）
            List<SceneRhythmAnalysis> rareScenes = sceneAnalyses.stream()
                .filter(a -> a.getTotalAppearances() <= 2)
                .collect(Collectors.toList());
            novelRhythm.put("rareScenes", rareScenes);
            
            // 整体节奏评价
            String overallAssessment = assessOverallRhythm(sceneAnalyses, patternDistribution);
            novelRhythm.put("overallAssessment", overallAssessment);
            novelRhythm.put("sceneAnalyses", sceneAnalyses);
            
            log.info("小说节奏分析完成: novelId={}, avgScore={}, patterns={}", 
                novelId, avgRhythmScore, patternDistribution);
            
            return novelRhythm;
        } catch (Exception e) {
            log.error("分析小说节奏失败: novelId={}", novelId, e);
            throw new RuntimeException("分析小说节奏失败", e);
        }
    }
    
    /**
     * 获取节奏优化建议
     */
    @Transactional(readOnly = true)
    public List<RhythmRecommendation> getRhythmRecommendations(Long novelId) {
        try {
            Map<String, Object> novelRhythm = analyzeNovelRhythm(novelId);
            List<RhythmRecommendation> recommendations = new ArrayList<>();
            
            @SuppressWarnings("unchecked")
            List<SceneRhythmAnalysis> analyses = (List<SceneRhythmAnalysis>) novelRhythm.get("sceneAnalyses");
            
            for (SceneRhythmAnalysis analysis : analyses) {
                RhythmRecommendation rec = new RhythmRecommendation();
                rec.setSceneId(analysis.getSceneId());
                rec.setSceneName(analysis.getSceneName());
                rec.setCurrentPattern(analysis.getRhythmPattern());
                rec.setCurrentScore(analysis.getRhythmScore());
                
                // 根据节奏模式生成建议
                if ("ACCELERATING".equals(analysis.getRhythmPattern())) {
                    rec.setIssue("出现频率加快");
                    rec.setRecommendation("场景出现间隔在缩短，可能导致节奏紧张。建议考虑在某些场景中增加停顿或转场。");
                    rec.setPriority("MEDIUM");
                } else if ("DECELERATING".equals(analysis.getRhythmPattern())) {
                    rec.setIssue("出现频率减缓");
                    rec.setRecommendation("场景出现间隔在增长，可能导致节奏松散。建议加强场景的复现或引入新的变化。");
                    rec.setPriority("MEDIUM");
                } else if ("STABLE".equals(analysis.getRhythmPattern())) {
                    rec.setIssue("出现频率稳定");
                    rec.setRecommendation("场景出现节奏保持一致，是较好的节奏控制。可继续保持。");
                    rec.setPriority("LOW");
                } else if ("SPARSE".equals(analysis.getRhythmPattern())) {
                    rec.setIssue("出现频率稀疏");
                    rec.setRecommendation("场景在小说中出现次数较少，可能降低读者的沉浸感。建议考虑增加场景的复现或引入新的变化。");
                    rec.setPriority("HIGH");
                }
                
                // 根据出现次数生成额外建议
                if (analysis.getTotalAppearances() > 5) {
                    rec.setNote("该场景是重要的核心场景，出现次数达到" + analysis.getTotalAppearances() + "次");
                } else if (analysis.getTotalAppearances() == 1) {
                    rec.setNote("该场景只出现过一次，是独特的场景设定");
                }
                
                recommendations.add(rec);
            }
            
            return recommendations.stream()
                .sorted(Comparator.comparing(RhythmRecommendation::getPriority)
                    .thenComparingLong((RhythmRecommendation r) -> r.getSceneId()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取节奏优化建议失败: novelId={}", novelId, e);
            throw new RuntimeException("获取节奏优化建议失败", e);
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private List<Integer> calculateIntervals(List<SceneUsage> usages) {
        List<Integer> intervals = new ArrayList<>();
        // 计算连续使用之间的时间间隔（天数）
        for (int i = 1; i < usages.size(); i++) {
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
                usages.get(i - 1).getUsageTime().toLocalDate(),
                usages.get(i).getUsageTime().toLocalDate()
            );
            if (daysDiff > 0) intervals.add((int) daysDiff);
        }
        return intervals;
    }
    
    private String detectRhythmPattern(List<Integer> intervals, List<Integer> wordCounts) {
        if (intervals.isEmpty()) return "SINGLE";
        
        if (intervals.size() < 2) return "SPARSE";
        
        // 分析间隔趋势
        int increasingIntervals = 0, decreasingIntervals = 0;
        for (int i = 1; i < intervals.size(); i++) {
            if (intervals.get(i) > intervals.get(i - 1)) increasingIntervals++;
            else if (intervals.get(i) < intervals.get(i - 1)) decreasingIntervals++;
        }
        
        // 计算标准差
        double avgInterval = intervals.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = intervals.stream()
            .mapToDouble(i -> Math.pow(i - avgInterval, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        // 评估稳定性
        if (stdDev < avgInterval * 0.3) {
            return "STABLE";
        } else if (decreasingIntervals > increasingIntervals) {
            return "ACCELERATING";
        } else if (increasingIntervals > decreasingIntervals) {
            return "DECELERATING";
        } else {
            return "VARIABLE";
        }
    }
    
    private double calculateRhythmScore(List<Integer> intervals, List<Integer> wordCounts) {
        if (intervals.isEmpty()) return 0.5;
        
        // 基础分：间隔稳定性
        double avgInterval = intervals.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = intervals.stream()
            .mapToDouble(i -> Math.pow(i - avgInterval, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        double stabilityScore = Math.max(0, 1 - (stdDev / (avgInterval + 1)));
        
        // 字数一致性分
        double wordCountScore = 0.5;
        if (!wordCounts.isEmpty()) {
            double avgWordCount = wordCounts.stream().mapToInt(Integer::intValue).average().orElse(0);
            double wordVariance = wordCounts.stream()
                .mapToDouble(w -> Math.pow(w - avgWordCount, 2))
                .average().orElse(0);
            double wordStdDev = Math.sqrt(wordVariance);
            wordCountScore = Math.max(0, 1 - (wordStdDev / (avgWordCount + 1)));
        }
        
        return (stabilityScore * 0.6 + wordCountScore * 0.4);
    }
    
    private Integer calculateDaysSinceLastUsage(LocalDateTime lastUsageTime) {
        return (int) ChronoUnit.DAYS.between(lastUsageTime, LocalDateTime.now());
    }
    
    private Map<String, Integer> calculateIntervalDistribution(List<Integer> intervals) {
        Map<String, Integer> dist = new HashMap<>();
        dist.put("1-2", 0);
        dist.put("3-5", 0);
        dist.put("6-10", 0);
        dist.put("11-20", 0);
        dist.put("20+", 0);
        
        for (Integer interval : intervals) {
            if (interval <= 2) dist.put("1-2", dist.get("1-2") + 1);
            else if (interval <= 5) dist.put("3-5", dist.get("3-5") + 1);
            else if (interval <= 10) dist.put("6-10", dist.get("6-10") + 1);
            else if (interval <= 20) dist.put("11-20", dist.get("11-20") + 1);
            else dist.put("20+", dist.get("20+") + 1);
        }
        
        return dist;
    }
    
    private SceneRhythmAnalysis buildEmptyRhythmAnalysis(Scene scene) {
        SceneRhythmAnalysis analysis = new SceneRhythmAnalysis();
        analysis.setSceneId(scene.getId());
        analysis.setSceneName(scene.getName());
        analysis.setSceneType(scene.getSceneType());
        analysis.setTotalAppearances(0L);
        analysis.setAverageInterval(0);
        analysis.setRhythmPattern("NO_DATA");
        analysis.setRhythmScore(0.0);
        return analysis;
    }
    
    private String assessOverallRhythm(List<SceneRhythmAnalysis> analyses, Map<String, Long> patternDistribution) {
        if (analyses.isEmpty()) return "无数据";
        
        double avgScore = analyses.stream()
            .mapToDouble(SceneRhythmAnalysis::getRhythmScore)
            .average()
            .orElse(0.0);
        
        long stableCount = patternDistribution.getOrDefault("STABLE", 0L);
        long totalScenes = analyses.size();
        
        if (avgScore > 0.8) {
            return "节奏控制优秀，场景出现频率稳定，建议继续保持当前节奏。";
        } else if (avgScore > 0.6) {
            return "节奏控制良好，大多数场景节奏适中。可根据单个场景的建议进行微调。";
        } else if (avgScore > 0.4) {
            return "节奏控制一般，场景出现频率波动较大。建议加强节奏管理，增加核心场景的复现或均衡出现。";
        } else {
            return "节奏控制需改进，场景出现非常不均匀。强烈建议重新规划场景的出现频率和间隔。";
        }
    }
}
