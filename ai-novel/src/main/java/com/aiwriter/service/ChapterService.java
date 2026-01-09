package com.aiwriter.service;

import com.aiwriter.config.AppConfig;
import com.aiwriter.dto.*;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.ContentAnalysis;
import com.aiwriter.entity.Novel;
import com.aiwriter.entity.Outline;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.service.ai.AiService;
import com.aiwriter.service.ai.ContextManager;
import com.aiwriter.service.ai.PromptBuilderService;
import com.aiwriter.service.ai.PromptTemplates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterService {
    
    private final ChapterRepository chapterRepository;
    private final NovelService novelService;
    private final AiService aiService;
    private final ContextManager contextManager;
    private final PromptTemplates promptTemplates;
    private final AppConfig appConfig;
    private final EditHistoryService editHistoryService;
    private final ContentAnalysisService contentAnalysisService;
    private final CharacterService characterService;
    private final SceneService sceneService;
    private final ObjectMapper objectMapper;
    private final AutoExtractionService autoExtractionService;
    private final PromptBuilderService promptBuilderService;  // 新增：Qwen-Project.md动态Prompt构建
    private final NovelWritingStyleService novelWritingStyleService;  // 新增：四维风格画像服务
    private final OutlineGenerationService outlineGenerationService;  // 新增：大纲生成功能
    
    /**
     * 创建章节
     */
    @Transactional
    public Chapter createChapter(ChapterCreateRequest request) {
        Novel novel = novelService.getNovel(request.getNovelId());
        
        // 如果是第一章且没有大纲，自动生成大纲
        // 注意：ChapterCreateRequest不包含章节号，我们使用数据库中的最大章节号+1来确定
        Integer maxChapterNumber = chapterRepository
            .findMaxChapterNumber(request.getNovelId())
            .orElse(0);
        if (maxChapterNumber == 0) { // 第一章
            List<Outline> outlines = outlineGenerationService.generateOutline(request.getNovelId());
            log.info("为小说 {} 自动生成了 {} 个大纲节点", request.getNovelId(), outlines.size());
        }
        
        // 获取下一个章节号
        Integer nextChapterNumber = maxChapterNumber + 1;
        
        // 创建章节
        Chapter chapter = new Chapter();
        chapter.setNovelId(request.getNovelId());
        chapter.setTitle(request.getTitle());
        chapter.setContent(request.getContent());
        chapter.setChapterNumber(nextChapterNumber);
        chapter.setSceneId(request.getSceneId());
        chapter.setOutlineNodeId(request.getOutlineNodeId());
        chapter.setContinuationDirection(request.getContinuationDirection());
        chapter.setIsAiGenerated(false);
        
        // 统计字数
        int wordCount = aiService.countWords(request.getContent());
        chapter.setWordCount(wordCount);
        
        Chapter saved = chapterRepository.save(chapter);
        
        // 更新小说统计
        novelService.updateNovelStats(request.getNovelId());
        
        // 🔥 触发自动提取（异步执行）
        autoExtractionService.extractAllFromChapter(saved);
        
        log.info("创建章节成功: novelId={}, chapterNumber={}, words={}", request.getNovelId(), nextChapterNumber, wordCount);
        
        return saved;
    }
    
    /**
     * AI续写章节
     */
    @Transactional
    public Chapter continueChapter(ChapterContinueRequest request) {
        Novel novel = novelService.getNovel(request.getNovelId());
        
        // 构建上下文
        int contextChapters = appConfig.getNovel().getMaxContextChapters();
        String context = contextManager.buildContext(
            request.getNovelId(), 
            request.getSceneId(), 
            contextChapters
        );
        
        // 获取增强上下文信息（角色、场景、最新分析）
        String enhancedContext = buildEnhancedContext(request.getNovelId(), request.getSceneId(), context);
        
        // 🔥 使用PromptBuilderService动态构建Prompt（Qwen-Project.md核心功能）
        // 如果小说有风格画像，使用完整Prompt；否则降级到简单模式
        Integer nextChapterNumber = chapterRepository
            .findMaxChapterNumber(request.getNovelId())
            .orElse(0) + 1;
        
        String fullPrompt;
        try {
            fullPrompt = promptBuilderService.buildFullPrompt(
                request.getNovelId(),
                nextChapterNumber
            );
            log.info("使用四维风格画像动态构建Prompt");
        } catch (Exception e) {
            log.warn("风格画像构建失败，降级到简单模式: {}", e.getMessage());
            fullPrompt = promptBuilderService.buildSimplePrompt(
                request.getNovelId(),
                nextChapterNumber,
                request.getDirection(),
                request.getTargetWordCount()
            );
        }
        
        String systemPrompt = PromptTemplates.CHAPTER_CONTINUE_SYSTEM;
        String userPrompt = fullPrompt; // 使用动态构建的Prompt
        
        log.info("开始AI续写: novelId={}, chapterNumber={}, targetWords={}", 
            request.getNovelId(), nextChapterNumber, request.getTargetWordCount());
        
        // 调用AI生成
        String generatedContent = aiService.chat(systemPrompt, userPrompt);
        String cleanedContent = aiService.extractContent(generatedContent);
        
        Chapter chapter = new Chapter();
        chapter.setNovelId(request.getNovelId());
        chapter.setTitle("第" + nextChapterNumber + "章"); // 自动生成标题
        chapter.setContent(cleanedContent);
        chapter.setChapterNumber(nextChapterNumber);
        chapter.setSceneId(request.getSceneId());
        chapter.setContinuationDirection(request.getDirection());
        chapter.setIsAiGenerated(true);
        
        int wordCount = aiService.countWords(cleanedContent);
        chapter.setWordCount(wordCount);
        
        Chapter saved = chapterRepository.save(chapter);
        
        // 更新小说统计
        novelService.updateNovelStats(request.getNovelId());
        
        // 🔥 触发自动提取（异步执行）
        autoExtractionService.extractAllFromChapter(saved);
        
        // 续写完成后自动分析并更新状态
        autoAnalyzeAfterContinuation(saved);
        
        log.info("AI续写完成: chapterId={}, words={}, 已触发自动提取", saved.getId(), wordCount);
        
        return saved;
    }
    
    /**
     * 获取章节详情
     */
    public Chapter getChapter(Long id) {
        return chapterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("章节不存在: " + id));
    }
    
    /**
     * 获取小说的所有章节
     */
    public List<Chapter> getChaptersByNovel(Long novelId) {
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
    }
    
    /**
     * 更新章节
     */
    @Transactional
    public Chapter updateChapter(Long id, ChapterCreateRequest request) {
        Chapter chapter = getChapter(id);
        
        // 保存修改前的内容用于历史记录
        String contentBefore = chapter.getContent();
        boolean contentChanged = false;
        
        if (request.getTitle() != null) {
            chapter.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            chapter.setContent(request.getContent());
            int wordCount = aiService.countWords(request.getContent());
            chapter.setWordCount(wordCount);
            contentChanged = true;
            
            // 记录编辑历史
            try {
                editHistoryService.recordEdit(id, "UPDATE", contentBefore, 
                        request.getContent(), "用户手动编辑");
            } catch (Exception e) {
                log.error("记录编辑历史失败", e);
            }
        }
        if (request.getSceneId() != null) {
            chapter.setSceneId(request.getSceneId());
        }
        if (request.getOutlineNodeId() != null) {
            chapter.setOutlineNodeId(request.getOutlineNodeId());
        }
        
        Chapter updated = chapterRepository.save(chapter);
        
        // 更新小说统计
        novelService.updateNovelStats(chapter.getNovelId());
        
        // 🔥 如果内容有变化，触发自动提取（异步执行）
        if (contentChanged) {
            autoExtractionService.extractAllFromChapter(updated);
            log.info("章节 {} 内容已更新，已触发自动提取", id);
        }
        
        // log.info("更新章节成功: id={}", id);
        return updated;
    }
    
    /**
     * 删除章节
     */
    @Transactional
    public void deleteChapter(Long id) {
        Chapter chapter = getChapter(id);
        Long novelId = chapter.getNovelId();
        
        chapterRepository.delete(chapter);
        
        // 更新小说统计
        novelService.updateNovelStats(novelId);
        
        // log.info("删除章节成功: id={}", id);
    }
    
    /**
     * 重新提取章节元数据
     */
    public void reExtractChapter(Long id) {
        Chapter chapter = getChapter(id);
        log.info("手动触发章节{}的元数据重新提取", id);
        
        // 触发异步提取
        autoExtractionService.extractAllFromChapter(chapter);
    }
    
    /**
     * 重新生成章节
     */
    @Transactional
    public Chapter regenerateChapter(Long id, String direction) {
        Chapter oldChapter = getChapter(id);
        
        // 删除旧章节
        chapterRepository.delete(oldChapter);
        
        // 重新生成
        ChapterContinueRequest request = new ChapterContinueRequest();
        request.setNovelId(oldChapter.getNovelId());
        request.setSceneId(oldChapter.getSceneId());
        request.setDirection(direction);
        request.setTargetWordCount(oldChapter.getWordCount());
        
        return continueChapter(request);
    }
    
    /**
     * 智能创建章节
     * 自动分析正文内容，提取标题、摘要、关键词、角色、场景等元数据
     */
    @Transactional
    public SmartChapterCreateResponse createChapterSmart(SmartChapterCreateRequest request) {
        log.info("开始智能章节创建: novelId={}, contentLength={}", 
            request.getNovelId(), request.getContent().length());
        
        Novel novel = novelService.getNovel(request.getNovelId());
        
        // 1. 提取章节标题（如果用户未提供）
        String title = request.getTitle();
        String extractedTitle = null;
        if (title == null || title.trim().isEmpty()) {
            log.info("标题为空，开始自动提取...");
            title = extractTitle(request.getContent(), novel);
            extractedTitle = title;
            log.info("自动提取标题: {}", title);
        }
        
        // 2. 分析章节内容
        log.info("开始分析章节内容...");
        ContentAnalysisRequest analysisRequest = new ContentAnalysisRequest();
        analysisRequest.setNovelId(request.getNovelId());
        analysisRequest.setContent(request.getContent());
        analysisRequest.setDeepAnalysis(request.getDeepAnalysis());
        
        ContentAnalysisResponse analysisResponse = contentAnalysisService.analyzeContent(analysisRequest);
        log.info("内容分析完成: analysisId={}", analysisResponse.getId());
        
        // 2.1 自动提取并新增角色信息
        autoCreateCharacters(request.getNovelId(), analysisResponse);
        
        // 2.2 自动识别并更新/新增场景信息
        Long actualSceneId = autoCreateOrUpdateScene(request.getNovelId(), analysisResponse, request.getSceneId());
        
        // 3. 生成章节摘要
        String summary = generateSummary(analysisResponse);
        log.info("生成摘要: length={}", summary.length());
        
        // 4. 生成续写方向
        String continuationDirection = null;
        if (request.getGenerateContinuationDirection()) {
            log.info("开始生成续写方向...");
            continuationDirection = generateContinuationDirection(
                request.getContent(), 
                analysisResponse, 
                novel
            );
            log.info("续写方向生成完成: length={}", 
                continuationDirection != null ? continuationDirection.length() : 0);
        }
        
        // 5. 创建章节
        Integer nextChapterNumber = chapterRepository
            .findMaxChapterNumber(request.getNovelId())
            .orElse(0) + 1;
        
        Chapter chapter = new Chapter();
        chapter.setNovelId(request.getNovelId());
        chapter.setTitle(title);
        chapter.setContent(request.getContent());
        chapter.setChapterNumber(nextChapterNumber);
        chapter.setSceneId(actualSceneId != null ? actualSceneId : request.getSceneId());
        chapter.setOutlineNodeId(request.getOutlineNodeId());
        chapter.setSummary(summary);
        chapter.setContinuationDirection(continuationDirection);
        chapter.setIsAiGenerated(false);
        
        // 统计字数
        int wordCount = aiService.countWords(request.getContent());
        chapter.setWordCount(wordCount);
        
        Chapter saved = chapterRepository.save(chapter);
        log.info("章节创建成功: id={}, chapterNumber={}, wordCount={}", 
            saved.getId(), nextChapterNumber, wordCount);
        
        // 6. 更新分析记录的章节ID
        contentAnalysisService.updateChapterId(analysisResponse.getId(), saved.getId());
        
        // 7. 更新小说统计
        novelService.updateNovelStats(request.getNovelId());
        
        // 8. 构建响应
        SmartChapterCreateResponse response = buildSmartResponse(
            saved, 
            analysisResponse, 
            extractedTitle
        );
        
        log.info("智能章节创建完成: chapterId={}, extractedMetadata={}", 
            saved.getId(), response.getKeywords() != null ? response.getKeywords().size() : 0);
        
        return response;
    }
    
    /**
     * 自动提取章节标题
     */
    private String extractTitle(String content, Novel novel) {
        String prompt = buildTitleExtractionPrompt(content, novel);
        String result = aiService.chat(
            "你是一位专业的小说编辑，擅长为章节提取简洁有力的标题。",
            prompt
        );
        
        // 清理标题（去除引号、多余空格等）
        String title = result.trim()
            .replaceAll("^[\"'《]|[\"'》]$", "")
            .replaceAll("^第.*?章[：:]?\\s*", "");
        
        // 限制标题长度
        if (title.length() > 50) {
            title = title.substring(0, 50);
        }
        
        return title.isEmpty() ? "第" + (getNextChapterNumber(novel.getId())) + "章" : title;
    }
    
    /**
     * 构建标题提取提示词
     */
    private String buildTitleExtractionPrompt(String content, Novel novel) {
        // 只取内容的前500字用于标题提取
        String snippet = content.length() > 500 ? content.substring(0, 500) : content;
        
        return String.format(
            "请为以下小说章节内容提取一个简洁有力的标题（10-20字）：\n\n" +
            "=== 小说信息 ===\n" +
            "标题：%s\n" +
            "类型：%s\n\n" +
            "=== 章节内容（片段） ===\n" +
            "%s\n\n" +
            "要求：\n" +
            "1. 标题应反映本章的核心情节或冲突\n" +
            "2. 避免使用第X章等序号\n" +
            "3. 语言简洁有力，吸引读者\n" +
            "4. 不要使用引号或书名号\n" +
            "5. 直接输出标题文本，不要有其他说明\n",
            novel.getTitle(),
            novel.getGenre() != null ? novel.getGenre() : "未知",
            snippet
        );
    }
    
    /**
     * 生成章节摘要
     */
    private String generateSummary(ContentAnalysisResponse analysis) {
        StringBuilder summary = new StringBuilder();
        
        // 主要情节
        if (analysis.getCurrentConflict() != null) {
            summary.append("情节：").append(analysis.getCurrentConflict()).append("\n");
        }
        
        // 主要角色
        if (analysis.getProtagonistName() != null && !analysis.getProtagonistName().isEmpty()) {
            summary.append("主角：").append(analysis.getProtagonistName()).append("\n");
        }
        
        // 场景
        if (analysis.getSceneInfo() != null && analysis.getSceneInfo().getDescription() != null) {
            summary.append("场景：").append(analysis.getSceneInfo().getDescription());
        }
        
        return summary.toString().trim();
    }
    
    /**
     * 生成续写方向
     */
    private String generateContinuationDirection(
        String content, 
        ContentAnalysisResponse analysis,
        Novel novel
    ) {
        String prompt = buildContinuationDirectionPrompt(content, analysis, novel);
        return aiService.chat(
            "你是一位资深小说编辑，擅长为作者提供续写建议。",
            prompt
        );
    }
    
    /**
     * 构建续写方向提示词
     */
    private String buildContinuationDirectionPrompt(
        String content,
        ContentAnalysisResponse analysis,
        Novel novel
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("根据当前章节内容，为下一章提供3-5条续写建议：\n\n");
        
        prompt.append("=== 小说信息 ===\n");
        prompt.append("标题：").append(novel.getTitle()).append("\n");
        prompt.append("类型：").append(novel.getGenre() != null ? novel.getGenre() : "未知").append("\n\n");
        
        prompt.append("=== 当前章节分析 ===\n");
        if (analysis.getCurrentConflict() != null) {
            prompt.append("冲突：").append(analysis.getCurrentConflict()).append("\n");
        }
        if (analysis.getEmotionalTone() != null) {
            prompt.append("情感：").append(analysis.getEmotionalTone()).append("\n");
        }
        
        if (analysis.getSceneInfo() != null && analysis.getSceneInfo().getDescription() != null) {
            prompt.append("场景：").append(analysis.getSceneInfo().getDescription()).append("\n");
        }
        
        // 内容片段（最后200字）
        int start = Math.max(0, content.length() - 200);
        String ending = content.substring(start);
        prompt.append("\n=== 章节结尾 ===\n");
        prompt.append(ending).append("\n\n");
        
        prompt.append("请提供：\n");
        prompt.append("1. 可能的情节发展方向\n");
        prompt.append("2. 潜在的冲突升级点\n");
        prompt.append("3. 角色关系的变化\n");
        prompt.append("4. 伏笔的铺设建议\n");
        prompt.append("\n直接输出续写建议，每条建议用换行分隔，不要编号。");
        
        return prompt.toString();
    }
    
    /**
     * 构建智能响应
     */
    private SmartChapterCreateResponse buildSmartResponse(
        Chapter chapter,
        ContentAnalysisResponse analysis,
        String extractedTitle
    ) {
        SmartChapterCreateResponse response = new SmartChapterCreateResponse();
        
        // 基础信息
        response.setId(chapter.getId());
        response.setNovelId(chapter.getNovelId());
        response.setChapterNumber(chapter.getChapterNumber());
        response.setTitle(chapter.getTitle());
        response.setContent(chapter.getContent());
        response.setWordCount(chapter.getWordCount());
        response.setSummary(chapter.getSummary());
        response.setContinuationDirection(chapter.getContinuationDirection());
        response.setCreatedAt(chapter.getCreatedAt());
        
        // 提取的元数据
        response.setExtractedTitle(extractedTitle);
        response.setAnalysisId(analysis.getId());
        response.setCompletenessScore(analysis.getCompletenessScore());
        
        // 提取关键词
        response.setKeywords(extractKeywords(analysis));
        
        // 角色信息
        response.setCharacters(extractCharacters(analysis));
        
        // 场景信息
        if (analysis.getSceneInfo() != null) {
            SmartChapterCreateResponse.SceneInfo sceneInfo = new SmartChapterCreateResponse.SceneInfo();
            sceneInfo.setDescription(analysis.getSceneInfo().getDescription());
            sceneInfo.setLocation(analysis.getSceneInfo().getLocation());
            sceneInfo.setTime(analysis.getSceneInfo().getTime());
            sceneInfo.setAtmosphere(analysis.getSceneInfo().getAtmosphere());
            response.setScene(sceneInfo);
        }
        
        // 情节信息
        if (analysis.getCurrentConflict() != null || analysis.getEmotionalTone() != null) {
            SmartChapterCreateResponse.PlotInfo plotInfo = new SmartChapterCreateResponse.PlotInfo();
            plotInfo.setConflict(analysis.getCurrentConflict());
            plotInfo.setEmotionalTone(analysis.getEmotionalTone());
            plotInfo.setForeshadowing(null); // ContentAnalysisResponse中没有foreshadowing字段
            response.setPlot(plotInfo);
        }
        
        // 风格信息
        if (analysis.getStyleInfo() != null) {
            SmartChapterCreateResponse.StyleInfo styleInfo = new SmartChapterCreateResponse.StyleInfo();
            styleInfo.setType(analysis.getStyleInfo().getStyle());
            styleInfo.setDescription(analysis.getStyleInfo().getDescription());
            styleInfo.setPacing(null); // WritingStyleDTO中没有pacing字段
            response.setStyle(styleInfo);
        }
        
        return response;
    }
    
    /**
     * 提取关键词
     */
    private List<String> extractKeywords(ContentAnalysisResponse analysis) {
        List<String> keywords = new ArrayList<>();
        
        // 从主角名称提取
        if (analysis.getProtagonistName() != null && !analysis.getProtagonistName().isEmpty()) {
            keywords.add(analysis.getProtagonistName());
        }
        
        // 从场景位置提取
        if (analysis.getSceneInfo() != null && analysis.getSceneInfo().getLocation() != null) {
            keywords.add(analysis.getSceneInfo().getLocation());
        }
        
        // 从情节冲突提取
        if (analysis.getCurrentConflict() != null) {
            String conflict = analysis.getCurrentConflict();
            // 简单提取：取前10个字作为关键词
            if (conflict.length() > 10) {
                keywords.add(conflict.substring(0, 10));
            } else {
                keywords.add(conflict);
            }
        }
        
        return keywords;
    }
    
    /**
     * 提取角色信息
     */
    private List<SmartChapterCreateResponse.CharacterInfo> extractCharacters(ContentAnalysisResponse analysis) {
        List<SmartChapterCreateResponse.CharacterInfo> characters = new ArrayList<>();
        
        // 直接使用extractedCharacters列表
        if (analysis.getExtractedCharacters() != null) {
            for (ContentAnalysisResponse.ExtractedCharacterDTO dto : analysis.getExtractedCharacters()) {
                SmartChapterCreateResponse.CharacterInfo character = 
                    new SmartChapterCreateResponse.CharacterInfo();
                character.setName(dto.getName());
                character.setRole(dto.getRole());
                character.setDescription(dto.getDescription() != null ? dto.getDescription() : dto.getTraits());
                
                if (character.getName() != null) {
                    characters.add(character);
                }
            }
        }
        
        // 如果没有解析到角色，至少添加主角
        if (characters.isEmpty() && analysis.getProtagonistName() != null) {
            SmartChapterCreateResponse.CharacterInfo protagonist = 
                new SmartChapterCreateResponse.CharacterInfo();
            protagonist.setName(analysis.getProtagonistName());
            protagonist.setRole("主角");
            characters.add(protagonist);
        }
        
        return characters;
    }
    
    /**
     * 构建增强上下文（包含角色、场景、最新分析等信息）
     */
    private String buildEnhancedContext(Long novelId, Long sceneId, String basicContext) {
        StringBuilder enhanced = new StringBuilder();
        
        // 1. 基础上下文
        enhanced.append("=== 已有章节内容 ===\n");
        enhanced.append(basicContext).append("\n\n");
        
        // 2. 角色信息
        try {
            List<com.aiwriter.entity.Character> characters = characterService.getCharactersByNovel(novelId);
            if (!characters.isEmpty()) {
                enhanced.append("=== 角色信息 ===\n");
                for (com.aiwriter.entity.Character character : characters) {
                    enhanced.append("【").append(character.getName()).append("】");
                    if (character.getRoleType() != null) {
                        enhanced.append("(").append(character.getRoleType()).append(")");
                    }
                    if (character.getPersonality() != null && !character.getPersonality().isEmpty()) {
                        enhanced.append(" - ").append(character.getPersonality());
                    }
                    enhanced.append("\n");
                }
                enhanced.append("\n");
            }
        } catch (Exception e) {
            log.warn("获取角色信息失败: {}", e.getMessage());
        }
        
        // 3. 当前场景信息
        if (sceneId != null) {
            try {
                com.aiwriter.entity.Scene scene = sceneService.getSceneById(sceneId);
                enhanced.append("=== 当前场景 ===\n");
                enhanced.append("场景名称：").append(scene.getName()).append("\n");
                if (scene.getLocation() != null) {
                    enhanced.append("地点：").append(scene.getLocation()).append("\n");
                }
                if (scene.getTimePeriod() != null) {
                    enhanced.append("时间：").append(scene.getTimePeriod()).append("\n");
                }
                if (scene.getAtmosphere() != null) {
                    enhanced.append("氛围：").append(scene.getAtmosphere()).append("\n");
                }
                if (scene.getDescription() != null && !scene.getDescription().isEmpty()) {
                    enhanced.append("描述：").append(scene.getDescription()).append("\n");
                }
                enhanced.append("\n");
            } catch (Exception e) {
                log.warn("获取场景信息失败: {}", e.getMessage());
            }
        }
        
        // 4. 最新内容分析
        try {
            List<ContentAnalysis> analyses = contentAnalysisService.getAnalysisByNovel(novelId);
            if (!analyses.isEmpty()) {
                ContentAnalysis latest = analyses.get(0); // 最新的分析
                enhanced.append("=== 最新内容分析 ===\n");
                if (latest.getProtagonistName() != null) {
                    enhanced.append("主角：").append(latest.getProtagonistName()).append("\n");
                }
                if (latest.getCurrentConflict() != null) {
                    enhanced.append("当前冲突：").append(latest.getCurrentConflict()).append("\n");
                }
                if (latest.getEmotionalTone() != null) {
                    enhanced.append("情感基调：").append(latest.getEmotionalTone()).append("\n");
                }
                if (latest.getWritingStyle() != null) {
                    enhanced.append("写作风格：").append(latest.getWritingStyle()).append("\n");
                }
                if (latest.getPlotPace() != null) {
                    enhanced.append("情节节奏：").append(latest.getPlotPace()).append("\n");
                }
                enhanced.append("\n");
            }
        } catch (Exception e) {
            log.warn("获取内容分析失败: {}", e.getMessage());
        }
        
        return enhanced.toString();
    }
    
    /**
     * 续写完成后自动分析并更新状态
     */
    private void autoAnalyzeAfterContinuation(Chapter chapter) {
        try {
            log.info("续写完成，开始自动分析章节: chapterId={}", chapter.getId());
            
            // 执行内容分析
            ContentAnalysisRequest analysisRequest = new ContentAnalysisRequest();
            analysisRequest.setNovelId(chapter.getNovelId());
            analysisRequest.setContent(chapter.getContent());
            analysisRequest.setDeepAnalysis(false); // 快速分析模式
            
            ContentAnalysisResponse analysis = contentAnalysisService.analyzeContent(analysisRequest);
            
            // 更新分析记录的章节ID
            contentAnalysisService.updateChapterId(analysis.getId(), chapter.getId());
            
            // 生成新的摘要和续写方向
            String summary = generateSummary(analysis);
            String continuationDirection = generateContinuationDirection(
                chapter.getContent(), 
                analysis,
                novelService.getNovel(chapter.getNovelId())
            );
            
            // 更新章节
            chapter.setSummary(summary);
            chapter.setContinuationDirection(continuationDirection);
            chapterRepository.save(chapter);
            
            // 自动提取并创建新角色
            autoCreateCharacters(chapter.getNovelId(), analysis);
            
            // 自动更新场景信息
            if (chapter.getSceneId() != null) {
                autoCreateOrUpdateScene(chapter.getNovelId(), analysis, chapter.getSceneId());
            }
            
            log.info("续写后自动分析完成: chapterId={}, analysisId={}", 
                chapter.getId(), analysis.getId());
        } catch (Exception e) {
            log.error("续写后自动分析失败: chapterId={}, error={}", 
                chapter.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 获取下一个章节号
     */
    private Integer getNextChapterNumber(Long novelId) {
        return chapterRepository.findMaxChapterNumber(novelId).orElse(0) + 1;
    }
    
    /**
     * 自动创建角色
     * 从内容分析中提取角色信息，如果角色不存在则自动创建
     */
    private void autoCreateCharacters(Long novelId, ContentAnalysisResponse analysis) {
        if (analysis.getExtractedCharacters() == null || analysis.getExtractedCharacters().isEmpty()) {
            log.info("未提取到角色信息");
            return;
        }
        
        log.info("开始自动创建/更新角色，共 {} 个", analysis.getExtractedCharacters().size());
        
        // 获取现有角色列表
        List<com.aiwriter.entity.Character> existingCharacters = characterService.getCharactersByNovel(novelId);
        java.util.Map<String, com.aiwriter.entity.Character> existingNameMap = existingCharacters.stream()
            .collect(java.util.stream.Collectors.toMap(
                com.aiwriter.entity.Character::getName, 
                character -> character,
                (existing, replacement) -> existing // 如果有重复键，保留现有值
            ));
        
        // 创建一个映射，用于存储规范化后的角色名到原始角色的映射
        java.util.Map<String, com.aiwriter.entity.Character> normalizedNameMap = new java.util.HashMap<>();
        for (com.aiwriter.entity.Character character : existingCharacters) {
            String normalized = normalizeCharacterName(character.getName());
            if (!normalizedNameMap.containsKey(normalized)) {
                normalizedNameMap.put(normalized, character);
            }
        }
        
        int processedCount = 0;
        for (ContentAnalysisResponse.ExtractedCharacterDTO dto : analysis.getExtractedCharacters()) {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                continue;
            }
            
            try {
                com.aiwriter.entity.Character character;
                boolean isUpdate = existingNameMap.containsKey(dto.getName());
                
                if (isUpdate) {
                    // 更新现有角色
                    character = existingNameMap.get(dto.getName());
                    log.debug("更新现有角色: {}", dto.getName());
                    
                    // 更新角色类型
                    if (dto.getRole() != null) {
                        character.setRoleType(mapRoleType(dto.getRole()));
                    }
                    
                    // 更新性格特征和描述
                    if (dto.getTraits() != null) {
                        character.setPersonality(dto.getTraits());
                    }
                    if (dto.getDescription() != null) {
                        character.setBackground(dto.getDescription());
                    }
                    
                    // 更新是否为整体主角
                    if (dto.getIsGlobalProtagonist() != null) {
                        character.setIsGlobalProtagonist(dto.getIsGlobalProtagonist());
                    } else if (character.getRoleType() != null && "PROTAGONIST".equals(character.getRoleType())) {
                        // 如果角色类型是主角，默认为整体主角
                        character.setIsGlobalProtagonist(true);
                    }
                    
                    // 更新重要性级别
                    if (dto.getImportanceLevel() != null) {
                        character.setImportanceLevel(dto.getImportanceLevel());
                    } else {
                        // 根据角色类型设置默认重要性
                        if (character.getImportanceLevel() == null || character.getImportanceLevel() == 0) {
                            character.setImportanceLevel(getDefaultImportanceLevel(character.getRoleType()));
                        }
                    }
                    
                    characterService.updateCharacter(character.getId(), character);
                    log.info("自动更新角色成功: {} (整体主角: {}, 重要性: {})", 
                        dto.getName(), character.getIsGlobalProtagonist(), character.getImportanceLevel());
                } else {
                    // 创建新角色
                    character = new com.aiwriter.entity.Character();
                    character.setNovelId(novelId);
                    character.setName(dto.getName());
                    character.setRoleType(mapRoleType(dto.getRole()));
                    
                    // 设置性格特征和描述
                    if (dto.getTraits() != null) {
                        character.setPersonality(dto.getTraits());
                    }
                    if (dto.getDescription() != null) {
                        character.setBackground(dto.getDescription());
                    }
                    
                    // 设置是否为整体主角
                    if (dto.getIsGlobalProtagonist() != null) {
                        character.setIsGlobalProtagonist(dto.getIsGlobalProtagonist());
                    } else {
                        // 如果角色类型是主角，默认为整体主角
                        character.setIsGlobalProtagonist("PROTAGONIST".equals(character.getRoleType()));
                    }
                    
                    // 设置重要性级别
                    if (dto.getImportanceLevel() != null) {
                        character.setImportanceLevel(dto.getImportanceLevel());
                    } else {
                        // 根据角色类型设置默认重要性
                        character.setImportanceLevel(getDefaultImportanceLevel(character.getRoleType()));
                    }
                    
                    characterService.createCharacter(character);
                    processedCount++;
                    log.info("自动创建角色成功: {} (整体主角: {}, 重要性: {})", 
                        dto.getName(), character.getIsGlobalProtagonist(), character.getImportanceLevel());
                }
            } catch (Exception e) {
                log.warn("自动创建/更新角色失败: {}, 错误: {}", dto.getName(), e.getMessage());
            }
        }
        
        log.info("自动创建/更新角色完成，成功处理 {} 个", processedCount);
    }
    
    /**
     * 规范化角色名称，移除标点符号和空格，转换为小写，用于比较
     */
    private String normalizeCharacterName(String name) {
        if (name == null) {
            return "";
        }
        // 移除空格和标点符号，转换为小写
        return name.replaceAll("[\\s\\p{Punct}]", "").toLowerCase();
    }
    
    /**
     * 获取默认重要性级别
     */
    private Integer getDefaultImportanceLevel(String roleType) {
        return switch (roleType) {
            case "PROTAGONIST" -> 10;  // 主角最高
            case "ANTAGONIST" -> 9;    // 反派很重要
            case "SUPPORTING" -> 6;    // 配角中等
            case "MINOR" -> 3;         // 次要角色较低
            default -> 5;
        };
    }
    
    /**
     * 判断两个角色名称是否相似（用于合并相同角色）
     */
    private boolean isSimilarName(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return false;
        }
        
        // 检查是否为"名称(真名)"格式
        String baseName1 = extractBaseName(name1);
        String baseName2 = extractBaseName(name2);
        
        // 如果基础名称相同，则认为是相似的
        if (baseName1.equals(baseName2)) {
            return true;
        }
        
        // 避免部分匹配（如"李明"和"李明华"）
        // 只有当一个是另一个的完全子串且长度差异不大时才认为相似
        if (name1.length() > name2.length()) {
            return name1.startsWith(name2) && name1.length() - name2.length() <= 2;
        } else if (name2.length() > name1.length()) {
            return name2.startsWith(name1) && name2.length() - name1.length() <= 2;
        }
        
        return false;
    }
    
    /**
     * 提取基础名称（去除括号及其内容）
     */
    private String extractBaseName(String name) {
        if (name == null) {
            return "";
        }
        
        // 使用正则表达式提取括号前的部分
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(.*?)\\s*\\([^)]*\\)");
        java.util.regex.Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有括号，返回原名称
        return name.trim();
    }
    
    /**
     * 提取真名（从括号中提取）
     */
    private String extractRealName(String name) {
        if (name == null) {
            return null;
        }
        
        // 使用正则表达式提取括号中的内容
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\(([^)]*)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    /**
     * 映射角色类型
     */
    private String mapRoleType(String role) {
        if (role == null) {
            return "SUPPORTING";
        }
        
        String roleUpper = role.toUpperCase();
        if (roleUpper.contains("主角") || roleUpper.contains("PROTAGONIST")) {
            return "PROTAGONIST";
        } else if (roleUpper.contains("反派") || roleUpper.contains("ANTAGONIST")) {
            return "ANTAGONIST";
        } else if (roleUpper.contains("配角") || roleUpper.contains("SUPPORTING")) {
            return "SUPPORTING";
        } else {
            return "MINOR";
        }
    }
    
    /**
     * 自动创建或更新场景
     * 从内容分析中提取场景信息，如果场景不存在则创建，存在则更新
     */
    private Long autoCreateOrUpdateScene(Long novelId, ContentAnalysisResponse analysis, Long specifiedSceneId) {
        if (analysis.getSceneInfo() == null) {
            log.info("未提取到场景信息");
            return specifiedSceneId;
        }
        
        ContentAnalysisResponse.SceneInfoDTO sceneInfo = analysis.getSceneInfo();
        log.info("开始自动处理场景: location={}, time={}", 
            sceneInfo.getLocation(), sceneInfo.getTime());
        
        // 如果用户指定了场景ID，更新该场景
        if (specifiedSceneId != null) {
            try {
                com.aiwriter.entity.Scene existingScene = sceneService.getSceneById(specifiedSceneId);
                
                // 更新场景信息
                if (sceneInfo.getDescription() != null && !sceneInfo.getDescription().isEmpty()) {
                    existingScene.setDescription(sceneInfo.getDescription());
                }
                if (sceneInfo.getLocation() != null && !sceneInfo.getLocation().isEmpty()) {
                    existingScene.setLocation(sceneInfo.getLocation());
                }
                if (sceneInfo.getTime() != null && !sceneInfo.getTime().isEmpty()) {
                    existingScene.setTimePeriod(sceneInfo.getTime());
                }
                if (sceneInfo.getAtmosphere() != null && !sceneInfo.getAtmosphere().isEmpty()) {
                    existingScene.setAtmosphere(sceneInfo.getAtmosphere());
                }
                
                sceneService.updateScene(specifiedSceneId, existingScene);
                log.info("更新现有场景成功: id={}", specifiedSceneId);
                return specifiedSceneId;
            } catch (Exception e) {
                log.warn("更新场景失败: {}", e.getMessage());
            }
        }
        
        // 如果没有指定场景，检查是否存在相似场景
        List<com.aiwriter.entity.Scene> existingScenes = sceneService.getScenesByNovel(novelId);
        
        // 根据位置查找相似场景
        if (sceneInfo.getLocation() != null) {
            for (com.aiwriter.entity.Scene scene : existingScenes) {
                if (scene.getLocation() != null && 
                    scene.getLocation().contains(sceneInfo.getLocation())) {
                    log.info("找到相似场景: id={}, location={}", scene.getId(), scene.getLocation());
                    
                    // 更新场景的时间和氛围信息
                    if (sceneInfo.getTime() != null) {
                        scene.setTimePeriod(sceneInfo.getTime());
                    }
                    if (sceneInfo.getAtmosphere() != null) {
                        scene.setAtmosphere(sceneInfo.getAtmosphere());
                    }
                    
                    try {
                        sceneService.updateScene(scene.getId(), scene);
                        log.info("更新相似场景成功: id={}", scene.getId());
                        return scene.getId();
                    } catch (Exception e) {
                        log.warn("更新相似场景失败: {}", e.getMessage());
                    }
                }
            }
        }
        
        // 创建新场景
        try {
            com.aiwriter.entity.Scene newScene = new com.aiwriter.entity.Scene();
            newScene.setNovelId(novelId);
            
            // 生成场景名称
            String sceneName = sceneInfo.getLocation() != null ? 
                sceneInfo.getLocation() : "未命名场景";
            if (sceneInfo.getTime() != null) {
                sceneName += " - " + sceneInfo.getTime();
            }
            newScene.setName(sceneName);
            
            newScene.setSceneType("LOCATION");
            newScene.setDescription(sceneInfo.getDescription());
            newScene.setLocation(sceneInfo.getLocation());
            newScene.setTimePeriod(sceneInfo.getTime());
            newScene.setAtmosphere(sceneInfo.getAtmosphere());
            
            com.aiwriter.entity.Scene created = sceneService.createScene(newScene);
            log.info("自动创建场景成功: id={}, name={}", created.getId(), created.getName());
            return created.getId();
        } catch (Exception e) {
            log.warn("自动创建场景失败: {}", e.getMessage());
            return null;
        }
    }
}