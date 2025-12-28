package com.ai.novel.controller;

import com.ai.novel.dto.request.CharacterCreateRequest;
import com.ai.novel.dto.response.ApiResponse;
import com.ai.novel.dto.response.CharacterResponse;
import com.ai.novel.entity.Character;
import com.ai.novel.service.CharacterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    
    private final CharacterService characterService;
    
    /**
     * 创建角色
     */
    @PostMapping
    public ApiResponse<CharacterResponse> createCharacter(@Valid @RequestBody CharacterCreateRequest request) {
        Character character = characterService.createCharacter(request);
        return ApiResponse.success(convertToResponse(character));
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public ApiResponse<CharacterResponse> updateCharacter(
            @PathVariable Long id,
            @Valid @RequestBody CharacterCreateRequest request) {
        Character character = characterService.updateCharacter(id, request);
        return ApiResponse.success(convertToResponse(character));
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CharacterResponse> getCharacter(@PathVariable Long id) {
        Character character = characterService.getCharacterById(id);
        return ApiResponse.success(convertToResponse(character));
    }
    
    /**
     * 查询小说的所有角色
     */
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<CharacterResponse>> listCharactersByNovel(@PathVariable Long novelId) {
        List<CharacterResponse> characters = characterService.listCharactersByNovel(novelId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(characters);
    }
    
    /**
     * 分页查询角色
     */
    @GetMapping("/novel/{novelId}/page")
    public ApiResponse<Page<CharacterResponse>> listCharactersPage(
            @PathVariable Long novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "importance"));
        
        Page<CharacterResponse> characters = characterService.listCharacters(novelId, pageable)
                .map(this::convertToResponse);
        
        return ApiResponse.success(characters);
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ApiResponse.success("角色删除成功", null);
    }
    
    /**
     * 转换为响应DTO
     */
    private CharacterResponse convertToResponse(Character character) {
        CharacterResponse response = new CharacterResponse();
        response.setId(character.getId());
        response.setNovelId(character.getNovel().getId());
        response.setName(character.getName());
        response.setDescription(character.getDescription());
        response.setPersonality(character.getPersonality());
        response.setBackstory(character.getBackstory());
        response.setImportance(character.getImportance());
        response.setAge(character.getAge());
        response.setGender(character.getGender());
        response.setAppearance(character.getAppearance());
        response.setAbilities(character.getAbilities());
        response.setGoals(character.getGoals());
        response.setCurrentState(character.getCurrentState());
        response.setAppearanceCount(character.getAppearanceCount());
        response.setFirstAppearance(character.getFirstAppearance());
        response.setLastAppearance(character.getLastAppearance());
        response.setCreatedAt(character.getCreatedAt());
        response.setUpdatedAt(character.getUpdatedAt());
        return response;
    }
}
