package com.aiwriter.service;

import com.aiwriter.entity.Character;
import com.aiwriter.entity.Chapter;
import com.aiwriter.repository.CharacterRepository;
import com.aiwriter.service.ai.AiService;
import com.aiwriter.service.CharacterLorebookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 角色自动提取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterExtractionService {
    
    private final CharacterRepository characterRepository;
    private final AiService aiService;
    private final CharacterLorebookService characterLorebookService;
    
    /**
     * 异步提取角色
     */
    @Async("autoExtractionExecutor")
    @Transactional
    public void extractAndSyncCharacter(Chapter chapter, double similarityThreshold) {
        try {
            Long novelId = chapter.getNovelId();
            
            // 获取已有角色
            List<Character> existingCharacters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
            
            // AI提取角色信息
            List<ExtractedCharacter> extractedCharacters = detectCharacters(chapter.getContent());
            if (extractedCharacters.isEmpty()) {
                log.info("未提取到角色信息");
                return;
            }
            
            log.info("章节 {} 中检测到 {} 个角色", chapter.getId(), extractedCharacters.size());
            
            // 处理每个提取到的角色
            for (ExtractedCharacter extracted : extractedCharacters) {
                // 检查是否已存在相似角色
                Character existing = findMostSimilarCharacter(extracted, existingCharacters, similarityThreshold);
                
                if (existing == null) {
                    // 创建新角色
                    Character newCharacter = createNewCharacter(chapter, extracted);
                    characterRepository.save(newCharacter);
                    log.info("创建新角色: id={}, name={}", newCharacter.getId(), newCharacter.getName());
                } else {
                    // 更新现有角色
                    updateExistingCharacter(existing, chapter, extracted);
                    characterRepository.save(existing);
                    log.info("更新角色: id={}, name={}", existing.getId(), existing.getName());
                }
            }
            
        } catch (Exception e) {
            log.error("角色提取失败: chapterId={}", chapter.getId(), e);
        }
    }
    
    /**
     * 检测章节中的角色
     */
    private List<ExtractedCharacter> detectCharacters(String content) {
        if (content == null || content.length() < 100) {
            log.info("章节内容过短，跳过角色提取");
            return new ArrayList<>();
        }
        
        try {
            // 使用AI分析章节内容，提取角色
            String systemPrompt = "你是一位专业的小说分析师，擅长识别故事中的角色。请识别文本中出现的所有重要角色。注意：如果角色有别名和真名，请使用格式'别名(真名)'，不要使用方括号。";
            String userPrompt = String.format(
                "请分析以下章节内容，识别其中的角色。返回格式：\n" +
                "角色名|角色类型|角色描述\n\n" +
                "角色类型包括：PROTAGONIST(主角)、ANTAGONIST(反派)、SUPPORTING(配角)、MINOR(次要角色)\n\n" +
                "特别注意：如果角色有别名和真名，请使用格式'别名(真名)'，例如'阿烬(陈烬)'，不要使用方括号如'阿烬[陈烬]'。\n\n" +
                "章节内容：\n%s",
                content.length() > 1500 ? content.substring(0, 1500) : content
            );
            
            String result = aiService.chat(systemPrompt, userPrompt).trim();
            
            List<ExtractedCharacter> characters = new ArrayList<>();
            String[] lines = result.split("\n");
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || !line.contains("|")) {
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    ExtractedCharacter character = new ExtractedCharacter();
                    character.name = parts[0].trim();
                    character.roleType = parts.length > 1 ? parts[1].trim() : "SUPPORTING";
                    character.description = parts.length > 2 ? parts[2].trim() : "";
                    
                    // 验证角色名是否有效
                    if (isValidCharacterName(character.name)) {
                        characters.add(character);
                    }
                }
            }
            
            // 去重
            return characters.stream()
                .filter(distinctByName())
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.warn("AI角色检测失败，回退到关键词检测: {}", e.getMessage());
            return fallbackDetectCharacters(content);
        }
    }
    
    /**
     * 验证角色名是否有效
     */
    private boolean isValidCharacterName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // 过滤掉无效的角色名
        String[] invalidNames = {"无", "无角色", "无明显角色", "无主要角色", "无新角色"};
        for (String invalidName : invalidNames) {
            if (name.contains(invalidName)) {
                return false;
            }
        }
        
        // 检查是否为中文或英文字符
        return name.matches(".*[\\u4e00-\\u9fa5a-zA-Z].*");
    }
    
    /**
     * 去重方法：按角色名去重
     */
    private java.util.function.Predicate<ExtractedCharacter> distinctByName() {
        Set<String> seen = new HashSet<>();
        return character -> seen.add(character.name);
    }
    
    /**
     * 回退：关键词检测
     */
    private List<ExtractedCharacter> fallbackDetectCharacters(String content) {
        List<ExtractedCharacter> characters = new ArrayList<>();
        
        // 使用正则表达式匹配可能的角色名（中文名、英文名）
        // 匹配中文姓名（2-4个中文字符）
        Pattern chineseNamePattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,4})(?=[说叫道问答])|(?<=[说叫道问答])([\\u4e00-\\u9fa5]{2,4})");
        Matcher matcher = chineseNamePattern.matcher(content);
        
        Set<String> uniqueNames = new HashSet<>();
        while (matcher.find()) {
            String name = matcher.group();
            if (name != null && name.length() >= 2 && name.length() <= 4) {
                uniqueNames.add(name);
            }
        }
        
        for (String name : uniqueNames) {
            ExtractedCharacter character = new ExtractedCharacter();
            character.name = name;
            character.roleType = "SUPPORTING";
            character.description = "自动检测到的角色";
            characters.add(character);
        }
        
        return characters;
    }
    
    /**
     * 查找最相似的角色
     */
    private Character findMostSimilarCharacter(ExtractedCharacter extracted, List<Character> existingCharacters, double threshold) {
        Character mostSimilar = null;
        double maxSimilarity = 0.0;
        
        for (Character existing : existingCharacters) {
            double similarity = calculateSimilarity(extracted.name, existing.getName());
            if (similarity > maxSimilarity && similarity >= threshold) {
                maxSimilarity = similarity;
                mostSimilar = existing;
            }
        }
        
        return mostSimilar;
    }
    
    /**
     * 计算相似度（简单实现）
     */
    private double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        // 计算最长公共子序列
        int lcsLength = longestCommonSubsequence(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        
        if (maxLength == 0) {
            return 1.0; // 两个空字符串完全相似
        }
        
        double similarity = (double) lcsLength / maxLength;
        
        // 如果名字完全相同，返回1.0
        if (text1.equals(text2)) {
            return 1.0;
        }
        
        return similarity;
    }
    
    /**
     * 计算最长公共子序列
     */
    private int longestCommonSubsequence(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }
    
    /**
     * 创建新角色
     */
    private Character createNewCharacter(Chapter chapter, ExtractedCharacter extracted) {
        Character character = new Character();
        character.setNovelId(chapter.getNovelId());
        character.setName(extracted.name);
        character.setRoleType(extractRoleType(extracted.roleType));
        character.setPersonality(extracted.description);
        character.setImportanceLevel(determineImportanceLevel(extracted.roleType));
        character.setFirstAppearanceChapter(chapter.getChapterNumber().intValue());
        character.setLastAppearanceChapter(chapter.getChapterNumber().intValue());
        character.setAppearanceCount(1);
        
        return character;
    }
    
    /**
     * 更新现有角色
     */
    private void updateExistingCharacter(Character existing, Chapter chapter, ExtractedCharacter extracted) {
        // 更新出现章节信息
        Integer chapterNum = chapter.getChapterNumber().intValue();
        if (existing.getFirstAppearanceChapter() == null || 
            chapterNum < existing.getFirstAppearanceChapter()) {
            existing.setFirstAppearanceChapter(chapterNum);
        }
        
        if (existing.getLastAppearanceChapter() == null || 
            chapterNum > existing.getLastAppearanceChapter()) {
            existing.setLastAppearanceChapter(chapterNum);
        }
        
        // 增加出现次数
        existing.setAppearanceCount(existing.getAppearanceCount() + 1);
        
        // 如果角色描述为空且提取到了描述，更新描述
        if ((existing.getPersonality() == null || existing.getPersonality().isEmpty()) && 
            extracted.description != null && !extracted.description.isEmpty()) {
            existing.setPersonality(extracted.description);
        }
        
        // 更新角色记忆库
        try {
            String updatedInfo = "性格特征: " + (existing.getPersonality() != null ? existing.getPersonality() : "") + "\n" +
                              "背景故事: " + (existing.getBackground() != null ? existing.getBackground() : "") + "\n" +
                              "外貌描述: " + (existing.getAppearance() != null ? existing.getAppearance() : "") + "\n" +
                              "能力特长: " + (existing.getAbilities() != null ? existing.getAbilities() : "") + "\n" +
                              "核心动机: " + (existing.getMotivation() != null ? existing.getMotivation() : "") + "\n" +
                              "角色弧光: " + (existing.getArc() != null ? existing.getArc() : "");
            
            characterLorebookService.updateCharacterLorebook(existing.getNovelId(), existing.getId(), updatedInfo);
            log.info("更新角色记忆库: {}", existing.getName());
        } catch (Exception e) {
            log.warn("更新角色记忆库失败: {}", existing.getName(), e);
        }
    }
    
    /**
     * 提取角色类型
     */
    private String extractRoleType(String roleType) {
        if (roleType == null) return "SUPPORTING";
        
        roleType = roleType.toUpperCase();
        if (roleType.contains("PROTAGONIST") || roleType.contains("主角")) {
            return "PROTAGONIST";
        } else if (roleType.contains("ANTAGONIST") || roleType.contains("反派")) {
            return "ANTAGONIST";
        } else if (roleType.contains("SUPPORTING") || roleType.contains("配角")) {
            return "SUPPORTING";
        } else {
            return "MINOR";
        }
    }
    
    /**
     * 确定重要性等级
     */
    private Integer determineImportanceLevel(String roleType) {
        if (roleType == null) return 5;
        
        roleType = roleType.toUpperCase();
        if (roleType.contains("PROTAGONIST") || roleType.contains("主角")) {
            return 10;
        } else if (roleType.contains("ANTAGONIST") || roleType.contains("反派")) {
            return 9;
        } else if (roleType.contains("SUPPORTING") || roleType.contains("配角")) {
            return 6;
        } else {
            return 3;
        }
    }
    
    /**
     * 内部类：提取的角色信息
     */
    private static class ExtractedCharacter {
        String name;
        String roleType;
        String description;
    }
}