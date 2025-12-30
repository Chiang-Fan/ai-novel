package com.aiwriter.service;

import com.aiwriter.dto.PaceFeedback;
import com.aiwriter.dto.WordCountValidation;
import com.aiwriter.entity.Chapter;
import com.aiwriter.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 节奏控制反馈服务
 * 
 * 功能:
 * 1. 实时验证章节字数是否符合目标
 * 2. 提供节奏优化反馈
 * 3. 生成改进建议
 * 4. 跟踪节奏控制的有效性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaceControlFeedbackService {
    
    private final ChapterRepository chapterRepository;
    private final SceneWordCountManagementService wordCountService;
    private final SceneRhythmAnalysisService rhythmService;
    private final ContentAnalysisService contentAnalysisService;
    
    private static final int MIN_CHAPTER_LENGTH = 500;
    private static final int MAX_CHAPTER_LENGTH = 5000;
    private static final int IDEAL_CHAPTER_LENGTH = 2000;
    
    /**
     * 获取章节的实时反馈
     */
    @Transactional(readOnly = true)
    public PaceFeedback getChapterFeedback(Long chapterId, Long novelId) {
        try {
            Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("章节不存在"));
            
            PaceFeedback feedback = new PaceFeedback();
            feedback.setChapterId(chapterId);
            feedback.setChapterNumber(chapter.getChapterNumber());
            feedback.setChapterTitle(chapter.getTitle());
            feedback.setActualWordCount(chapter.getWordCount());
            feedback.setGeneratedAt(new Date());
            
            // 1. 字数反馈
            WordCountValidation wordCountValidation = validateChapterWordCount(chapter);
            feedback.setWordCountValidation(wordCountValidation);
            
            // 2. 节奏反馈
            feedback.setPaceStatus(analyzePaceStatus(chapter, novelId));
            
            // 3. 内容质量反馈
            feedback.setContentQuality(analyzeContentQuality(chapterId));
            
            // 4. 改进建议
            feedback.setImprovementSuggestions(generateImprovementSuggestions(chapter, wordCountValidation));
            
            // 5. 风险警告
            feedback.setWarnings(generateWarnings(chapter, wordCountValidation));
            
            log.info("获取章节反馈: chapterId={}, status={}", chapterId, feedback.getPaceStatus());
            
            return feedback;
        } catch (Exception e) {
            log.error("获取章节反馈失败: chapterId={}", chapterId, e);
            throw new RuntimeException("获取章节反馈失败", e);
        }
    }
    
    /**
     * 获取小说整体反馈
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getNovelPaceFeedback(Long novelId) {
        try {
            List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
            
            if (chapters.isEmpty()) {
                throw new RuntimeException("小说没有章节");
            }
            
            // 计算整体统计
            int totalWords = chapters.stream().mapToInt(Chapter::getWordCount).sum();
            double averageWordCount = (double) totalWords / chapters.size();
            int totalChapters = chapters.size();
            
            // 分析字数分布
            Map<String, Integer> wordDistribution = analyzeWordDistribution(chapters);
            
            // 分析节奏一致性
            String rhythmConsistency = analyzeRhythmConsistency(chapters);
            
            // 计算整体评分
            double overallScore = calculateOverallScore(chapters);
            
            // 检测异常章节
            List<Map<String, Object>> anomalies = detectAnomalousChapters(chapters);
            
            Map<String, Object> novelFeedback = new HashMap<>();
            novelFeedback.put("novelId", novelId);
            novelFeedback.put("totalChapters", totalChapters);
            novelFeedback.put("totalWords", totalWords);
            novelFeedback.put("averageWordCount", Math.round(averageWordCount));
            novelFeedback.put("wordDistribution", wordDistribution);
            novelFeedback.put("rhythmConsistency", rhythmConsistency);
            novelFeedback.put("overallScore", Math.round(overallScore * 100.0) / 100.0);
            novelFeedback.put("anomalies", anomalies);
            
            // 生成整体评价
            String assessment = generateNovelAssessment(overallScore, rhythmConsistency, anomalies.size());
            novelFeedback.put("assessment", assessment);
            
            log.info("获取小说反馈: novelId={}, avgWords={}, score={}", novelId, averageWordCount, overallScore);
            
            return novelFeedback;
        } catch (Exception e) {
            log.error("获取小说反馈失败: novelId={}", novelId, e);
            throw new RuntimeException("获取小说反馈失败", e);
        }
    }
    
    /**
     * 生成节奏控制报告
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generatePaceControlReport(Long novelId) {
        try {
            Map<String, Object> report = new HashMap<>();
            report.put("novelId", novelId);
            report.put("generatedAt", new Date());
            
            // 字数控制分析
            Map<String, Object> wordControlAnalysis = wordCountService.getNovelWordCountSummary(novelId);
            report.put("wordControlAnalysis", wordControlAnalysis);
            
            // 字数优化建议
            List<Map<String, Object>> wordOptimizations = wordCountService.getWordCountOptimizationSuggestions(novelId);
            report.put("wordOptimizations", wordOptimizations);
            
            // 节奏分析
            Map<String, Object> rhythmAnalysis = rhythmService.analyzeNovelRhythm(novelId);
            report.put("rhythmAnalysis", rhythmAnalysis);
            
            // 节奏优化建议
            List<Map<String, Object>> rhythmOptimizations = rhythmService.getRhythmRecommendations(novelId).stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("sceneId", r.getSceneId());
                    m.put("sceneName", r.getSceneName());
                    m.put("issue", r.getIssue());
                    m.put("recommendation", r.getRecommendation());
                    m.put("priority", r.getPriority());
                    return m;
                })
                .collect(Collectors.toList());
            report.put("rhythmOptimizations", rhythmOptimizations);
            
            // 整体反馈
            Map<String, Object> novelFeedback = getNovelPaceFeedback(novelId);
            report.put("novelFeedback", novelFeedback);
            
            log.info("生成节奏控制报告: novelId={}", novelId);
            
            return report;
        } catch (Exception e) {
            log.error("生成节奏控制报告失败: novelId={}", novelId, e);
            throw new RuntimeException("生成节奏控制报告失败", e);
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private WordCountValidation validateChapterWordCount(Chapter chapter) {
        WordCountValidation validation = new WordCountValidation();
        validation.setChapterId(chapter.getId());
        validation.setActualWordCount(chapter.getWordCount());
        validation.setMinTarget(MIN_CHAPTER_LENGTH);
        validation.setMaxTarget(MAX_CHAPTER_LENGTH);
        validation.setIdealTarget(IDEAL_CHAPTER_LENGTH);
        
        if (chapter.getWordCount() < MIN_CHAPTER_LENGTH) {
            validation.setStatus("TOO_SHORT");
            validation.setDeviation(MIN_CHAPTER_LENGTH - chapter.getWordCount());
            validation.setMessage("章节过短，建议补充至少" + validation.getDeviation() + "字");
        } else if (chapter.getWordCount() > MAX_CHAPTER_LENGTH) {
            validation.setStatus("TOO_LONG");
            validation.setDeviation(chapter.getWordCount() - MAX_CHAPTER_LENGTH);
            validation.setMessage("章节过长，建议删减至少" + validation.getDeviation() + "字");
        } else {
            validation.setStatus("ACCEPTABLE");
            validation.setDeviation(0);
            validation.setMessage("字数符合目标范围");
        }
        
        return validation;
    }
    
    private String analyzePaceStatus(Chapter chapter, Long novelId) {
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        
        if (chapters.size() < 3) {
            return "DATA_INSUFFICIENT";
        }
        
        // 计算最近3章的平均字数
        int recentAvg = chapters.stream()
            .skip(Math.max(0, chapters.size() - 3))
            .mapToInt(Chapter::getWordCount)
            .sum() / 3;
        
        int currentWords = chapter.getWordCount();
        
        if (Math.abs(currentWords - recentAvg) > recentAvg * 0.5) {
            return "PACE_UNSTABLE";
        } else if (currentWords < IDEAL_CHAPTER_LENGTH) {
            return "PACE_SLOW";
        } else if (currentWords > IDEAL_CHAPTER_LENGTH) {
            return "PACE_FAST";
        } else {
            return "PACE_IDEAL";
        }
    }
    
    private Map<String, Object> analyzeContentQuality(Long chapterId) {
        try {
            // TODO: 调用内容分析服务获取质量评分
            Map<String, Object> quality = new HashMap<>();
            quality.put("overallScore", 0.8);
            quality.put("readability", "HIGH");
            quality.put("coherence", "GOOD");
            return quality;
        } catch (Exception e) {
            log.warn("分析内容质量失败: chapterId={}", chapterId);
            return new HashMap<>();
        }
    }
    
    private List<String> generateImprovementSuggestions(Chapter chapter, WordCountValidation validation) {
        List<String> suggestions = new ArrayList<>();
        
        if ("TOO_SHORT".equals(validation.getStatus())) {
            suggestions.add("补充环境细节描写");
            suggestions.add("增加角色心理活动");
            suggestions.add("加入更多对话交互");
            suggestions.add("扩展情感描写");
        } else if ("TOO_LONG".equals(validation.getStatus())) {
            suggestions.add("精简环境描写");
            suggestions.add("删除冗余对话");
            suggestions.add("简化心理独白");
            suggestions.add("移除次要情节");
        }
        
        return suggestions;
    }
    
    private List<String> generateWarnings(Chapter chapter, WordCountValidation validation) {
        List<String> warnings = new ArrayList<>();
        
        if ("TOO_SHORT".equals(validation.getStatus()) && validation.getDeviation() > 1000) {
            warnings.add("⚠️ 章节严重过短，强烈建议补充内容");
        } else if ("TOO_LONG".equals(validation.getStatus()) && validation.getDeviation() > 1500) {
            warnings.add("⚠️ 章节严重过长，可能影响阅读体验");
        }
        
        return warnings;
    }
    
    private Map<String, Integer> analyzeWordDistribution(List<Chapter> chapters) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("very_short", 0);
        distribution.put("short", 0);
        distribution.put("ideal", 0);
        distribution.put("long", 0);
        distribution.put("very_long", 0);
        
        for (Chapter chapter : chapters) {
            int wordCount = chapter.getWordCount();
            if (wordCount < 800) {
                distribution.put("very_short", distribution.get("very_short") + 1);
            } else if (wordCount < 1500) {
                distribution.put("short", distribution.get("short") + 1);
            } else if (wordCount <= 2500) {
                distribution.put("ideal", distribution.get("ideal") + 1);
            } else if (wordCount <= 3500) {
                distribution.put("long", distribution.get("long") + 1);
            } else {
                distribution.put("very_long", distribution.get("very_long") + 1);
            }
        }
        
        return distribution;
    }
    
    private String analyzeRhythmConsistency(List<Chapter> chapters) {
        if (chapters.size() < 3) return "DATA_INSUFFICIENT";
        
        double[] wordCounts = chapters.stream()
            .mapToDouble(c -> (double) c.getWordCount())
            .toArray();
        
        double mean = Arrays.stream(wordCounts).average().orElse(0);
        double variance = Arrays.stream(wordCounts)
            .map(x -> Math.pow(x - mean, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        double cv = mean > 0 ? stdDev / mean : 0;
        
        if (cv < 0.2) return "VERY_CONSISTENT";
        if (cv < 0.4) return "CONSISTENT";
        if (cv < 0.6) return "MODERATE";
        if (cv < 0.8) return "VARIABLE";
        return "VERY_VARIABLE";
    }
    
    private double calculateOverallScore(List<Chapter> chapters) {
        if (chapters.isEmpty()) return 0.0;
        
        double wordCountScore = 0.0;
        int validChapters = 0;
        
        for (Chapter chapter : chapters) {
            if (chapter.getWordCount() >= MIN_CHAPTER_LENGTH && chapter.getWordCount() <= MAX_CHAPTER_LENGTH) {
                wordCountScore += 1.0;
            } else if (chapter.getWordCount() >= MIN_CHAPTER_LENGTH * 0.7 && 
                      chapter.getWordCount() <= MAX_CHAPTER_LENGTH * 1.2) {
                wordCountScore += 0.7;
            } else {
                wordCountScore += 0.4;
            }
            validChapters++;
        }
        
        return validChapters > 0 ? wordCountScore / validChapters : 0.0;
    }
    
    private List<Map<String, Object>> detectAnomalousChapters(List<Chapter> chapters) {
        List<Map<String, Object>> anomalies = new ArrayList<>();
        
        double avgWords = chapters.stream().mapToInt(Chapter::getWordCount).average().orElse(0);
        
        for (Chapter chapter : chapters) {
            double deviation = Math.abs(chapter.getWordCount() - avgWords) / avgWords;
            if (deviation > 0.5) {
                Map<String, Object> anomaly = new HashMap<>();
                anomaly.put("chapterId", chapter.getId());
                anomaly.put("chapterNumber", chapter.getChapterNumber());
                anomaly.put("wordCount", chapter.getWordCount());
                anomaly.put("deviation", Math.round(deviation * 100));
                anomaly.put("severity", deviation > 0.8 ? "SEVERE" : "MODERATE");
                anomalies.add(anomaly);
            }
        }
        
        return anomalies;
    }
    
    private String generateNovelAssessment(double score, String consistency, int anomalyCount) {
        if (score > 0.8 && "VERY_CONSISTENT".equals(consistency)) {
            return "📈 优秀! 小说节奏控制非常稳定，字数分布均衡。继续保持当前的写作风格。";
        } else if (score > 0.7 && anomalyCount <= 2) {
            return "✅ 良好! 小说节奏控制良好，只有少数章节需要微调。";
        } else if (score > 0.6) {
            return "⚠️ 一般。小说节奏控制需改进，建议根据建议进行字数调整。";
        } else {
            return "❌ 需要改进。小说节奏控制不稳定，强烈建议重新审视每章的字数目标。";
        }
    }
}
