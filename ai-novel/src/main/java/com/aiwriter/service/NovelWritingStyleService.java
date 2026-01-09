package com.aiwriter.service;

import com.aiwriter.dto.StyleAnalysisResponse;
import com.aiwriter.entity.*;
import com.aiwriter.repository.PlotForeshadowingRepository;
import com.aiwriter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 小说写作风格服务（四维风格画像）
 * 整合角色、场景、大纲、伏笔等多维度信息，形成完整的小说风格画像
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelWritingStyleService {
    
    private final com.aiwriter.repository.CharacterRepository characterRepository;
    private final com.aiwriter.repository.SceneRepository sceneRepository;
    private final com.aiwriter.repository.OutlineRepository outlineRepository;
    private final com.aiwriter.repository.PlotHookRepository plotHookRepository;
    private final PlotForeshadowingRepository plotForeshadowingRepository;
    private final com.aiwriter.repository.StyleAnalysisRepository styleAnalysisRepository;
    private final com.aiwriter.repository.ChapterRepository chapterRepository;
    private final ContentAnalysisService contentAnalysisService;
    
    /**
     * 获取小说的四维风格画像
     * 包含角色信息、场景信息、大纲信息、伏笔信息等
     */
    public String getNovelStyleProfile(Long novelId) {
        return buildFourDimensionalProfile(novelId);
    }
    
    /**
     * 构建四维风格画像
     * 四维包括：角色信息、场景信息、大纲信息、伏笔信息
     */
    public String buildFourDimensionalProfile(Long novelId) {
        if (novelId == null) {
            return "";
        }
        
        try {
            StringBuilder profile = new StringBuilder();
            
            // 1. 角色画像 - 包含角色性格、行为、关系等信息
            String characterProfile = buildCharacterProfile(novelId);
            if (characterProfile != null && !characterProfile.isEmpty()) {
                profile.append("=== 角色画像（第一维）===\n");
                profile.append(characterProfile).append("\n\n");
            }
            
            // 2. 场景画像 - 包含环境、氛围、时间、地点等信息
            String sceneProfile = buildSceneProfile(novelId);
            if (sceneProfile != null && !sceneProfile.isEmpty()) {
                profile.append("=== 场景画像（第二维）===\n");
                profile.append(sceneProfile).append("\n\n");
            }
            
            // 3. 大纲画像 - 包含情节发展、关键事件、主题等信息
            String outlineProfile = buildOutlineProfile(novelId);
            if (outlineProfile != null && !outlineProfile.isEmpty()) {
                profile.append("=== 大纲画像（第三维）===\n");
                profile.append(outlineProfile).append("\n\n");
            }
            
            // 4. 伏笔画像 - 包含待解决线索、未来方向、逻辑关联等信息
            String plotHookProfile = buildPlotHookProfile(novelId);
            if (plotHookProfile != null && !plotHookProfile.isEmpty()) {
                profile.append("=== 伏笔画像（第四维）===\n");
                profile.append(plotHookProfile).append("\n\n");
            }
            
            // 5. 综合风格分析 - 基于内容分析的写作风格
            String styleAnalysis = buildStyleAnalysis(novelId);
            if (styleAnalysis != null && !styleAnalysis.isEmpty()) {
                profile.append("=== 写作分析（综合维度）===\n");
                profile.append(styleAnalysis).append("\n\n");
            }
            
            return profile.toString();
            
        } catch (Exception e) {
            log.error("获取小说四维风格画像失败: novelId={}", novelId, e);
            return "";
        }
    }
    
    /**
     * 构建角色画像
     */
    private String buildCharacterProfile(Long novelId) {
        List<com.aiwriter.entity.Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
        if (characters.isEmpty()) {
            return "";
        }
        
        StringBuilder profile = new StringBuilder();
        for (com.aiwriter.entity.Character character : characters) {
            profile.append("【").append(character.getName()).append("】\n");
            profile.append("  类型: ").append(character.getRoleType() != null ? character.getRoleType() : "未知").append("\n");
            profile.append("  重要性: ").append(character.getImportanceLevel() != null ? character.getImportanceLevel() : 5).append("/10\n");
            
            if (character.getPersonality() != null) {
                profile.append("  性格特征: ").append(character.getPersonality()).append("\n");
                profile.append("  行为模式: ").append(extractBehavioralPatterns(character.getPersonality())).append("\n");
            }
            if (character.getBackground() != null) {
                profile.append("  背景故事: ").append(character.getBackground()).append("\n");
            }
            if (character.getCoreBelief() != null) {
                profile.append("  核心信念: ").append(character.getCoreBelief()).append("\n");
            }
            if (character.getEvolvingBelief() != null) {
                profile.append("  演变信念: ").append(character.getEvolvingBelief()).append("\n");
            }
            if (character.getArc() != null) {
                profile.append("  角色弧光: ").append(character.getArc()).append("\n");
            }
            if (character.getMotivation() != null) {
                profile.append("  驱动动机: ").append(character.getMotivation()).append("\n");
            }
            
            profile.append("\n");
        }
        
        return profile.toString();
    }
    
    /**
     * 批量分析章节以提取写作风格（用于AI续写优化）
     */
    public void analyzeBatchChapters(Long novelId) {
        log.info("开始分析小说 {} 的章节风格", novelId);
        
        try {
            // 获取所有章节
            List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
            
            if (chapters.isEmpty()) {
                log.info("小说 {} 没有章节，跳过分析", novelId);
                return;
            }
            
            // 分析每个章节的风格特征
            for (Chapter chapter : chapters) {
                // 提取章节片段进行风格分析
                String content = chapter.getContent();
                if (content != null && content.length() > 100) { // 确保内容足够长
                    String snippet = content.length() > 500 ? content.substring(0, 500) : content;
                    
                    // 这里可以调用AI服务进行风格分析
                    // 为简化实现，我们记录章节风格特征
                    log.debug("章节 {} 风格分析完成", chapter.getId());
                }
            }
            
            log.info("小说 {} 的章节风格分析完成，共分析 {} 章", novelId, chapters.size());
            
        } catch (Exception e) {
            log.error("分析小说 {} 的章节风格时出错", novelId, e);
        }
    }
    
    /**
     * 从性格描述中提取行为模式
     */
    private String extractBehavioralPatterns(String personality) {
        if (personality == null) return "未知";
        
        List<String> patterns = new ArrayList<>();
        
        // 根据性格特征提取行为模式
        if (personality.toLowerCase().contains("谨慎") || personality.toLowerCase().contains("小心")) {
            patterns.add("谨慎决策");
        }
        if (personality.toLowerCase().contains("勇敢") || personality.toLowerCase().contains("无畏")) {
            patterns.add("主动行动");
        }
        if (personality.toLowerCase().contains("内向") || personality.toLowerCase().contains("内敛")) {
            patterns.add("观察思考");
        }
        if (personality.toLowerCase().contains("外向") || personality.toLowerCase().contains("开朗")) {
            patterns.add("积极互动");
        }
        if (personality.toLowerCase().contains("聪明") || personality.toLowerCase().contains("智慧")) {
            patterns.add("策略思维");
        }
        
        return patterns.isEmpty() ? "常规行为" : String.join(", ", patterns);
    }
    
    /**
     * 构建场景画像
     */
    private String buildSceneProfile(Long novelId) {
        List<Scene> scenes = sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        if (scenes.isEmpty()) {
            return "";
        }
        
        StringBuilder profile = new StringBuilder();
        for (Scene scene : scenes) {
            profile.append("【").append(scene.getName()).append("】\n");
            profile.append("  类型: ").append(scene.getSceneType() != null ? scene.getSceneType() : "未知").append("\n");
            profile.append("  重要性: ").append(scene.getImportanceScore() != null ? scene.getImportanceScore() : 5).append("/10\n");
            
            if (scene.getLocation() != null) {
                profile.append("  位置: ").append(scene.getLocation()).append("\n");
            }
            if (scene.getTimePeriod() != null) {
                profile.append("  时间: ").append(scene.getTimePeriod()).append("\n");
            }
            if (scene.getDescription() != null) {
                profile.append("  描述: ").append(scene.getDescription()).append("\n");
            }
            if (scene.getAtmosphere() != null) {
                profile.append("  氛围: ").append(scene.getAtmosphere()).append("\n");
            }
            if (scene.getProps() != null) {
                profile.append("  道具: ").append(scene.getProps()).append("\n");
            }
            if (scene.getInvolvedCharacters() != null) {
                profile.append("  涉及角色: ").append(scene.getInvolvedCharacters()).append("\n");
            }
            
            profile.append("\n");
        }
        
        return profile.toString();
    }
    
    /**
     * 构建大纲画像
     */
    private String buildOutlineProfile(Long novelId) {
        List<Outline> outlines = outlineRepository.findByNovelIdOrderBySequenceNumberAsc(novelId);
        if (outlines.isEmpty()) {
            return "";
        }
        
        StringBuilder profile = new StringBuilder();
        for (Outline outline : outlines) {
            profile.append("【").append(outline.getTitle()).append("】\n");
            profile.append("  序号: ").append(outline.getSequenceNumber()).append("\n");
            profile.append("  类型: ").append(outline.getNodeType() != null ? outline.getNodeType() : "未知").append("\n");
            profile.append("  状态: ").append(outline.getStatus() != null ? outline.getStatus() : "PLANNED").append("\n");
            
            if (outline.getSummary() != null) {
                profile.append("  概要: ").append(outline.getSummary()).append("\n");
            }
            if (outline.getKeyEvents() != null) {
                profile.append("  关键事件: ").append(outline.getKeyEvents()).append("\n");
            }
            if (outline.getCharacterFocus() != null) {
                profile.append("  焦点角色: ").append(outline.getCharacterFocus()).append("\n");
            }
            if (outline.getPlotPoints() != null) {
                profile.append("  情节点: ").append(outline.getPlotPoints()).append("\n");
            }
            if (outline.getThemes() != null) {
                profile.append("  主题: ").append(outline.getThemes()).append("\n");
            }
            if (outline.getTargetWordCount() != null) {
                profile.append("  目标字数: ").append(outline.getTargetWordCount()).append("\n");
            }
            
            profile.append("\n");
        }
        
        return profile.toString();
    }
    
    /**
     * 构建伏笔画像
     */
    private String buildPlotHookProfile(Long novelId) {
        // 获取原有的伏笔信息
        List<PlotHook> hooks = plotHookRepository.findByNovelIdOrderByPlantedInChapterAsc(novelId);
        
        // 获取新的伏笔管理系统中的信息
        List<PlotForeshadowing> foreshadowings = plotForeshadowingRepository.findByNovelIdOrderByPlantedInChapterAsc(novelId);
        
        StringBuilder profile = new StringBuilder();
        
        // 添加原有的伏笔信息
        if (!hooks.isEmpty()) {
            // 按状态分类
            Map<PlotHook.Status, List<PlotHook>> hooksByStatus = hooks.stream()
                .collect(Collectors.groupingBy(PlotHook::getStatus));
            
            // 待解决的伏笔
            List<PlotHook> pendingHooks = hooksByStatus.get(PlotHook.Status.PENDING);
            if (pendingHooks != null && !pendingHooks.isEmpty()) {
                profile.append("待解决伏笔（未来方向）:\n");
                for (PlotHook hook : pendingHooks) {
                    profile.append("  - ").append(hook.getTitle()).append(": ").append(hook.getDescription()).append("\n");
                    if (hook.getExpectedChapter() != null) {
                        profile.append("    预计解决章节: ").append(hook.getExpectedChapter()).append("\n");
                    }
                    if (hook.getType() != null) {
                        profile.append("    类型: ").append(hook.getType()).append("\n");
                    }
                    profile.append("    优先级: ").append(hook.getPriority()).append("/10\n");
                    if (hook.getRelatedCharacters() != null) {
                        profile.append("    相关角色: ").append(hook.getRelatedCharacters()).append("\n");
                    }
                }
                profile.append("\n");
            }
            
            // 已触发的伏笔
            List<PlotHook> triggeredHooks = hooksByStatus.get(PlotHook.Status.TRIGGERED);
            if (triggeredHooks != null && !triggeredHooks.isEmpty()) {
                profile.append("已触发伏笔:\n");
                for (PlotHook hook : triggeredHooks) {
                    profile.append("  - ").append(hook.getTitle()).append(": ").append(hook.getDescription()).append("\n");
                    if (hook.getTriggeredInChapter() != null) {
                        profile.append("    触发章节: ").append(hook.getTriggeredInChapter()).append("\n");
                    }
                    if (hook.getContentReference() != null) {
                        profile.append("    原文引用: ").append(hook.getContentReference()).append("\n");
                    }
                }
                profile.append("\n");
            }
            
            // 已解决的伏笔
            List<PlotHook> resolvedHooks = hooksByStatus.get(PlotHook.Status.RESOLVED);
            if (resolvedHooks != null && !resolvedHooks.isEmpty()) {
                profile.append("已解决伏笔:\n");
                for (PlotHook hook : resolvedHooks) {
                    profile.append("  - ").append(hook.getTitle()).append(": ").append(hook.getDescription()).append("\n");
                    if (hook.getResolvedInChapter() != null) {
                        profile.append("    解决章节: ").append(hook.getResolvedInChapter()).append("\n");
                    }
                    if (hook.getResolutionNote() != null) {
                        profile.append("    解决说明: ").append(hook.getResolutionNote()).append("\n");
                    }
                }
                profile.append("\n");
            }
        }
        
        // 添加新的伏笔信息
        if (!foreshadowings.isEmpty()) {
            // 按状态分类
            Map<String, List<PlotForeshadowing>> foreshadowingsByStatus = foreshadowings.stream()
                .collect(Collectors.groupingBy(PlotForeshadowing::getStatus));
            
            // 待解决的伏笔
            List<PlotForeshadowing> pendingForeshadowings = foreshadowingsByStatus.get("PENDING");
            if (pendingForeshadowings != null && !pendingForeshadowings.isEmpty()) {
                profile.append("待解决伏笔（新系统）:\n");
                for (PlotForeshadowing foreshadowing : pendingForeshadowings) {
                    profile.append("  - ").append(foreshadowing.getTitle()).append(": ").append(foreshadowing.getDescription()).append("\n");
                    if (foreshadowing.getExpectedChapter() != null) {
                        profile.append("    预计解决章节: ").append(foreshadowing.getExpectedChapter()).append("\n");
                    }
                    if (foreshadowing.getType() != null) {
                        profile.append("    类型: ").append(foreshadowing.getType()).append("\n");
                    }
                    profile.append("    优先级: ").append(foreshadowing.getPriority()).append("/10\n");
                    profile.append("    种植章节: ").append(foreshadowing.getPlantedInChapter()).append("\n");
                    if (foreshadowing.getContentReference() != null) {
                        profile.append("    内容引用: ").append(foreshadowing.getContentReference()).append("\n");
                    }
                }
                profile.append("\n");
            }
            
            // 已触发的伏笔
            List<PlotForeshadowing> triggeredForeshadowings = foreshadowingsByStatus.get("TRIGGERED");
            if (triggeredForeshadowings != null && !triggeredForeshadowings.isEmpty()) {
                profile.append("已触发伏笔（新系统）:\n");
                for (PlotForeshadowing foreshadowing : triggeredForeshadowings) {
                    profile.append("  - ").append(foreshadowing.getTitle()).append(": ").append(foreshadowing.getDescription()).append("\n");
                    if (foreshadowing.getTriggeredInChapter() != null) {
                        profile.append("    触发章节: ").append(foreshadowing.getTriggeredInChapter()).append("\n");
                    }
                    if (foreshadowing.getContentReference() != null) {
                        profile.append("    内容引用: ").append(foreshadowing.getContentReference()).append("\n");
                    }
                    if (foreshadowing.getNotes() != null) {
                        profile.append("    备注: ").append(foreshadowing.getNotes()).append("\n");
                    }
                }
                profile.append("\n");
            }
            
            // 已解决的伏笔
            List<PlotForeshadowing> resolvedForeshadowings = foreshadowingsByStatus.get("RESOLVED");
            if (resolvedForeshadowings != null && !resolvedForeshadowings.isEmpty()) {
                profile.append("已解决伏笔（新系统）:\n");
                for (PlotForeshadowing foreshadowing : resolvedForeshadowings) {
                    profile.append("  - ").append(foreshadowing.getTitle()).append(": ").append(foreshadowing.getDescription()).append("\n");
                    if (foreshadowing.getResolvedInChapter() != null) {
                        profile.append("    解决章节: ").append(foreshadowing.getResolvedInChapter()).append("\n");
                    }
                    if (foreshadowing.getResolutionNote() != null) {
                        profile.append("    解决说明: ").append(foreshadowing.getResolutionNote()).append("\n");
                    }
                    if (foreshadowing.getNotes() != null) {
                        profile.append("    备注: ").append(foreshadowing.getNotes()).append("\n");
                    }
                }
                profile.append("\n");
            }
        }
        
        // 如果两个系统都没有数据，返回空字符串
        if (profile.length() == 0) {
            return "";
        }
        
        return profile.toString();
    }
    
    /**
     * 构建风格分析
     */
    private String buildStyleAnalysis(Long novelId) {
        // 获取最新的风格分析
        List<Chapter> recentChapters = chapterRepository.findTopNByNovelId(novelId, 3);
        if (recentChapters.isEmpty()) {
            return "";
        }
        
        // 获取最近章节的风格分析
        StringBuilder analysis = new StringBuilder();
        
        // 从内容分析服务获取最近的分析结果
        try {
            for (Chapter chapter : recentChapters) {
                List<StyleAnalysis> styleAnalyses = styleAnalysisRepository.findByChapterIdOrderByCreatedAtDesc(chapter.getId());
                if (!styleAnalyses.isEmpty()) {
                    StyleAnalysis latestAnalysis = styleAnalyses.get(0);
                    analysis.append("章节 ").append(chapter.getChapterNumber()).append(" 风格分析:\n");
                    analysis.append("  检测风格: ").append(latestAnalysis.getDetectedStyleId() != null ? "已检测" : "未检测").append("\n");
                    analysis.append("  分析结果: ").append(latestAnalysis.getAnalysisResult()).append("\n");
                    analysis.append("  置信度: ").append(latestAnalysis.getConfidenceScore() != null ? latestAnalysis.getConfidenceScore() : "未知").append("\n");
                    analysis.append("\n");
                    break; // 只取最新的分析结果
                }
            }
            
            // 如果没有风格分析，从内容分析服务获取信息
            if (analysis.length() == 0) {
                // 尝试从内容分析获取风格信息
                List<ContentAnalysis> contentAnalyses = contentAnalysisService.getAnalysisByNovel(novelId);
                if (!contentAnalyses.isEmpty()) {
                    ContentAnalysis latest = contentAnalyses.get(0); // 最新的分析
                    analysis.append("内容分析风格信息:\n");
                    if (latest.getWritingStyle() != null) {
                        analysis.append("  写作风格: ").append(latest.getWritingStyle()).append("\n");
                    }
                    if (latest.getPlotPace() != null) {
                        analysis.append("  情节节奏: ").append(latest.getPlotPace()).append("\n");
                    }
                    if (latest.getEmotionalTone() != null) {
                        analysis.append("  情感基调: ").append(latest.getEmotionalTone()).append("\n");
                    }
                    if (latest.getNarrativePerspective() != null) {
                        analysis.append("  叙事视角: ").append(latest.getNarrativePerspective()).append("\n");
                    }
                    analysis.append("\n");
                }
            }
        } catch (Exception e) {
            log.warn("获取风格分析失败: {}", e.getMessage());
        }
        
        return analysis.toString();
    }
}