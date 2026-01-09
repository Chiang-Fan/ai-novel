package com.aiwriter.service;

import com.aiwriter.entity.Character;
import com.aiwriter.entity.Novel;
import com.aiwriter.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色记忆库服务（Lorebook）
 * 管理角色设定，确保长篇创作中的一致性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterLorebookService {
    
    private final CharacterRepository characterRepository;
    
    /**
     * 获取角色记忆库内容
     */
    public Map<String, String> getCharacterLorebook(Long novelId) {
        List<Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
        
        return characters.stream()
            .collect(Collectors.toMap(
                Character::getName,
                this::buildCharacterProfile,
                (existing, replacement) -> existing // 如果有重复键，保留现有值
            ));
    }
    
    /**
     * 构建角色档案
     */
    private String buildCharacterProfile(Character character) {
        StringBuilder profile = new StringBuilder();
        
        profile.append("角色名: ").append(character.getName()).append("\n");
        profile.append("角色类型: ").append(character.getRoleType()).append("\n");
        
        if (character.getPersonality() != null) {
            profile.append("性格特征: ").append(character.getPersonality()).append("\n");
        }
        
        if (character.getBackground() != null) {
            profile.append("背景故事: ").append(character.getBackground()).append("\n");
        }
        
        if (character.getAppearance() != null) {
            profile.append("外貌描述: ").append(character.getAppearance()).append("\n");
        }
        
        if (character.getAbilities() != null) {
            profile.append("能力特长: ").append(character.getAbilities()).append("\n");
        }
        
        if (character.getMotivation() != null) {
            profile.append("核心动机: ").append(character.getMotivation()).append("\n");
        }
        
        if (character.getArc() != null) {
            profile.append("角色弧光: ").append(character.getArc()).append("\n");
        }
        
        return profile.toString();
    }
    
    /**
     * 更新角色记忆库
     */
    @Transactional
    public void updateCharacterLorebook(Long novelId, Long characterId, String updatedInfo) {
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        // 解析更新信息并更新角色属性
        // 这里简化实现，实际应用中需要更复杂的解析逻辑
        if (updatedInfo.contains("性格特征:")) {
            String personality = extractValue(updatedInfo, "性格特征:");
            character.setPersonality(personality);
        }
        
        if (updatedInfo.contains("背景故事:")) {
            String background = extractValue(updatedInfo, "背景故事:");
            character.setBackground(background);
        }
        
        characterRepository.save(character);
        log.info("更新角色记忆库: {}", character.getName());
    }
    
    /**
     * 提取值
     */
    private String extractValue(String text, String key) {
        int startIndex = text.indexOf(key);
        if (startIndex == -1) {
            return "";
        }
        
        int endIndex = text.indexOf("\n", startIndex + key.length());
        if (endIndex == -1) {
            endIndex = text.length();
        }
        
        return text.substring(startIndex + key.length(), endIndex).trim();
    }
}