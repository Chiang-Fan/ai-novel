package com.aiwriter.service.ai;

import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import com.aiwriter.service.*;
import com.aiwriter.service.PlotForeshadowingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 提示词构建服务
 * 根据小说的当前进展、上下文、未来方向、角色性格等信息动态构建AI续写提示词
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptBuilderService {
    
    private final NovelRepository novelRepository;
    private final com.aiwriter.repository.ChapterRepository chapterRepository;
    private final com.aiwriter.repository.CharacterRepository characterRepository;
    private final com.aiwriter.repository.SceneRepository sceneRepository;
    private final OutlineRepository outlineRepository;
    private final ContentAnalysisService contentAnalysisService;
    private final com.aiwriter.service.NovelWritingStyleService novelWritingStyleService; // 四维风格画像服务
    private final PlotHookService plotHookService;
    private final PlotForeshadowingService plotForeshadowingService;
    private final PlotConsistencyCheckService consistencyCheckService;
    
    /**
     * 构建完整提示词
     * 包含四维风格画像、上下文、角色信息、场景信息、伏笔等
     */
    public String buildFullPrompt(Long novelId, Integer nextChapterNumber) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
        // 获取上下文信息
        List<Chapter> recentChapters = getRecentChapters(novelId, 3);
        String context = buildContext(recentChapters);
        
        // 获取角色信息
        List<com.aiwriter.entity.Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
        String characterInfo = buildCharacterInfo(characters);
        
        // 获取场景信息
        List<Scene> scenes = sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        String sceneInfo = buildSceneInfo(scenes);
        
        // 获取大纲信息
        List<Outline> outlines = outlineRepository.findByNovelIdOrderBySequenceNumberAsc(novelId);
        String outlineInfo = buildOutlineInfo(outlines, nextChapterNumber);
        
        // 获取四维风格画像
        String styleInfo = buildStyleInfo(novelId);
        
        // 获取当前伏笔信息
        String plotHookInfo = buildPlotHookInfo(novelId);
        
        // 构建完整提示词
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== 小说续写任务 ===\n");
        prompt.append("小说标题: ").append(novel.getTitle()).append("\n");
        prompt.append("小说类型: ").append(novel.getGenre() != null ? novel.getGenre() : "未知").append("\n");
        prompt.append("当前章节: 第").append(nextChapterNumber).append("章\n\n");
        
        // 添加四维风格画像
        if (styleInfo != null && !styleInfo.isEmpty()) {
            prompt.append("=== 风格画像 ===\n");
            prompt.append(styleInfo).append("\n\n");
        }
        
        // 添加上下文
        prompt.append("=== 上下文信息 ===\n");
        prompt.append(context).append("\n\n");
        
        // 添加角色信息
        if (characterInfo != null && !characterInfo.isEmpty()) {
            prompt.append("=== 角色信息 ===\n");
            prompt.append(characterInfo).append("\n\n");
        }
        
        // 添加场景信息
        if (sceneInfo != null && !sceneInfo.isEmpty()) {
            prompt.append("=== 场景信息 ===\n");
            prompt.append(sceneInfo).append("\n\n");
        }
        
        // 添加大纲信息
        if (outlineInfo != null && !outlineInfo.isEmpty()) {
            prompt.append("=== 大纲信息 ===\n");
            prompt.append(outlineInfo).append("\n\n");
        }
        
        // 添加伏笔信息
        if (plotHookInfo != null && !plotHookInfo.isEmpty()) {
            prompt.append("=== 伏笔信息 ===\n");
            prompt.append(plotHookInfo).append("\n\n");
        }
        
        prompt.append("=== 续写要求 ===\n");
        prompt.append("1. 保持与前文一致的写作风格\n");
        prompt.append("2. 人物性格和行为要与设定一致\n");
        prompt.append("3. 情节发展要合理自然，有起伏\n");
        prompt.append("4. 适当呼应和解决已有的伏笔\n");
        prompt.append("5. 为后续情节发展埋下新的伏笔\n");
        prompt.append("6. 语言风格要与小说整体风格保持一致\n");
        prompt.append("7. 注意世界观设定的一致性\n\n");
        
        prompt.append("请根据以上信息，续写第").append(nextChapterNumber).append("章内容：\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建简单提示词（降级方案）
     */
    public String buildSimplePrompt(Long novelId, Integer nextChapterNumber, String direction, Integer targetWordCount) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
        // 获取最近章节作为上下文
        List<Chapter> recentChapters = getRecentChapters(novelId, 2);
        String context = buildSimpleContext(recentChapters);
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("=== 小说续写任务 ===\n");
        prompt.append("小说标题: ").append(novel.getTitle()).append("\n");
        prompt.append("当前章节: 第").append(nextChapterNumber).append("章\n\n");
        
        prompt.append("=== 上下文 ===\n");
        prompt.append(context).append("\n\n");
        
        prompt.append("=== 续写要求 ===\n");
        if (direction != null && !direction.isEmpty()) {
            prompt.append("续写方向: ").append(direction).append("\n");
        }
        if (targetWordCount != null) {
            prompt.append("目标字数: 约").append(targetWordCount).append("字\n");
        }
        prompt.append("1. 保持前文的写作风格和叙事节奏\n");
        prompt.append("2. 人物性格和行为要与设定一致\n");
        prompt.append("3. 情节发展要合理自然\n\n");
        
        prompt.append("请根据以上信息，续写第").append(nextChapterNumber).append("章内容：\n");
        
        return prompt.toString();
    }
    
    /**
     * 获取最近章节
     */
    private List<Chapter> getRecentChapters(Long novelId, int count) {
        List<Chapter> allChapters = chapterRepository.findTopNByNovelId(novelId, count);
        return allChapters.stream()
            .sorted(Comparator.comparing(Chapter::getChapterNumber))
            .collect(Collectors.toList());
    }
    
    /**
     * 构建上下文信息
     */
    private String buildContext(List<Chapter> chapters) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            context.append("第").append(chapter.getChapterNumber()).append("章: ").append(chapter.getTitle()).append("\n");
            // 只取章节内容的前300字作为上下文
            String content = chapter.getContent();
            if (content != null) {
                if (content.length() > 300) {
                    content = content.substring(0, 300) + "...";
                }
                context.append(content).append("\n\n");
            }
        }
        
        return context.toString();
    }
    
    /**
     * 构建简单上下文信息
     */
    private String buildSimpleContext(List<Chapter> chapters) {
        if (chapters.isEmpty()) {
            return "";
        }
        
        Chapter lastChapter = chapters.get(chapters.size() - 1);
        StringBuilder context = new StringBuilder();
        context.append("第").append(lastChapter.getChapterNumber()).append("章: ").append(lastChapter.getTitle()).append("\n");
        
        String content = lastChapter.getContent();
        if (content != null) {
            if (content.length() > 500) {
                content = content.substring(content.length() - 500);
            }
            context.append(content);
        }
        
        return context.toString();
    }
    
    /**
     * 构建角色信息
     */
    private String buildCharacterInfo(List<com.aiwriter.entity.Character> characters) {
        if (characters.isEmpty()) {
            return "";
        }
        
        StringBuilder info = new StringBuilder();
        for (com.aiwriter.entity.Character character : characters) {
            info.append("【").append(character.getName()).append("】\n");
            if (character.getRoleType() != null) {
                info.append("  角色类型: ").append(character.getRoleType()).append("\n");
            }
            if (character.getPersonality() != null) {
                info.append("  性格特征: ").append(character.getPersonality()).append("\n");
            }
            if (character.getBackground() != null) {
                info.append("  背景: ").append(character.getBackground()).append("\n");
            }
            info.append("  重要性: ").append(character.getImportanceLevel() != null ? character.getImportanceLevel() : 5).append("/10\n");
            info.append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * 构建场景信息
     */
    private String buildSceneInfo(List<Scene> scenes) {
        if (scenes.isEmpty()) {
            return "";
        }
        
        StringBuilder info = new StringBuilder();
        for (Scene scene : scenes) {
            info.append("【").append(scene.getName()).append("】\n");
            if (scene.getLocation() != null) {
                info.append("  位置: ").append(scene.getLocation()).append("\n");
            }
            if (scene.getTimePeriod() != null) {
                info.append("  时间: ").append(scene.getTimePeriod()).append("\n");
            }
            if (scene.getDescription() != null) {
                info.append("  描述: ").append(scene.getDescription()).append("\n");
            }
            info.append("  类型: ").append(scene.getSceneType()).append("\n");
            info.append("  重要性: ").append(scene.getImportanceScore() != null ? scene.getImportanceScore() : 5).append("/10\n");
            info.append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * 构建大纲信息
     */
    private String buildOutlineInfo(List<Outline> outlines, Integer nextChapterNumber) {
        if (outlines.isEmpty()) {
            return "";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("小说大纲:\n");
        
        for (Outline outline : outlines) {
            info.append("  ").append(outline.getSequenceNumber()).append(". ").append(outline.getTitle()).append("\n");
            if (outline.getSummary() != null) {
                info.append("     概要: ").append(outline.getSummary()).append("\n");
            }
            if (outline.getStatus() != null) {
                info.append("     状态: ").append(outline.getStatus()).append("\n");
            }
            if (outline.getChapterId() != null) {
                info.append("     已关联章节: ").append(outline.getChapterId()).append("\n");
            }
            info.append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * 构建风格信息（四维风格画像）
     */
    private String buildStyleInfo(Long novelId) {
        try {
            // 尝试获取小说的四维风格画像
            String styleProfile = novelWritingStyleService.getNovelStyleProfile(novelId);
            if (styleProfile != null && !styleProfile.isEmpty()) {
                return styleProfile;
            }
        } catch (Exception e) {
            log.warn("获取小说风格画像失败: {}", e.getMessage());
        }
        
        return "";
    }
    
    /**
     * 构建伏笔信息
     */
    private String buildPlotHookInfo(Long novelId) {
        try {
            StringBuilder info = new StringBuilder();
            
            // 获取原有的未解决的伏笔
            List<com.aiwriter.dto.PlotHookDto> pendingHooks = plotHookService.getPendingHooks(novelId);
            if (pendingHooks != null && !pendingHooks.isEmpty()) {
                info.append("当前待解决的伏笔（旧系统）:\n");
                for (com.aiwriter.dto.PlotHookDto hook : pendingHooks) {
                    info.append("  - ").append(hook.getTitle()).append(": ").append(hook.getDescription()).append("\n");
                    info.append("    优先级: ").append(hook.getPriority()).append("/10\n");
                    if (hook.getExpectedChapter() != null) {
                        info.append("    预计解决章节: ").append(hook.getExpectedChapter()).append("\n");
                    }
                    if (hook.getType() != null) {
                        info.append("    类型: ").append(hook.getType()).append("\n");
                    }
                }
                info.append("\n");
            }
            
            // 获取新的伏笔管理系统中的未解决伏笔
            List<PlotForeshadowing> pendingForeshadowings = plotForeshadowingService.getPlotForeshadowings(novelId).stream()
                .filter(f -> "PENDING".equals(f.getStatus()))
                .collect(Collectors.toList());
                
            if (!pendingForeshadowings.isEmpty()) {
                info.append("当前待解决的伏笔（新系统）:\n");
                for (PlotForeshadowing foreshadowing : pendingForeshadowings) {
                    info.append("  - ").append(foreshadowing.getTitle()).append(": ").append(foreshadowing.getDescription()).append("\n");
                    info.append("    优先级: ").append(foreshadowing.getPriority()).append("/10\n");
                    if (foreshadowing.getExpectedChapter() != null) {
                        info.append("    预计解决章节: ").append(foreshadowing.getExpectedChapter()).append("\n");
                    }
                    if (foreshadowing.getType() != null) {
                        info.append("    类型: ").append(foreshadowing.getType()).append("\n");
                    }
                    info.append("    种植章节: ").append(foreshadowing.getPlantedInChapter()).append("\n");
                    if (foreshadowing.getContentReference() != null) {
                        info.append("    内容引用: ").append(foreshadowing.getContentReference()).append("\n");
                    }
                }
                info.append("\n");
            }
            
            // 获取已触发但未解决的伏笔
            List<PlotForeshadowing> triggeredForeshadowings = plotForeshadowingService.getPlotForeshadowings(novelId).stream()
                .filter(f -> "TRIGGERED".equals(f.getStatus()))
                .collect(Collectors.toList());
                
            if (!triggeredForeshadowings.isEmpty()) {
                info.append("已触发待解决的伏笔（新系统）:\n");
                for (PlotForeshadowing foreshadowing : triggeredForeshadowings) {
                    info.append("  - ").append(foreshadowing.getTitle()).append(": ").append(foreshadowing.getDescription()).append("\n");
                    if (foreshadowing.getTriggeredInChapter() != null) {
                        info.append("    触发章节: ").append(foreshadowing.getTriggeredInChapter()).append("\n");
                    }
                    if (foreshadowing.getContentReference() != null) {
                        info.append("    内容引用: ").append(foreshadowing.getContentReference()).append("\n");
                    }
                    if (foreshadowing.getNotes() != null) {
                        info.append("    备注: ").append(foreshadowing.getNotes()).append("\n");
                    }
                }
                info.append("\n");
            }
            
            // 如果没有伏笔信息，返回空字符串
            if (info.length() == 0) {
                return "";
            }
            
            return info.toString();
        } catch (Exception e) {
            log.warn("获取伏笔信息失败: {}", e.getMessage());
        }
        
        return "";
    }
}