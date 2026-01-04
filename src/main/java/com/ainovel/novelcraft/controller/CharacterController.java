package com.ainovel.novelcraft.controller;

import com.ainovel.novelcraft.entity.Character;
import com.ainovel.novelcraft.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/novels/{novelId}/characters")
public class CharacterController {
    
    @Autowired
    private CharacterService characterService;
    
    @GetMapping
    public ResponseEntity<List<Character>> getCharactersByNovelId(@PathVariable Long novelId) {
        List<Character> characters = characterService.getCharactersByNovelId(novelId);
        return ResponseEntity.ok(characters);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Character> getCharacterById(@PathVariable Long novelId, @PathVariable Long id) {
        Character character = characterService.getCharacterById(id);
        if (character != null && character.getNovelId().equals(novelId)) {
            return ResponseEntity.ok(character);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Character> createCharacter(@PathVariable Long novelId, @RequestBody Character character) {
        character.setNovelId(novelId);
        Character createdCharacter = characterService.createCharacter(character);
        return ResponseEntity.ok(createdCharacter);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Character> updateCharacter(@PathVariable Long novelId, @PathVariable Long id, @RequestBody Character character) {
        Character existingCharacter = characterService.getCharacterById(id);
        if (existingCharacter != null && existingCharacter.getNovelId().equals(novelId)) {
            character.setId(id);
            character.setNovelId(novelId);
            Character updatedCharacter = characterService.updateCharacter(character);
            return ResponseEntity.ok(updatedCharacter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long novelId, @PathVariable Long id) {
        Character character = characterService.getCharacterById(id);
        if (character != null && character.getNovelId().equals(novelId)) {
            characterService.deleteCharacter(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}