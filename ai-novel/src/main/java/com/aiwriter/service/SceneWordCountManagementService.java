package com.aiwriter.service;

import com.aiwriter.dto.SceneWordCountAnalysis;
import com.aiwriter.dto.SceneWordCountTarget;
import com.aiwriter.entity.SceneUsage;
import com.aiwriter.repository.SceneUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 场景字数控制管理服务
 * 
 * 功能:
 * 1. 追踪场景在各章节的字数贡献
 * 2. 分析场景平均字数和变化趋势
 * 3. 管理场景字数目标
 * 4. 提供字数优化建议
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneWordCountManagementService {
    
    private final SceneUsageRepository sceneUsageRepository;
    private final ChapterService chapterService;
    private final SceneService sceneService;
    
    /**
     * 获取场景字数分析
     */
    @Transactional(readOnly = true)
    public SceneWordCountAnalysis getSceneWordCountAnalysis(Long sceneId) {
        try {
            // 获取该场景的所有使用记录
            List<SceneUsage> usages = sceneUsageRepository.findBySceneIdOrderByUsageTimeDesc(sceneId);
            
            if (usages.isEmpty()) {
                return buildEmptyAnalysis(sceneId);
            }
            
            // 统计字数数据
            long totalWordCount = 0;
            int minWordCount = Integer.MAX_VALUE;
            int maxWordCount = 0;
            List<Integer> wordCounts = new ArrayList<>();
            
            for (SceneUsage usage : usages) {
                // 从chapter中获取字数
                Integer wordCount = getChapterWordCount(usage.getChapterId());
                if (wordCount != null && wordCount > 0) {
                    totalWordCount += wordCount;
                    wordCounts.add(wordCount);
                    minWordCount = Math.min(minWordCount, wordCount);
                    maxWordCount = Math.max(maxWordCount, wordCount);
                }
            }
            
            if (wordCounts.isEmpty()) {
                return buildEmptyAnalysis(sceneId);
            }
            
            SceneWordCountAnalysis analysis = new SceneWordCountAnalysis();
            analysis.setSceneId(sceneId);
            analysis.setSceneName(sceneService.getScene(sceneId).getName());
            analysis.setTotalWordCount(totalWordCount);
            analysis.setAverageWordCount(Math.round((double) totalWordCount / wordCounts.size()));
            analysis.setUsageCount((long) usages.size());
            analysis.setMinWordCount(minWordCount == Integer.MAX_VALUE ? 0 : minWordCount);
            analysis.setMaxWordCount(maxWordCount);
            analysis.setWordCountDistribution(calculateDistribution(wordCounts));
            analysis.setTrend(analyzeTrend(wordCounts));
            
            log.info("场景字数分析完成: sceneId={}, totalWords={}, avgWords={}", 
                sceneId, totalWordCount, analysis.getAverageWordCount());
            
            return analysis;
        } catch (Exception e) {
            log.error("获取场景字数分析失败: sceneId={}", sceneId, e);
            throw new RuntimeException("获取场景字数分析失败", e);
        }
    }
    
    /**
     * 获取小说所有场景的字数分析
     */
    @Transactional(readOnly = true)
    public List<SceneWordCountAnalysis> getNovelSceneWordAnalysis(Long novelId) {
        try {
            List<Long> sceneIds = sceneService.getScenesByNovel(novelId).stream()
                .map(scene -> scene.getId())
                .collect(Collectors.toList());
            
            return sceneIds.stream()
                .map(this::getSceneWordCountAnalysis)
                .filter(analysis -> analysis.getUsageCount() > 0)
                .sorted(Comparator.comparingLong(SceneWordCountAnalysis::getTotalWordCount).reversed())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取小说场景字数分析失败: novelId={}", novelId, e);
            throw new RuntimeException("获取小说场景字数分析失败", e);
        }
    }
    
    /**
     * 设置场景字数目标
     */
    @Transactional
    public SceneWordCountTarget setSceneWordCountTarget(Long sceneId, Integer minWords, Integer maxWords) {
        try {
            if (minWords == null || maxWords == null || minWords < 0 || maxWords < minWords) {
                throw new IllegalArgumentException("目标字数范围无效: min=" + minWords + ", max=" + maxWords);
            }
            
            SceneWordCountTarget target = new SceneWordCountTarget();
            target.setSceneId(sceneId);
            target.setSceneName(sceneService.getScene(sceneId).getName());
            target.setMinWords(minWords);
            target.setMaxWords(maxWords);
            target.setTargetAverageWords((minWords + maxWords) / 2);
            
            log.info("设置场景字数目标: sceneId={}, min={}, max={}", sceneId, minWords, maxWords);
            
            return target;
        } catch (Exception e) {
            log.error("设置场景字数目标失败: sceneId={}", sceneId, e);
            throw new RuntimeException("设置场景字数目标失败", e);
        }
    }
    
    /**
     * 验证章节字数是否符合场景目标
     */
    @Transactional(readOnly = true)
    public Map<String, Object> validateChapterWordCount(Long sceneId, Long chapterId, Integer targetMinWords, Integer targetMaxWords) {
        try {
            Integer actualWordCount = getChapterWordCount(chapterId);
            Map<String, Object> result = new HashMap<>();
            result.put("sceneId", sceneId);
            result.put("chapterId", chapterId);
            result.put("actualWordCount", actualWordCount);
            result.put("targetMinWords", targetMinWords);
            result.put("targetMaxWords", targetMaxWords);
            
            if (actualWordCount == null) {
                result.put("isValid", false);
                result.put("status", "ERROR");
                result.put("message", "无法获取章节字数");
                return result;
            }
            
            boolean isValid = actualWordCount >= targetMinWords && actualWordCount <= targetMaxWords;
            result.put("isValid", isValid);
            
            if (isValid) {
                result.put("status", "OK");
                result.put("message", "字数符合目标范围");
                result.put("deviation", 0);
            } else if (actualWordCount < targetMinWords) {
                result.put("status", "BELOW");
                result.put("message", "字数低于目标范围");
                result.put("deviation", targetMinWords - actualWordCount);
                result.put("suggestion", "需要补充约" + (targetMinWords - actualWordCount) + "字");
            } else {
                result.put("status", "ABOVE");
                result.put("message", "字数超出目标范围");
                result.put("deviation", actualWordCount - targetMaxWords);
                result.put("suggestion", "需要删减约" + (actualWordCount - targetMaxWords) + "字");
            }
            
            log.info("验证章节字数: sceneId={}, chapterId={}, status={}", sceneId, chapterId, result.get("status"));
            
            return result;
        } catch (Exception e) {
            log.error("验证章节字数失败: sceneId={}, chapterId={}", sceneId, chapterId, e);
            throw new RuntimeException("验证章节字数失败", e);
        }
    }
    
    /**
     * 获取小说字数统计汇总
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getNovelWordCountSummary(Long novelId) {
        try {
            List<SceneWordCountAnalysis> analyses = getNovelSceneWordAnalysis(novelId);
            
            long totalWords = analyses.stream()
                .mapToLong(SceneWordCountAnalysis::getTotalWordCount)
                .sum();
            
            double averageWords = analyses.isEmpty() ? 0 : 
                (double) totalWords / analyses.size();
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("novelId", novelId);
            summary.put("totalScenes", analyses.size());
            summary.put("totalWords", totalWords);
            summary.put("averageWordsPerScene", Math.round(averageWords));
            summary.put("sceneAnalyses", analyses);
            
            // 计算字数分布
            Map<String, Long> distribution = new HashMap<>();
            distribution.put("low", analyses.stream().filter(a -> a.getAverageWordCount() < 500).count());
            distribution.put("medium", analyses.stream().filter(a -> a.getAverageWordCount() >= 500 && a.getAverageWordCount() < 1000).count());
            distribution.put("high", analyses.stream().filter(a -> a.getAverageWordCount() >= 1000).count());
            summary.put("wordCountDistribution", distribution);
            
            log.info("获取小说字数统计: novelId={}, totalWords={}, avgWords={}", 
                novelId, totalWords, Math.round(averageWords));
            
            return summary;
        } catch (Exception e) {
            log.error("获取小说字数统计失败: novelId={}", novelId, e);
            throw new RuntimeException("获取小说字数统计失败", e);
        }
    }
    
    /**
     * 获取字数优化建议
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWordCountOptimizationSuggestions(Long novelId) {
        try {
            List<SceneWordCountAnalysis> analyses = getNovelSceneWordAnalysis(novelId);
            List<Map<String, Object>> suggestions = new ArrayList<>();
            
            for (SceneWordCountAnalysis analysis : analyses) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("sceneId", analysis.getSceneId());
                suggestion.put("sceneName", analysis.getSceneName());
                suggestion.put("currentAverage", analysis.getAverageWordCount());
                suggestion.put("usageCount", analysis.getUsageCount());
                
                // 根据趋势生成建议
                if ("INCREASING".equals(analysis.getTrend())) {
                    suggestion.put("trend", "字数递增");
                    suggestion.put("recommendation", "场景在不断扩展，注意避免过度铺垫");
                } else if ("DECREASING".equals(analysis.getTrend())) {
                    suggestion.put("trend", "字数递减");
                    suggestion.put("recommendation", "场景描写在简化，考虑保持细节的丰富度");
                } else {
                    suggestion.put("trend", "字数稳定");
                    suggestion.put("recommendation", "场景呈现保持一致，节奏稳定");
                }
                
                // 根据字数绝对值生成建议
                if (analysis.getAverageWordCount() < 300) {
                    suggestion.put("priority", "HIGH");
                    suggestion.put("action", "该场景描写过短，建议补充环境、心理或对话细节");
                } else if (analysis.getAverageWordCount() > 2000) {
                    suggestion.put("priority", "MEDIUM");
                    suggestion.put("action", "该场景描写较长，考虑适当简化以保持节奏");
                } else {
                    suggestion.put("priority", "LOW");
                    suggestion.put("action", "该场景字数适中，无需调整");
                }
                
                suggestions.add(suggestion);
            }
            
            return suggestions.stream()
                .sorted(Comparator.comparing((Map<String, Object> m) -> (String) m.get("priority")))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取字数优化建议失败: novelId={}", novelId, e);
            throw new RuntimeException("获取字数优化建议失败", e);
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private Integer getChapterWordCount(Long chapterId) {
        try {
            return chapterService.getChapter(chapterId).getWordCount();
        } catch (Exception e) {
            log.warn("获取章节字数失败: chapterId={}", chapterId);
            return null;
        }
    }
    
    private SceneWordCountAnalysis buildEmptyAnalysis(Long sceneId) {
        SceneWordCountAnalysis analysis = new SceneWordCountAnalysis();
        analysis.setSceneId(sceneId);
        analysis.setSceneName(sceneService.getScene(sceneId).getName());
        analysis.setTotalWordCount(0L);
        analysis.setAverageWordCount(0L);
        analysis.setUsageCount(0L);
        analysis.setMinWordCount(0);
        analysis.setMaxWordCount(0);
        return analysis;
    }
    
    private Map<String, Integer> calculateDistribution(List<Integer> wordCounts) {
        Map<String, Integer> dist = new HashMap<>();
        dist.put("0-300", 0);
        dist.put("300-500", 0);
        dist.put("500-1000", 0);
        dist.put("1000-2000", 0);
        dist.put("2000+", 0);
        
        for (Integer wordCount : wordCounts) {
            if (wordCount <= 300) dist.put("0-300", dist.get("0-300") + 1);
            else if (wordCount <= 500) dist.put("300-500", dist.get("300-500") + 1);
            else if (wordCount <= 1000) dist.put("500-1000", dist.get("500-1000") + 1);
            else if (wordCount <= 2000) dist.put("1000-2000", dist.get("1000-2000") + 1);
            else dist.put("2000+", dist.get("2000+") + 1);
        }
        
        return dist;
    }
    
    private String analyzeTrend(List<Integer> wordCounts) {
        if (wordCounts.size() < 2) return "STABLE";
        
        int upCount = 0, downCount = 0;
        for (int i = 1; i < wordCounts.size(); i++) {
            if (wordCounts.get(i) > wordCounts.get(i - 1)) {
                upCount++;
            } else if (wordCounts.get(i) < wordCounts.get(i - 1)) {
                downCount++;
            }
        }
        
        if (upCount > downCount) return "INCREASING";
        if (downCount > upCount) return "DECREASING";
        return "STABLE";
    }
}
