package com.ai.novel.service;

import com.ai.novel.dto.request.ChapterContinueRequest;
import com.ai.novel.dto.request.ChapterCreateRequest;
import com.ai.novel.entity.*;
import com.ai.novel.entity.enums.ChapterStatus;
import com.ai.novel.entity.enums.PlotThreadStatus;
import com.ai.novel.exception.ResourceNotFoundException;
import com.ai.novel.repository.*;
import com.ai.novel.service.ai.AIService;
import com.ai.novel.service.ai.PromptManager;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 章节服务 - 核心AI续写逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final SceneRepository sceneRepository;
    private final CharacterRepository characterRepository;
    private final PlotThreadRepository plotThreadRepository;
    private final WorldSettingRepository worldSettingRepository;
    private final AIService aiService;
    private final PromptManager promptManager;

    /**
     * 创建章节（使用DTO）
     */
    @Transactional
    public Chapter createChapter(ChapterCreateRequest request) {
        Novel novel = novelRepository.findById(request.getNovelId())
                .orElseThrow(() -> new ResourceNotFoundException("小说不存在: " + request.getNovelId()));
        
        Scene scene = null;
        if (request.getSceneId() != null) {
            scene = sceneRepository.findById(request.getSceneId()).orElse(null);
        }
        
        Chapter chapter = Chapter.builder()
                .novel(novel)
                .scene(scene)
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle())
                .content(request.getContent())
                .status(ChapterStatus.DRAFT)
                .build();
        
        chapter.updateWordCount();
        return chapterRepository.save(chapter);
    }

    /**
     * AI续写章节（使用DTO）
     */
    @Transactional
    public Chapter continueChapter(ChapterContinueRequest request) {
        return continueChapter(
                request.getNovelId(),
                request.getDirection(),
                request.getMaxLength(),
                request.getSceneId()
        );
    }

    /**
     * AI续写章节
     */
    @Transactional
    public Chapter continueChapter(Long novelId, String writingDirection, 
                                   Integer targetWordCount, Long sceneId) {
        // 获取小说
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));

        // 获取最大章节号
        Integer maxChapterNum = chapterRepository.findMaxChapterNumber(novelId).orElse(0);
        int nextChapterNum = maxChapterNum + 1;

        // 如果没有提供续写方向,使用默认
        if (writingDirection == null || writingDirection.isEmpty()) {
            writingDirection = "继续故事发展,推进主线剧情";
        }

        // 构建上下文
        String contextInfo = buildContextInfo(novelId, nextChapterNum, sceneId);
        
        // 构建风格信息
        String styleInfo = buildStyleInfo(novel.getWritingStyle());
        
        // 构建场景信息
        String sceneInfo = "";
        Scene scene = null;
        if (sceneId != null) {
            scene = sceneRepository.findById(sceneId).orElse(null);
            if (scene != null) {
                sceneInfo = String.format("场景: %s, 氛围: %s", 
                                        scene.getTitle(), 
                                        scene.getAtmosphere());
            }
        }

        // 构建提示词
        String prompt = promptManager.buildChapterContinuePrompt(
            novel.getTitle(),
            nextChapterNum,
            contextInfo,
            styleInfo,
            writingDirection,
            targetWordCount != null ? targetWordCount : 2000,
            sceneInfo,
            ""
        );

        // 调用AI生成
        log.info("开始AI续写章节 - 小说ID: {}, 章节号: {}", novelId, nextChapterNum);
        String content = aiService.chat(
            "你是一位专业小说作家,擅长保持风格一致性和情节连贯性。",
            prompt,
            0.8,
            6000
        );

        // 创建新章节
        Chapter chapter = Chapter.builder()
                .novel(novel)
                .scene(scene)
                .chapterNumber(nextChapterNum)
                .title("第" + nextChapterNum + "章")
                .content(content)
                .writingDirection(writingDirection)
                .status(ChapterStatus.COMPLETED)
                .build();
        
        chapter.updateWordCount();
        chapter = chapterRepository.save(chapter);

        // 更新场景进度
        if (scene != null) {
            scene.setCurrentChapters(scene.getCurrentChapters() + 1);
            scene.setCurrentWordCount(scene.getCurrentWordCount() + chapter.getWordCount());
            sceneRepository.save(scene);
        }

        log.info("章节续写完成 - 章节ID: {}, 字数: {}", chapter.getId(), chapter.getWordCount());

        // 异步分析章节内容(可选)
        // analyzeChapterAsync(chapter.getId());

        return chapter;
    }

    /**
     * 构建上下文信息
     */
    private String buildContextInfo(Long novelId, int nextChapterNum, Long sceneId) {
        StringBuilder context = new StringBuilder();
        
        // 获取最近章节
        List<Chapter> recentChapters = chapterRepository.findRecentChapters(novelId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());
        
        if (!recentChapters.isEmpty()) {
            context.append("【最近章节摘要】\n");
            for (Chapter ch : recentChapters) {
                context.append(String.format("第%d章: %s\n", 
                                           ch.getChapterNumber(), 
                                           ch.getSummary() != null ? ch.getSummary() : "暂无摘要"));
            }
        }

        // 主要角色状态
        List<com.ai.novel.entity.Character> mainCharacters = characterRepository.findByNovelId(novelId)
                .stream()
                .filter(c -> c.getImportanceLevel().name().equals("MAIN") || 
                           c.getImportanceLevel().name().equals("SECONDARY"))
                .limit(10)
                .collect(Collectors.toList());
        
        if (!mainCharacters.isEmpty()) {
            context.append("\n【主要角色状态】\n");
            for (com.ai.novel.entity.Character character : mainCharacters) {
                context.append(String.format("- %s: %s, 当前状态: %s\n",
                                           character.getName(),
                                           character.getPersonality() != null ? character.getPersonality() : "无描述",
                                           character.getCurrentStatus() != null ? character.getCurrentStatus() : "未知"));
            }
        }

        // 待处理伏笔
        List<PlotThread> pendingThreads = plotThreadRepository.findByNovelIdAndStatus(
                novelId, PlotThreadStatus.PLANTED
        );
        
        if (!pendingThreads.isEmpty()) {
            context.append("\n【待处理伏笔】\n");
            for (PlotThread thread : pendingThreads.stream().limit(5).collect(Collectors.toList())) {
                context.append(String.format("- %s: %s\n", thread.getTitle(), thread.getDescription()));
            }
        }

        return context.toString();
    }

    /**
     * 构建风格信息
     */
    private String buildStyleInfo(Map<String, Object> writingStyle) {
        if (writingStyle == null || writingStyle.isEmpty()) {
            return "暂无风格特征";
        }

        StringBuilder style = new StringBuilder();
        style.append("叙述视角: ").append(writingStyle.getOrDefault("narrative_perspective", "第三人称")).append("\n");
        style.append("语言风格: ").append(writingStyle.getOrDefault("language_style", "平实")).append("\n");
        style.append("节奏特点: ").append(writingStyle.getOrDefault("pacing", "适中")).append("\n");
        style.append("情感基调: ").append(writingStyle.getOrDefault("emotional_tone", "中性"));
        
        return style.toString();
    }

    /**
     * 获取章节详情
     */
    @Transactional(readOnly = true)
    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("章节不存在: " + id));
    }

    /**
     * 分页查询章节列表
     */
    @Transactional(readOnly = true)
    public Page<Chapter> listChapters(Long novelId, Pageable pageable) {
        return chapterRepository.findByNovelId(novelId, pageable);
    }

    /**
     * 获取小说的所有章节
     */
    @Transactional(readOnly = true)
    public List<Chapter> getNovelChapters(Long novelId) {
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
    }

    /**
     * 获取章节详情
     */
    @Transactional(readOnly = true)
    public Chapter getChapter(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("章节不存在: " + id));
    }

    /**
     * 更新章节（使用DTO）
     */
    @Transactional
    public Chapter updateChapter(Long id, ChapterCreateRequest request) {
        Chapter chapter = getChapterById(id);
        
        if (request.getTitle() != null) {
            chapter.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            chapter.setContent(request.getContent());
            chapter.updateWordCount();
        }
        if (request.getSceneId() != null) {
            Scene scene = sceneRepository.findById(request.getSceneId()).orElse(null);
            chapter.setScene(scene);
        }
        
        return chapterRepository.save(chapter);
    }

    /**
     * 更新章节
     */
    @Transactional
    public Chapter updateChapter(Long id, String title, String content, String summary) {
        Chapter chapter = getChapter(id);
        
        if (title != null) {
            chapter.setTitle(title);
        }
        if (content != null) {
            chapter.setContent(content);
            chapter.updateWordCount();
        }
        if (summary != null) {
            chapter.setSummary(summary);
        }
        
        return chapterRepository.save(chapter);
    }

    /**
     * 删除章节
     */
    @Transactional
    public void deleteChapter(Long id) {
        chapterRepository.deleteById(id);
    }
}
