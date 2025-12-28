package com.ai.novel.service;

import com.ai.novel.dto.request.CharacterCreateRequest;
import com.ai.novel.entity.Character;
import com.ai.novel.entity.Novel;
import com.ai.novel.exception.ResourceNotFoundException;
import com.ai.novel.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {
    
    private final CharacterRepository characterRepository;
    private final NovelService novelService;
    
    /**
     * 创建角色
     */
    @Transactional
    public Character createCharacter(CharacterCreateRequest request) {
        log.info("创建角色: {}", request.getName());
        
        // 验证小说存在
        Novel novel = novelService.getNovelById(request.getNovelId());
        
        Character character = new Character();
        character.setNovel(novel);
        character.setName(request.getName());
        character.setDescription(request.getDescription());
        character.setPersonality(request.getPersonality());
        character.setBackstory(request.getBackstory());
        character.setImportance(request.getImportance());
        character.setAge(request.getAge());
        character.setGender(request.getGender());
        character.setAppearance(request.getAppearance());
        character.setAbilities(request.getAbilities());
        character.setGoals(request.getGoals());
        character.setCurrentState(request.getCurrentState());
        character.setAppearanceCount(0);
        
        Character saved = characterRepository.save(character);
        log.info("角色创建成功, ID: {}", saved.getId());
        return saved;
    }
    
    /**
     * 更新角色
     */
    @Transactional
    public Character updateCharacter(Long id, CharacterCreateRequest request) {
        log.info("更新角色: {}", id);
        
        Character character = getCharacterById(id);
        
        if (request.getName() != null) {
            character.setName(request.getName());
        }
        if (request.getDescription() != null) {
            character.setDescription(request.getDescription());
        }
        if (request.getPersonality() != null) {
            character.setPersonality(request.getPersonality());
        }
        if (request.getBackstory() != null) {
            character.setBackstory(request.getBackstory());
        }
        if (request.getImportance() != null) {
            character.setImportance(request.getImportance());
        }
        if (request.getAge() != null) {
            character.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            character.setGender(request.getGender());
        }
        if (request.getAppearance() != null) {
            character.setAppearance(request.getAppearance());
        }
        if (request.getAbilities() != null) {
            character.setAbilities(request.getAbilities());
        }
        if (request.getGoals() != null) {
            character.setGoals(request.getGoals());
        }
        if (request.getCurrentState() != null) {
            character.setCurrentState(request.getCurrentState());
        }
        
        return characterRepository.save(character);
    }
    
    /**
     * 获取角色详情
     */
    @Transactional(readOnly = true)
    public Character getCharacterById(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.character(id));
    }
    
    /**
     * 查询小说的所有角色
     */
    @Transactional(readOnly = true)
    public List<Character> listCharactersByNovel(Long novelId) {
        novelService.getNovelById(novelId); // 验证小说存在
        return characterRepository.findByNovelId(novelId);
    }
    
    /**
     * 分页查询角色
     */
    @Transactional(readOnly = true)
    public Page<Character> listCharacters(Long novelId, Pageable pageable) {
        novelService.getNovelById(novelId); // 验证小说存在
        return characterRepository.findByNovelId(novelId, pageable);
    }
    
    /**
     * 删除角色
     */
    @Transactional
    public void deleteCharacter(Long id) {
        log.info("删除角色: {}", id);
        
        if (!characterRepository.existsById(id)) {
            throw ResourceNotFoundException.character(id);
        }
        
        characterRepository.deleteById(id);
        log.info("角色删除成功, ID: {}", id);
    }
    
    /**
     * 更新角色出场信息
     */
    @Transactional
    public void updateAppearance(Long characterId) {
        Character character = getCharacterById(characterId);
        character.setAppearanceCount(character.getAppearanceCount() + 1);
        
        if (character.getFirstAppearance() == null) {
            character.setFirstAppearance(LocalDateTime.now());
        }
        // Note: lastAppearance字段当前使用lastAppearanceChapter来跟踪
        
        characterRepository.save(character);
    }
}
