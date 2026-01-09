package com.aiwriter.service;

import com.aiwriter.dto.WritingStatusInfo;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 写作状态服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WritingStatusService {
    
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final WritingStyleRepository writingStyleRepository;
    private final com.aiwriter.repository.CharacterRepository characterRepository;
    private final SceneRepository sceneRepository;
    private final PlotThreadRepository plotThreadRepository;
    private final ContinuationSuggestionRepository suggestionRepository;
    
    /**
     * 获取当前写作状态信息
     */
    public WritingStatusInfo getWritingStatus(Long novelId) {
        WritingStatusInfo statusInfo = new WritingStatusInfo();
        
        try {
            // 获取写作风格信息
            statusInfo.setWritingStyle(getWritingStyleInfo(novelId));
            
            // 获取核心角色信息
            statusInfo.setCoreCharacters(getCoreCharacters(novelId));
            
            // 获取近期出现的角色
            statusInfo.setRecentCharacters(getRecentCharacters(novelId));
            
            // 获取当前进度信息
            statusInfo.setProgress(getProgressInfo(novelId));
            
            // 获取建议续写方向
            statusInfo.setContinuationSuggestions(getContinuationSuggestions(novelId));
            
            // 获取当前场景信息
            statusInfo.setCurrentScene(getCurrentScene(novelId));
            
            // 获取情节线索信息
            statusInfo.setPlotThreads(getPlotThreads(novelId));
            
        } catch (Exception e) {
            log.error("获取写作状态失败: novelId={}", novelId, e);
            throw e;
        }
        
        return statusInfo;
    }
    
    private WritingStatusInfo.WritingStyleInfo getWritingStyleInfo(Long novelId) {
        WritingStyle style = writingStyleRepository.findByNovelId(novelId);
        if (style == null) {
            return null;
        }
        
        WritingStatusInfo.WritingStyleInfo styleInfo = new WritingStatusInfo.WritingStyleInfo();
        styleInfo.setTone(style.getTone());
        // 使用现有字段映射到对应的属性
        // 设置句式特点为sentenceLength字段
        styleInfo.setSentenceStyle(style.getSentenceLength());
        styleInfo.setDescription(style.getDescription());
        
        // 解析平均句长
        if (style.getSentenceLength() != null) {
            try {
                // 从sentenceLength字段中提取数字部分
                String numericPart = style.getSentenceLength().replaceAll("[^0-9]", "");
                if (!numericPart.isEmpty()) {
                    styleInfo.setAvgSentenceLength(Integer.parseInt(numericPart));
                } else {
                    styleInfo.setAvgSentenceLength(null);
                }
            } catch (NumberFormatException e) {
                log.warn("解析句长失败: {}", style.getSentenceLength());
                styleInfo.setAvgSentenceLength(null);
            }
        } else {
            styleInfo.setAvgSentenceLength(null);
        }
        // 将languageComplexity映射到descriptionDensity字段
        styleInfo.setDescriptionDensity(style.getLanguageComplexity());
        
        // 使用styleFeatures作为关键词
        if (style.getStyleFeatures() != null) {
            try {
                // 将styleFeatures按逗号分割作为关键词列表
                String[] features = style.getStyleFeatures().split(",");
                styleInfo.setKeywords(List.of(features));
            } catch (Exception e) {
                log.warn("解析风格特征失败: {}", e.getMessage());
                styleInfo.setKeywords(List.of(style.getStyleFeatures()));
            }
        } else {
            styleInfo.setKeywords(List.of());
        }
        
        return styleInfo;
    }
    
    private List<WritingStatusInfo.CharacterInfo> getCoreCharacters(Long novelId) {
        // 获取重要性高的角色（如主角、配角）
        List<com.aiwriter.entity.Character> allCharacters = characterRepository
            .findByNovelIdOrderByRoleTypeAsc(novelId);
        
        // 过滤重要性高的角色
        List<com.aiwriter.entity.Character> characters = allCharacters.stream()
            .filter(c -> "PRIMARY".equals(c.getImportanceLevel()) || 
                         "SECONDARY".equals(c.getImportanceLevel()) ||
                         "PROTAGONIST".equals(c.getRoleType()) ||
                         "ANTAGONIST".equals(c.getRoleType()))
            .collect(Collectors.toList());
        
        return characters.stream()
            .map(this::convertToCharacterInfo)
            .collect(Collectors.toList());
    }
    
    private List<WritingStatusInfo.CharacterInfo> getRecentCharacters(Long novelId) {
        // 获取最近章节中出现的角色（这里简化为获取所有角色的最新信息）
        List<com.aiwriter.entity.Character> characters = characterRepository
            .findByNovelIdOrderByRoleTypeAsc(novelId);
        
        // 只取前几个最近更新的角色
        return characters.stream()
            .limit(10) // 只取最近的10个角色
            .map(this::convertToCharacterInfo)
            .collect(Collectors.toList());
    }
    
    private WritingStatusInfo.CharacterInfo convertToCharacterInfo(com.aiwriter.entity.Character character) {
        WritingStatusInfo.CharacterInfo charInfo = new WritingStatusInfo.CharacterInfo();
        charInfo.setId(character.getId());
        charInfo.setName(character.getName());
        charInfo.setRoleType(character.getRoleType());
        charInfo.setPersonality(character.getPersonality());
        charInfo.setImportanceLevel(character.getImportanceLevel() != null ? character.getImportanceLevel().toString() : null);
        charInfo.setIsGlobalProtagonist(character.getIsGlobalProtagonist());
        charInfo.setAppearance(character.getAppearance());
        charInfo.setAbilities(character.getAbilities());
        charInfo.setMotivation(character.getMotivation());
        return charInfo;
    }
    
    private WritingStatusInfo.ProgressInfo getProgressInfo(Long novelId) {
        Novel novel = novelRepository.findById(novelId)
            .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        int currentChapter = chapters.isEmpty() ? 0 : chapters.get(chapters.size() - 1).getChapterNumber();
        int totalChapters = novel.getTotalChapters();
        int currentWordCount = chapters.stream().mapToInt(Chapter::getWordCount).sum();
        int targetWordCount = novel.getTotalWords() != null ? novel.getTotalWords() : 0;
        
        WritingStatusInfo.ProgressInfo progress = new WritingStatusInfo.ProgressInfo();
        progress.setCurrentChapter(currentChapter);
        progress.setTotalChapters(totalChapters);
        progress.setCurrentWordCount(currentWordCount);
        progress.setTargetWordCount(targetWordCount);
        progress.setStatus(novel.getStatus());
        
        if (targetWordCount > 0) {
            progress.setProgressPercentage((double) currentWordCount / targetWordCount * 100);
        } else {
            progress.setProgressPercentage(0.0);
        }
        
        return progress;
    }
    
    private List<WritingStatusInfo.SuggestionInfo> getContinuationSuggestions(Long novelId) {
        // 获取未采纳的续写建议
        List<ContinuationSuggestion> suggestions = suggestionRepository
            .findByNovelIdAndIsAdoptedFalseOrderByPriorityDesc(novelId);
        
        return suggestions.stream()
            .map(this::convertToSuggestionInfo)
            .collect(Collectors.toList());
    }
    
    private WritingStatusInfo.SuggestionInfo convertToSuggestionInfo(ContinuationSuggestion suggestion) {
        WritingStatusInfo.SuggestionInfo suggestionInfo = new WritingStatusInfo.SuggestionInfo();
        suggestionInfo.setId(suggestion.getId());
        suggestionInfo.setTitle(suggestion.getTitle());
        suggestionInfo.setDescription(suggestion.getDescription());
        suggestionInfo.setType(suggestion.getPlotDirection());
        suggestionInfo.setPriority(suggestion.getPriority());
        // 使用priority作为相关性评分，转换为0-1之间的值
        suggestionInfo.setRelevanceScore(suggestion.getPriority() != null ? (double) suggestion.getPriority() / 10.0 : 0.5);
        return suggestionInfo;
    }
    
    private WritingStatusInfo.SceneInfo getCurrentScene(Long novelId) {
        // 获取最新的场景（按创建时间排序）
        List<Scene> scenes = sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        if (scenes.isEmpty()) {
            return null;
        }
        
        Scene scene = scenes.get(0); // 最新的场景
        WritingStatusInfo.SceneInfo sceneInfo = new WritingStatusInfo.SceneInfo();
        sceneInfo.setId(scene.getId());
        sceneInfo.setName(scene.getName());
        sceneInfo.setLocation(scene.getLocation());
        sceneInfo.setSceneType(scene.getSceneType());
        sceneInfo.setDescription(scene.getDescription());
        sceneInfo.setAtmosphere(scene.getAtmosphere());
        
        // 解析涉及角色（假设是JSON格式）
        if (scene.getInvolvedCharacters() != null) {
            try {
                sceneInfo.setInvolvedCharacters(List.of(scene.getInvolvedCharacters()));
            } catch (Exception e) {
                log.warn("解析场景涉及角色失败: {}", e.getMessage());
                sceneInfo.setInvolvedCharacters(List.of());
            }
        } else {
            sceneInfo.setInvolvedCharacters(List.of());
        }
        
        return sceneInfo;
    }
    
    private List<WritingStatusInfo.PlotThreadInfo> getPlotThreads(Long novelId) {
        List<PlotThread> threads = plotThreadRepository.findByNovelId(novelId);
        
        return threads.stream()
            .map(this::convertToPlotThreadInfo)
            .collect(Collectors.toList());
    }
    
    private WritingStatusInfo.PlotThreadInfo convertToPlotThreadInfo(PlotThread thread) {
        WritingStatusInfo.PlotThreadInfo threadInfo = new WritingStatusInfo.PlotThreadInfo();
        threadInfo.setId(thread.getId());
        threadInfo.setName(thread.getTitle());  // 使用title作为name
        threadInfo.setType(thread.getImportance());  // 使用importance作为type
        threadInfo.setDescription(thread.getDescription());
        threadInfo.setStatus(thread.getStatus());
        
        // 解析相关角色（从relatedCharacterIds字段，JSON格式）
        if (thread.getRelatedCharacterIds() != null) {
            try {
                // 将JSON格式的字符ID列表转换为字符串列表
                threadInfo.setInvolvedCharacters(List.of(thread.getRelatedCharacterIds()));
            } catch (Exception e) {
                log.warn("解析情节线索涉及角色失败: {}", e.getMessage());
                threadInfo.setInvolvedCharacters(List.of());
            }
        } else {
            threadInfo.setInvolvedCharacters(List.of());
        }
        
        // KeyEvents不存在，使用notes作为替代
        if (thread.getNotes() != null) {
            try {
                // 将notes作为单个事件
                threadInfo.setKeyEvents(List.of(thread.getNotes()));
            } catch (Exception e) {
                log.warn("解析情节线索关键事件失败: {}", e.getMessage());
                threadInfo.setKeyEvents(List.of());
            }
        } else {
            threadInfo.setKeyEvents(List.of());
        }
        
        return threadInfo;
    }
}