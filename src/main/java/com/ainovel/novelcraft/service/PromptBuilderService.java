package com.ainovel.novelcraft.service;

import com.ainovel.novelcraft.entity.Chapter;
import com.ainovel.novelcraft.entity.Novel;
import com.ainovel.novelcraft.entity.Character;
import com.ainovel.novelcraft.entity.WritingStyle;
import com.ainovel.novelcraft.entity.PlotHook;
import com.ainovel.novelcraft.entity.PlotHook.Status;
import com.ainovel.novelcraft.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PromptBuilderService {
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private CharacterRepository characterRepository;
    
    @Autowired
    private WritingStyleRepository writingStyleRepository;
    
    @Autowired
    private PlotHookRepository plotHookRepository;
    
    @Value("${ai.model.temperature:0.7}")
    private Double temperature;
    
    public String buildChapterGenerationPrompt(Long novelId, Integer chapterNumber) {
        return buildChapterGenerationPrompt(novelId, chapterNumber, null, null, null, null);
    }
    
    public String buildChapterGenerationPrompt(Long novelId, Integer chapterNumber, String direction, String[] bannedElements, String mood, Boolean overrideHighStakes) {
        // 获取小说信息
        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (!novelOpt.isPresent()) {
            throw new IllegalArgumentException("Novel not found with id: " + novelId);
        }
        Novel novel = novelOpt.get();
        
        // 获取写作风格
        WritingStyle writingStyle = writingStyleRepository.findByNovelId(novelId);
        
        // 获取前一章内容作为上下文
        String previousSummary = "";
        if (chapterNumber > 1) {
            Optional<Chapter> previousChapterOpt = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber - 1);
            if (previousChapterOpt.isPresent()) {
                previousSummary = previousChapterOpt.get().getSummary();
            } else {
                previousSummary = "开始新小说";
            }
        } else {
            previousSummary = "开始新小说";
        }
        
        // 获取角色信息
        List<Character> characters = characterRepository.findByNovelId(novelId);
        String characterInfo = buildCharacterInfo(characters);
        
        // 获取待展开的伏笔
        List<PlotHook> pendingHooks = plotHookRepository.findByNovelIdAndStatus(novelId, PlotHook.Status.PENDING);
        String pendingHookInfo = buildHookInfo(pendingHooks, chapterNumber);
        
        // 构建完整的Prompt
        StringBuilder prompt = new StringBuilder();
        
        // 添加作者方向段落（如果提供）
        if (direction != null && !direction.trim().isEmpty()) {
            prompt.append("【作者方向】\n");
            prompt.append("本章需包含：").append(direction).append("\n");
            
            if (bannedElements != null && bannedElements.length > 0) {
                prompt.append("禁止：").append(String.join("，", bannedElements)).append("\n");
            }
            
            if (mood != null && !mood.trim().isEmpty()) {
                prompt.append("情绪基调：").append(mood).append("\n");
            }
            
            // 根据是否覆盖高重要场景来设置节奏
            String rhythmInfo = checkRhythmForChapter(novelId, chapterNumber, overrideHighStakes);
            if (!rhythmInfo.isEmpty()) {
                prompt.append(rhythmInfo).append("\n");
            }
            
            prompt.append("\n");
        }
        
        prompt.append("【风格指令】\n");
        
        if (writingStyle != null) {
            if (writingStyle.getAvgSentenceLength() != null) {
                prompt.append("语言：平均句长 ").append(writingStyle.getAvgSentenceLength()).append(" 字");
            }
            if (writingStyle.getFrequentVerbs() != null && !writingStyle.getFrequentVerbs().isEmpty()) {
                prompt.append("，高频动词 ").append(writingStyle.getFrequentVerbs());
            }
            if (writingStyle.getBannedWords() != null && !writingStyle.getBannedWords().isEmpty()) {
                prompt.append("，禁用词 ").append(writingStyle.getBannedWords());
            }
            prompt.append("\n");
            
            if (writingStyle.getEmotionalDistance() != null) {
                prompt.append("叙事：").append(writingStyle.getEmotionalDistance()).append(" 视角");
            }
            if (writingStyle.getMoralStance() != null) {
                prompt.append("，").append(writingStyle.getMoralStance()).append(" 立场");
            }
            if (writingStyle.getHumorStyle() != null) {
                prompt.append("，").append(writingStyle.getHumorStyle()).append(" 幽默");
            }
            prompt.append("\n");
            
            if (writingStyle.getDescriptionProfile() != null) {
                prompt.append("描写：").append(writingStyle.getDescriptionProfile()).append("\n");
            }
            
            if (writingStyle.getAuthorRules() != null) {
                prompt.append("约束：").append(writingStyle.getAuthorRules()).append("\n");
            }
        } else {
            prompt.append("语言：自然流畅，符合文学创作规范\n");
            prompt.append("叙事：第三人称，客观立场，适度文学性\n");
            prompt.append("描写：细节丰富，注重人物心理和环境描写\n");
        }
        
        prompt.append("\n【当前状态】\n");
        prompt.append("小说：《").append(novel.getTitle()).append("》");
        if (novel.getOutline() != null) {
            prompt.append("，整体走向：").append(novel.getOutline().length() > 100 ? novel.getOutline().substring(0, 100) + "..." : novel.getOutline());
        }
        prompt.append("\n");
        prompt.append("上一章结尾：\"").append(previousSummary).append("\"\n");
        
        if (!characterInfo.isEmpty()) {
            prompt.append(characterInfo).append("\n");
        }
        
        if (!pendingHookInfo.isEmpty()) {
            prompt.append("伏笔待展开：").append(pendingHookInfo).append("\n");
        }
        
        prompt.append("\n【任务】\n");
        prompt.append("生成第 ").append(chapterNumber).append(" 章，严格遵循上述所有要求。\n");
        prompt.append("1. 严格遵循上述风格\n");
        prompt.append("2. 保持与前文的连贯性\n");
        prompt.append("3. 体现角色的连贯性，避免OOC\n");
        prompt.append("4. 输出纯正文，无说明\n");
        prompt.append("5. 章节长度适中，结构完整");
        
        return prompt.toString();
    }
    
    private String buildCharacterInfo(List<Character> characters) {
        if (characters.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("角色状态：\n");
        for (Character character : characters) {
            sb.append("主角 ").append(character.getName()).append("：处于 ").append(character.getCurrentArcStage())
              .append(" 阶段，信念：").append(character.getCoreBelief()).append("\n");
        }
        return sb.toString();
    }
    
    private String buildHookInfo(List<PlotHook> hooks, Integer currentChapter) {
        if (hooks.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hooks.size(); i++) {
            PlotHook hook = hooks.get(i);
            if (currentChapter >= hook.getMinChapter() && currentChapter <= hook.getMaxChapter()) {
                if (i > 0) sb.append("；");
                sb.append(hook.getDescription()).append("（").append(hook.getMinChapter()).append("-").append(hook.getMaxChapter()).append("章展开）");
            }
        }
        return sb.toString();
    }
    
    private String checkRhythmForChapter(Long novelId, Integer chapterNumber, Boolean overrideHighStakes) {
        // 如果用户明确覆盖高重要场景设置，则使用用户的设置
        if (overrideHighStakes != null) {
            if (overrideHighStakes) {
                return "节奏建议：高重要场景";
            } else {
                return "节奏建议：非高重要场景（铺垫）";
            }
        }
        
        // 检查最近的几章是否都是高重要场景
        // 简单实现：检查前3章是否都是高重要场景
        int highStakesCount = 0;
        for (int i = 1; i <= 3; i++) {
            if (chapterNumber - i <= 0) break;
            Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber - i);
            if (chapterOpt.isPresent() && Boolean.TRUE.equals(chapterOpt.get().getIsHighStakes())) {
                highStakesCount++;
            } else {
                break; // 不连续时停止计数
            }
        }
        
        if (highStakesCount >= 3) {
            return "节奏：已连续 " + highStakesCount + " 章高重要场景，本章不得解决冲突";
        }
        return "";
    }
    
    // 保留原有方法以兼容性
    private String checkRhythmForChapter(Long novelId, Integer chapterNumber) {
        return checkRhythmForChapter(novelId, chapterNumber, null);
    }
}