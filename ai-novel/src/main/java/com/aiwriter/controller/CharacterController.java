package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.CharacterRecommendationRequest;
import com.aiwriter.dto.CharacterRecommendationResponse;
import com.aiwriter.entity.Character;
import com.aiwriter.service.CharacterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    
    private final CharacterService characterService;
    
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<Character>> getCharactersByNovel(@PathVariable Long novelId) {
        List<Character> characters = characterService.getCharactersByNovel(novelId);
        return ApiResponse.success(characters);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Character> getCharacter(@PathVariable Long id) {
        Character character = characterService.getCharacterById(id);
        return ApiResponse.success(character);
    }
    
    @PostMapping
    public ApiResponse<Character> createCharacter(@Valid @RequestBody Character character) {
        Character created = characterService.createCharacter(character);
        return ApiResponse.success(created);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Character> updateCharacter(@PathVariable Long id, 
                                                   @Valid @RequestBody Character character) {
        Character updated = characterService.updateCharacter(id, character);
        return ApiResponse.success(updated);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/recommend")
    public ApiResponse<List<CharacterRecommendationResponse>> recommendCharacters(
            @Valid @RequestBody CharacterRecommendationRequest request) {
        List<CharacterRecommendationResponse> recommendations = 
            characterService.recommendCharacters(request);
        return ApiResponse.success(recommendations);
    }
}
