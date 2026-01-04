package com.ainovel.novelcraft.service;

import com.ainovel.novelcraft.entity.Character;
import com.ainovel.novelcraft.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CharacterService {
    
    @Autowired
    private CharacterRepository characterRepository;
    
    public List<Character> getCharactersByNovelId(Long novelId) {
        return characterRepository.findByNovelId(novelId);
    }
    
    public Character getCharacterById(Long id) {
        return characterRepository.findById(id).orElse(null);
    }
    
    public Character createCharacter(Character character) {
        return characterRepository.save(character);
    }
    
    public Character updateCharacter(Character character) {
        return characterRepository.save(character);
    }
    
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
    }
}