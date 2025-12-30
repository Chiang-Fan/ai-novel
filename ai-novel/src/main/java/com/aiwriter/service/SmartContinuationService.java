package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.AIContinuation;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.Outline;
import com.aiwriter.entity.WritingSuggestion;
import com.aiwriter.repository.AIContinuationRepository;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.OutlineRepository;
import com.aiwriter.repository.WritingSuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能续写服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartContinuationService {
    
    private final ChapterRepository chapterRepository;
    private final AIContinuationRepository continuationRepository;
    private final WritingSuggestionRepository suggestionRepository;
    private final OutlineRepository outlineRepository;
    
    /**
     * 智能续写
     */
    @Transactional
    public ContinuationResponse generateContinuation(ContinuationRequest request) {
        log.info("生成续写: chapterId={}, style={}, length={}", 
                request.getChapterId(), request.getStyle(), request.getLength());
        
        // 获取章节信息
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        // 构建提示词
        String prompt = buildContinuationPrompt(chapter, request);
        
        // 调用AI生成
        String continuationText = generateWithAI(prompt);
        
        // 获取当前版本号
        List<AIContinuation> existing = continuationRepository.findByChapterIdAndStyleOrderByVersionDesc(
                request.getChapterId(), request.getStyle());
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;
        
        // 保存续写结果
        AIContinuation continuation = AIContinuation.builder()
                .chapterId(request.getChapterId())
                .sourceText(request.getSourceText())
                .continuationText(continuationText)
                .style(request.getStyle())
                .length(request.getLength())
                .promptUsed(prompt)
                .aiModel("deepseek-chat")
                .version(nextVersion)
                .isApplied(false)
                .build();
        continuation = continuationRepository.save(continuation);
        
        // 大纲一致性检查（如果需要）
        ContinuationResponse.OutlineCheck outlineCheck = null;
        if (Boolean.TRUE.equals(request.getCheckOutline())) {
            outlineCheck = checkOutlineConsistency(chapter, continuationText);
        }
        
        return toResponse(continuation, outlineCheck);
    }
    
    /**
     * 应用续写
     */
    @Transactional
    public void applyContinuation(Long continuationId) {
        log.info("应用续写: {}", continuationId);
        
        AIContinuation continuation = continuationRepository.findById(continuationId)
                .orElseThrow(() -> new RuntimeException("续写不存在"));
        
        // 取消同章节其他续写的应用状态
        continuationRepository.findByChapterIdAndIsAppliedTrue(continuation.getChapterId())
                .ifPresent(existing -> {
                    existing.setIsApplied(false);
                    existing.setAppliedAt(null);
                    continuationRepository.save(existing);
                });
        
        // 应用当前续写
        continuation.setIsApplied(true);
        continuation.setAppliedAt(LocalDateTime.now());
        continuationRepository.save(continuation);
        
        // 更新章节内容
        Chapter chapter = chapterRepository.findById(continuation.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        String newContent = chapter.getContent() + "\n\n" + continuation.getContinuationText();
        chapter.setContent(newContent);
        chapterRepository.save(chapter);
    }
    
    /**
     * 评分续写
     */
    @Transactional
    public void rateContinuation(Long continuationId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        
        AIContinuation continuation = continuationRepository.findById(continuationId)
                .orElseThrow(() -> new RuntimeException("续写不存在"));
        continuation.setRating(rating);
        continuationRepository.save(continuation);
    }
    
    /**
     * 获取章节的所有续写
     */
    public List<ContinuationResponse> getChapterContinuations(Long chapterId) {
        List<AIContinuation> continuations = continuationRepository.findByChapterIdOrderByCreatedAtDesc(chapterId);
        return continuations.stream()
                .map(c -> toResponse(c, null))
                .collect(Collectors.toList());
    }
    
    /**
     * 生成写作建议
     */
    @Transactional
    public List<SuggestionResponse> generateSuggestions(SuggestionRequest request) {
        log.info("生成写作建议: chapterId={}, type={}", request.getChapterId(), request.getSuggestionType());
        
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        // 构建建议提示词
        String prompt = buildSuggestionPrompt(chapter, request);
        
        // 调用AI生成建议
        String aiResponse = generateWithAI(prompt);
        
        // 解析AI响应并创建建议
        List<WritingSuggestion> suggestions = parseSuggestions(aiResponse, request, chapter);
        suggestions = suggestionRepository.saveAll(suggestions);
        
        return suggestions.stream()
                .map(this::toSuggestionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取章节建议
     */
    public List<SuggestionResponse> getChapterSuggestions(Long chapterId, String status) {
        List<WritingSuggestion> suggestions;
        if (status != null) {
            suggestions = suggestionRepository.findByChapterIdAndStatus(chapterId, status);
        } else {
            suggestions = suggestionRepository.findByChapterIdOrderByPriorityDescCreatedAtDesc(chapterId);
        }
        
        return suggestions.stream()
                .map(this::toSuggestionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新建议状态
     */
    @Transactional
    public void updateSuggestionStatus(Long suggestionId, String status) {
        WritingSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("建议不存在"));
        suggestion.setStatus(status);
        suggestionRepository.save(suggestion);
    }
    
    /**
     * 构建续写提示词
     */
    private String buildContinuationPrompt(Chapter chapter, ContinuationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下内容进行续写：\n\n");
        
        // 章节信息
        prompt.append("【章节标题】\n").append(chapter.getTitle()).append("\n\n");
        
        // 上文内容
        prompt.append("【上文内容】\n").append(request.getSourceText()).append("\n\n");
        
        // 额外上下文
        if (request.getAdditionalContext() != null) {
            prompt.append("【背景信息】\n").append(request.getAdditionalContext()).append("\n\n");
        }
        
        // 风格要求
        prompt.append("【写作风格】\n");
        switch (request.getStyle()) {
            case "SERIOUS" -> prompt.append("严肃认真，注重情节和人物心理\n");
            case "LIGHT" -> prompt.append("轻松幽默，语言生动活泼\n");
            case "SUSPENSE" -> prompt.append("悬疑紧张，营造氛围感\n");
            case "ROMANTIC" -> prompt.append("浪漫温馨，注重情感描写\n");
            case "ACTION" -> prompt.append("动作激烈，节奏紧凑\n");
            default -> prompt.append("自然流畅，符合上文风格\n");
        }
        
        // 长度要求
        prompt.append("\n【长度要求】\n");
        switch (request.getLength()) {
            case "SENTENCE" -> prompt.append("1-2句话，简短衔接\n");
            case "PARAGRAPH" -> prompt.append("1-2个段落，100-200字\n");
            case "SECTION" -> prompt.append("3-5个段落，300-500字\n");
            default -> prompt.append("2-3个段落，200-300字\n");
        }
        
        prompt.append("\n【生成要求】\n");
        prompt.append("1. 自然衔接上文，保持一致性\n");
        prompt.append("2. 推进情节发展，不要停滞\n");
        prompt.append("3. 保持人物性格一致\n");
        prompt.append("4. 只输出续写内容，不要额外说明\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建建议提示词
     */
    private String buildSuggestionPrompt(Chapter chapter, SuggestionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下章节内容并提供写作建议：\n\n");
        
        prompt.append("【章节标题】\n").append(chapter.getTitle()).append("\n\n");
        
        if (request.getContextText() != null) {
            prompt.append("【当前内容】\n").append(request.getContextText()).append("\n\n");
        } else {
            prompt.append("【章节内容】\n").append(chapter.getContent()).append("\n\n");
        }
        
        prompt.append("【建议类型】\n");
        switch (request.getSuggestionType()) {
            case "PLOT" -> prompt.append("情节发展建议（下一步走向、转折点等）\n");
            case "CONFLICT" -> prompt.append("冲突设计建议（冲突点、升级方式等）\n");
            case "CHARACTER" -> prompt.append("角色发展建议（性格展现、成长方向等）\n");
            case "DIALOGUE" -> prompt.append("对话优化建议（对话自然度、推进情节等）\n");
            case "PACING" -> prompt.append("节奏调整建议（快慢控制、张弛有度等）\n");
            default -> prompt.append("综合写作建议\n");
        }
        
        prompt.append("\n请按以下格式提供建议：\n");
        prompt.append("建议1: [具体建议内容]\n");
        prompt.append("理由: [为什么这样建议]\n");
        prompt.append("优先级: [HIGH/MEDIUM/LOW]\n");
        prompt.append("\n建议2: ...\n");
        
        return prompt.toString();
    }
    
    /**
     * 检查大纲一致性
     */
    private ContinuationResponse.OutlineCheck checkOutlineConsistency(Chapter chapter, String continuationText) {
        // 获取相关大纲
        List<Outline> outlines = outlineRepository.findByNovelIdOrderBySequenceNumberAsc(chapter.getNovelId());
        
        // 简单检查（实际应使用AI分析）
        boolean isConsistent = true;
        String issue = null;
        String suggestion = null;
        
        // TODO: 实现真实的一致性检查逻辑
        
        return ContinuationResponse.OutlineCheck.builder()
                .isConsistent(isConsistent)
                .issue(issue)
                .suggestion(suggestion)
                .build();
    }
    
    /**
     * 解析建议响应
     */
    private List<WritingSuggestion> parseSuggestions(String aiResponse, SuggestionRequest request, Chapter chapter) {
        List<WritingSuggestion> suggestions = new java.util.ArrayList<>();
        
        // 简单解析（实际应使用更复杂的解析逻辑）
        String[] parts = aiResponse.split("建议\\d+:");
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            
            String suggestionText = extractValue(part, null);
            String reasoning = extractValue(part, "理由");
            String priority = extractValue(part, "优先级");
            
            if (suggestionText != null && !suggestionText.isEmpty()) {
                WritingSuggestion suggestion = WritingSuggestion.builder()
                        .chapterId(request.getChapterId())
                        .suggestionType(request.getSuggestionType())
                        .suggestion(suggestionText)
                        .reasoning(reasoning)
                        .priority(priority != null ? priority : "MEDIUM")
                        .status("PENDING")
                        .contextText(request.getContextText())
                        .build();
                suggestions.add(suggestion);
            }
        }
        
        // 如果解析失败，创建一个默认建议
        if (suggestions.isEmpty()) {
            WritingSuggestion defaultSuggestion = WritingSuggestion.builder()
                    .chapterId(request.getChapterId())
                    .suggestionType(request.getSuggestionType())
                    .suggestion(aiResponse)
                    .priority("MEDIUM")
                    .status("PENDING")
                    .contextText(request.getContextText())
                    .build();
            suggestions.add(defaultSuggestion);
        }
        
        return suggestions;
    }
    
    /**
     * 从文本中提取值
     */
    private String extractValue(String text, String key) {
        if (text == null) return null;
        
        if (key == null) {
            // 提取第一行或第一个换行符之前的内容
            int newlineIndex = text.indexOf('\n');
            if (newlineIndex > 0) {
                return text.substring(0, newlineIndex).trim();
            }
            return text.trim();
        }
        
        int start = text.indexOf(key);
        if (start == -1) return null;
        
        start = text.indexOf(":", start);
        if (start == -1) start = text.indexOf("：", start);
        if (start == -1) return null;
        
        int end = text.indexOf("\n", start);
        if (end == -1) end = text.length();
        
        return text.substring(start + 1, end).trim();
    }
    
    /**
     * 使用AI生成文本
     */
    private String generateWithAI(String prompt) {
        // TODO: 集成实际的AI服务
        log.info("AI生成请求: {}", prompt.substring(0, Math.min(100, prompt.length())));
        
        if (prompt.contains("续写")) {
            return "主角深吸一口气，眼神变得坚定起来。窗外的夜色渐浓，远处传来若有若无的脚步声。" +
                   "他知道，真正的考验才刚刚开始。握紧手中的物品，他转身走向门口，" +
                   "心中已经做好了迎接一切的准备。";
        } else {
            return "建议1: 可以在此处增加一些心理描写，展现主角的内心挣扎\n" +
                   "理由: 当前情节发展较快，适当的心理描写能让读者更好地理解角色动机\n" +
                   "优先级: MEDIUM\n\n" +
                   "建议2: 考虑引入一个小冲突点，提升情节张力\n" +
                   "理由: 目前节奏稍显平缓，小冲突能调动读者兴趣\n" +
                   "优先级: HIGH";
        }
    }
    
    /**
     * 转换为响应对象
     */
    private ContinuationResponse toResponse(AIContinuation continuation, 
                                           ContinuationResponse.OutlineCheck outlineCheck) {
        return ContinuationResponse.builder()
                .id(continuation.getId())
                .chapterId(continuation.getChapterId())
                .sourceText(continuation.getSourceText())
                .continuationText(continuation.getContinuationText())
                .style(continuation.getStyle())
                .length(continuation.getLength())
                .promptUsed(continuation.getPromptUsed())
                .aiModel(continuation.getAiModel())
                .version(continuation.getVersion())
                .rating(continuation.getRating())
                .isApplied(continuation.getIsApplied())
                .appliedAt(continuation.getAppliedAt())
                .createdAt(continuation.getCreatedAt())
                .outlineCheck(outlineCheck)
                .build();
    }
    
    /**
     * 转换为建议响应对象
     */
    private SuggestionResponse toSuggestionResponse(WritingSuggestion suggestion) {
        String outlineTitle = null;
        if (suggestion.getRelatedOutlineId() != null) {
            outlineTitle = outlineRepository.findById(suggestion.getRelatedOutlineId())
                    .map(Outline::getTitle)
                    .orElse(null);
        }
        
        return SuggestionResponse.builder()
                .id(suggestion.getId())
                .chapterId(suggestion.getChapterId())
                .suggestionType(suggestion.getSuggestionType())
                .suggestion(suggestion.getSuggestion())
                .reasoning(suggestion.getReasoning())
                .priority(suggestion.getPriority())
                .status(suggestion.getStatus())
                .contextText(suggestion.getContextText())
                .relatedOutlineId(suggestion.getRelatedOutlineId())
                .relatedOutlineTitle(outlineTitle)
                .createdAt(suggestion.getCreatedAt())
                .updatedAt(suggestion.getUpdatedAt())
                .build();
    }
}
