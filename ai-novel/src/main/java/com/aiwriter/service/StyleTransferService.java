package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
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
 * 风格迁移服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StyleTransferService {
    
    private final ChapterRepository chapterRepository;
    private final WritingStyleRepository styleRepository;
    private final StyleConversionRepository conversionRepository;
    private final StyleAnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 分析文本风格
     */
    @Transactional
    public StyleAnalysisResponse analyzeStyle(Long chapterId, String text) {
        log.info("开始分析文本风格，章节ID: {}", chapterId);
        
        // 验证章节
        chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        // 提取文本特征
        StyleAnalysisResponse.StyleFeatures features = extractStyleFeatures(text);
        
        // 匹配最相似的风格
        WritingStyle detectedStyle = findMostSimilarStyle(features);
        
        // 计算置信度
        int confidenceScore = calculateConfidence(features, detectedStyle);
        
        // 构建分析结果
        Map<String, Object> result = new HashMap<>();
        result.put("features", features);
        result.put("detectedStyleId", detectedStyle != null ? detectedStyle.getId() : null);
        result.put("confidenceScore", confidenceScore);
        
        // 保存分析结果
        StyleAnalysis analysis = StyleAnalysis.builder()
                .chapterId(chapterId)
                .textSample(text.length() > 500 ? text.substring(0, 500) : text)
                .detectedStyleId(detectedStyle != null ? detectedStyle.getId() : null)
                .analysisResult(serializeObject(result))
                .confidenceScore(confidenceScore)
                .build();
        
        analysis = analysisRepository.save(analysis);
        
        return buildAnalysisResponse(analysis, detectedStyle, features);
    }
    
    /**
     * 转换文本风格
     */
    @Transactional
    public StyleConversionResponse convertStyle(StyleConversionRequest request) {
        log.info("开始转换风格，目标风格ID: {}", request.getTargetStyleId());
        
        // 验证章节和目标风格
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        WritingStyle targetStyle = styleRepository.findById(request.getTargetStyleId())
                .orElseThrow(() -> new RuntimeException("目标风格不存在"));
        
        // 分析源风格（如果需要）
        Long sourceStyleId = null;
        if (Boolean.TRUE.equals(request.getAnalyzeSource())) {
            StyleAnalysisResponse sourceAnalysis = analyzeStyle(request.getChapterId(), request.getText());
            if (sourceAnalysis.getDetectedStyle() != null) {
                sourceStyleId = sourceAnalysis.getDetectedStyle().getId();
            }
        }
        
        // 构建转换提示词
        String prompt = buildConversionPrompt(request.getText(), targetStyle);
        
        // 调用AI进行转换
        String convertedText = convertWithAI(prompt, request.getText(), targetStyle);
        
        // 计算风格匹配度
        int matchScore = calculateStyleMatch(convertedText, targetStyle);
        
        // 生成转换说明
        String notes = generateConversionNotes(request.getText(), convertedText, targetStyle);
        
        // 获取版本号
        long count = conversionRepository.countByChapterId(request.getChapterId());
        int version = (int) (count + 1);
        
        // 保存转换记录
        StyleConversion conversion = StyleConversion.builder()
                .chapterId(request.getChapterId())
                .sourceText(request.getText())
                .convertedText(convertedText)
                .sourceStyleId(sourceStyleId)
                .targetStyleId(request.getTargetStyleId())
                .conversionNotes(notes)
                .styleMatchScore(matchScore)
                .promptUsed(prompt)
                .aiModel("DeepSeek")
                .version(version)
                .isApplied(false)
                .build();
        
        conversion = conversionRepository.save(conversion);
        
        // 更新风格使用次数
        targetStyle.setUsageCount(targetStyle.getUsageCount() + 1);
        styleRepository.save(targetStyle);
        
        return buildConversionResponse(conversion, 
                sourceStyleId != null ? styleRepository.findById(sourceStyleId).orElse(null) : null,
                targetStyle);
    }
    
    /**
     * 获取所有风格
     */
    public List<WritingStyleResponse> getAllStyles(String category) {
        List<WritingStyle> styles;
        
        if (category != null && !category.isEmpty()) {
            styles = styleRepository.findByCategoryOrderByUsageCountDesc(category);
        } else {
            styles = styleRepository.findAllByOrderByUsageCountDesc();
        }
        
        return styles.stream()
                .map(this::toStyleResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取转换历史
     */
    public List<StyleConversionResponse> getConversionHistory(Long chapterId) {
        List<StyleConversion> conversions = conversionRepository
                .findByChapterIdOrderByCreatedAtDesc(chapterId);
        
        return conversions.stream()
                .map(this::toConversionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 应用转换
     */
    @Transactional
    public void applyConversion(Long conversionId) {
        StyleConversion conversion = conversionRepository.findById(conversionId)
                .orElseThrow(() -> new RuntimeException("转换记录不存在"));
        
        // 更新章节内容
        Chapter chapter = chapterRepository.findById(conversion.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        String currentContent = chapter.getContent();
        String updatedContent = currentContent.replace(
                conversion.getSourceText(),
                conversion.getConvertedText());
        
        chapter.setContent(updatedContent);
        chapterRepository.save(chapter);
        
        // 标记为已应用
        conversion.setIsApplied(true);
        conversion.setAppliedAt(LocalDateTime.now());
        conversionRepository.save(conversion);
        
        log.info("已应用风格转换: {}", conversionId);
    }
    
    /**
     * 评分
     */
    @Transactional
    public void rateConversion(Long conversionId, Integer rating) {
        StyleConversion conversion = conversionRepository.findById(conversionId)
                .orElseThrow(() -> new RuntimeException("转换记录不存在"));
        
        conversion.setRating(rating);
        conversionRepository.save(conversion);
    }
    
    /**
     * 提取风格特征
     */
    private StyleAnalysisResponse.StyleFeatures extractStyleFeatures(String text) {
        // 计算平均句长
        String[] sentences = text.split("[。！？]");
        int avgSentenceLength = sentences.length > 0 
                ? text.length() / sentences.length : 0;
        
        // 计算平均段长
        String[] paragraphs = text.split("\n\n");
        int avgParagraphLength = paragraphs.length > 0 
                ? text.length() / paragraphs.length : 0;
        
        // 判断语言复杂度
        String complexity = avgSentenceLength < 20 ? "SIMPLE" 
                : avgSentenceLength < 40 ? "MEDIUM" : "COMPLEX";
        
        // 判断句子长度风格
        String sentenceLength = avgSentenceLength < 25 ? "SHORT" 
                : avgSentenceLength < 50 ? "MEDIUM" : "LONG";
        
        // 简单词频分析
        Map<String, Integer> wordFreq = new HashMap<>();
        // TODO: 实现真实的词频统计
        
        return StyleAnalysisResponse.StyleFeatures.builder()
                .languageComplexity(complexity)
                .sentenceLength(sentenceLength)
                .tone("NEUTRAL")
                .avgSentenceLength(avgSentenceLength)
                .avgParagraphLength(avgParagraphLength)
                .wordFrequency(wordFreq)
                .description("分析的文本风格特征")
                .build();
    }
    
    /**
     * 查找最相似的风格
     */
    private WritingStyle findMostSimilarStyle(StyleAnalysisResponse.StyleFeatures features) {
        List<WritingStyle> styles = styleRepository.findAll();
        
        if (styles.isEmpty()) {
            return null;
        }
        
        // TODO: 实现真实的相似度计算
        // 这里返回使用最多的风格作为演示
        return styles.stream()
                .max(Comparator.comparing(WritingStyle::getUsageCount))
                .orElse(null);
    }
    
    /**
     * 计算置信度
     */
    private int calculateConfidence(StyleAnalysisResponse.StyleFeatures features, WritingStyle style) {
        // TODO: 实现真实的置信度计算
        return 75; // 模拟值
    }
    
    /**
     * 构建转换提示词
     */
    private String buildConversionPrompt(String text, WritingStyle targetStyle) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请将以下文本转换为").append(targetStyle.getName()).append("的风格：\n\n");
        prompt.append("原文：\n").append(text).append("\n\n");
        prompt.append("目标风格特点：\n");
        prompt.append("- 作者：").append(targetStyle.getAuthor()).append("\n");
        prompt.append("- 语言复杂度：").append(targetStyle.getLanguageComplexity()).append("\n");
        prompt.append("- 句子长度：").append(targetStyle.getSentenceLength()).append("\n");
        prompt.append("- 语调：").append(targetStyle.getTone()).append("\n");
        prompt.append("\n示例文本：\n").append(targetStyle.getSampleText()).append("\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 保持原文的核心内容和情节\n");
        prompt.append("2. 采用目标风格的语言特点\n");
        prompt.append("3. 模仿示例文本的表达方式\n");
        
        return prompt.toString();
    }
    
    /**
     * 使用AI转换风格
     */
    private String convertWithAI(String prompt, String text, WritingStyle targetStyle) {
        // TODO: 集成实际的AI服务
        log.info("AI风格转换请求: {}", prompt.substring(0, Math.min(100, prompt.length())));
        
        // 模拟转换结果
        return text + "\n\n[已转换为" + targetStyle.getName() + "风格]";
    }
    
    /**
     * 计算风格匹配度
     */
    private int calculateStyleMatch(String text, WritingStyle style) {
        // TODO: 实现真实的匹配度计算
        return 85; // 模拟值
    }
    
    /**
     * 生成转换说明
     */
    private String generateConversionNotes(String source, String converted, WritingStyle style) {
        return "已将文本转换为" + style.getName() + "风格，主要调整了语言表达和叙述方式。";
    }
    
    /**
     * 序列化对象
     */
    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            return "{}";
        }
    }
    
    /**
     * 反序列化对象
     */
    private <T> T deserializeObject(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("反序列化失败", e);
            return null;
        }
    }
    
    /**
     * 构建分析响应
     */
    private StyleAnalysisResponse buildAnalysisResponse(
            StyleAnalysis analysis, WritingStyle detectedStyle, 
            StyleAnalysisResponse.StyleFeatures features) {
        
        StyleAnalysisResponse.DetectedStyle detected = null;
        if (detectedStyle != null) {
            detected = StyleAnalysisResponse.DetectedStyle.builder()
                    .id(detectedStyle.getId())
                    .name(detectedStyle.getName())
                    .author(detectedStyle.getAuthor())
                    .category(detectedStyle.getCategory())
                    .matchScore(analysis.getConfidenceScore())
                    .build();
        }
        
        return StyleAnalysisResponse.builder()
                .id(analysis.getId())
                .chapterId(analysis.getChapterId())
                .textSample(analysis.getTextSample())
                .detectedStyle(detected)
                .features(features)
                .confidenceScore(analysis.getConfidenceScore())
                .createdAt(analysis.getCreatedAt())
                .build();
    }
    
    /**
     * 构建转换响应
     */
    private StyleConversionResponse buildConversionResponse(
            StyleConversion conversion, WritingStyle sourceStyle, WritingStyle targetStyle) {
        
        StyleConversionResponse.StyleInfo source = null;
        if (sourceStyle != null) {
            source = StyleConversionResponse.StyleInfo.builder()
                    .id(sourceStyle.getId())
                    .name(sourceStyle.getName())
                    .author(sourceStyle.getAuthor())
                    .category(sourceStyle.getCategory())
                    .tone(sourceStyle.getTone())
                    .build();
        }
        
        StyleConversionResponse.StyleInfo target = StyleConversionResponse.StyleInfo.builder()
                .id(targetStyle.getId())
                .name(targetStyle.getName())
                .author(targetStyle.getAuthor())
                .category(targetStyle.getCategory())
                .tone(targetStyle.getTone())
                .build();
        
        return StyleConversionResponse.builder()
                .id(conversion.getId())
                .chapterId(conversion.getChapterId())
                .sourceText(conversion.getSourceText())
                .convertedText(conversion.getConvertedText())
                .sourceStyle(source)
                .targetStyle(target)
                .conversionNotes(conversion.getConversionNotes())
                .styleMatchScore(conversion.getStyleMatchScore())
                .version(conversion.getVersion())
                .rating(conversion.getRating())
                .isApplied(conversion.getIsApplied())
                .createdAt(conversion.getCreatedAt())
                .build();
    }
    
    /**
     * 转换为转换响应
     */
    private StyleConversionResponse toConversionResponse(StyleConversion conversion) {
        WritingStyle sourceStyle = conversion.getSourceStyleId() != null 
                ? styleRepository.findById(conversion.getSourceStyleId()).orElse(null) : null;
        WritingStyle targetStyle = styleRepository.findById(conversion.getTargetStyleId())
                .orElseThrow(() -> new RuntimeException("目标风格不存在"));
        
        return buildConversionResponse(conversion, sourceStyle, targetStyle);
    }
    
    /**
     * 转换为风格响应
     */
    private WritingStyleResponse toStyleResponse(WritingStyle style) {
        return WritingStyleResponse.builder()
                .id(style.getId())
                .name(style.getName())
                .author(style.getAuthor())
                .description(style.getDescription())
                .sampleText(style.getSampleText())
                .category(style.getCategory())
                .languageComplexity(style.getLanguageComplexity())
                .sentenceLength(style.getSentenceLength())
                .tone(style.getTone())
                .usageCount(style.getUsageCount())
                .rating(style.getRating())
                .isSystem(style.getIsSystem())
                .createdAt(style.getCreatedAt())
                .build();
    }
}
