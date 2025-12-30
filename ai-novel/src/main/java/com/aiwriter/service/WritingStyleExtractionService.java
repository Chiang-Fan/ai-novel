package com.aiwriter.service;

import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.WritingStyle;
import com.aiwriter.repository.WritingStyleRepository;
import com.aiwriter.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 文风自动提取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WritingStyleExtractionService {
    
    private final WritingStyleRepository writingStyleRepository;
    private final AiService aiService;
    
    /**
     * 异步提取文风
     */
    @Async("autoExtractionExecutor")
    @Transactional
    public void extractAndSyncWritingStyle(Chapter chapter, double similarityThreshold) {
        try {
            String content = chapter.getContent();
            if (content == null || content.length() < 100) {
                log.info("章节内容过短，跳过文风提取");
                return;
            }
            
            // 提取文本片段（前500字）
            String sample = content.substring(0, Math.min(500, content.length()));
            
            // AI分析文风特征
            String features = analyzeStyleFeatures(sample);
            
            // 检查相似度
            List<WritingStyle> existingStyles = writingStyleRepository.findAll();
            WritingStyle mostSimilar = null;
            double maxSimilarity = 0.0;
            
            for (WritingStyle style : existingStyles) {
                double similarity = calculateSimilarity(features, style.getStyleFeatures());
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilar = style;
                }
            }
            
            if (maxSimilarity >= similarityThreshold && mostSimilar != null) {
                // 更新已有文风
                mostSimilar.setUsageCount(mostSimilar.getUsageCount() + 1);
                writingStyleRepository.save(mostSimilar);
                log.info("更新文风: id={}, name={}, 相似度={}", 
                        mostSimilar.getId(), mostSimilar.getName(), maxSimilarity);
            } else {
                // 创建新文风
                WritingStyle newStyle = createNewStyle(chapter, sample, features);
                log.info("创建新文风: id={}, name={}", newStyle.getId(), newStyle.getName());
            }
            
        } catch (Exception e) {
            log.error("文风提取失败: chapterId={}", chapter.getId(), e);
        }
    }
    
    /**
     * AI分析文风特征
     */
    private String analyzeStyleFeatures(String sample) {
        try {
            String systemPrompt = "你是一位专业的文学评论家，擅长分析写作风格。";
            String userPrompt = String.format(
                "请分析以下文本的写作风格特征，用简短的标签描述（例如：句长:15,语气:平和,复杂度:中等,修辞:排比）。\n\n文本内容：\n%s",
                sample
            );
            
            String result = aiService.chat(systemPrompt, userPrompt).trim();
            
            if (result.isEmpty() || result.length() > 200) {
                log.info("AI文风分析返回异常，使用简化分析");
                return fallbackAnalyzeStyleFeatures(sample);
            }
            
            return result;
        } catch (Exception e) {
            log.warn("AI文风分析失败，回退到简化分析: {}", e.getMessage());
            return fallbackAnalyzeStyleFeatures(sample);
        }
    }
    
    private String fallbackAnalyzeStyleFeatures(String sample) {
        // 简化实现：分析句长、语气、复杂度
        int avgSentenceLength = calculateAvgSentenceLength(sample);
        String tone = detectTone(sample);
        String complexity = detectComplexity(sample);
        
        return String.format("句长:%d,语气:%s,复杂度:%s", avgSentenceLength, tone, complexity);
    }
    
    private int calculateAvgSentenceLength(String text) {
        String[] sentences = text.split("[。！？\\n]");
        int totalLength = 0;
        for (String s : sentences) {
            totalLength += s.length();
        }
        return sentences.length > 0 ? totalLength / sentences.length : 0;
    }
    
    private String detectTone(String text) {
        if (text.contains("！") || text.contains("激动")) return "激昂";
        if (text.contains("。") && !text.contains("！")) return "平和";
        if (text.contains("？")) return "疑问";
        return "中性";
    }
    
    private String detectComplexity(String text) {
        int avgLength = calculateAvgSentenceLength(text);
        if (avgLength > 50) return "复杂";
        if (avgLength > 25) return "中等";
        return "简单";
    }
    
    /**
     * 创建新文风
     */
    private WritingStyle createNewStyle(Chapter chapter, String sample, String features) {
        WritingStyle style = WritingStyle.builder()
                .name("章节" + chapter.getChapterNumber() + "风格")
                .description("自动提取的文风特征")
                .sampleText(sample)
                .styleFeatures(features)
                .category("自动提取")
                .languageComplexity(detectComplexity(sample))
                .sentenceLength(String.valueOf(calculateAvgSentenceLength(sample)))
                .tone(detectTone(sample))
                .usageCount(1)
                .rating(new BigDecimal("3.50"))
                .isSystem(false)
                .build();
        
        return writingStyleRepository.save(style);
    }
    
    /**
     * 计算相似度（简单实现）
     */
    private double calculateSimilarity(String features1, String features2) {
        if (features1 == null || features2 == null) return 0.0;
        
        String[] f1 = features1.split(",");
        String[] f2 = features2.split(",");
        
        int matches = 0;
        for (String item1 : f1) {
            for (String item2 : f2) {
                if (item1.trim().equals(item2.trim())) {
                    matches++;
                }
            }
        }
        
        return (double) matches / Math.max(f1.length, f2.length);
    }
}
