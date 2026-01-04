package com.ainovel.novelcraft.service;

import com.ainovel.novelcraft.entity.Chapter;
import com.ainovel.novelcraft.entity.Novel;
import com.ainovel.novelcraft.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChapterService {
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private NovelService novelService;
    
    public List<Chapter> getChaptersByNovelId(Long novelId) {
        return chapterRepository.findByNovelIdOrderByChapterNumber(novelId);
    }
    
    public Optional<Chapter> getChapterByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber) {
        return chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
    }
    
    public Chapter saveChapter(Chapter chapter) {
        return chapterRepository.save(chapter);
    }
    
    public Chapter createChapter(Long novelId, Integer chapterNumber, String title, String content) {
        // 验证小说是否存在
        if (!novelService.getNovelById(novelId).isPresent()) {
            throw new IllegalArgumentException("Novel not found with id: " + novelId);
        }
        
        // 检查是否已存在该章节号
        Optional<Chapter> existingChapter = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (existingChapter.isPresent()) {
            throw new IllegalArgumentException("Chapter " + chapterNumber + " already exists for novel " + novelId);
        }
        
        Chapter chapterEntity = new Chapter();
        chapterEntity.setNovelId(novelId);
        chapterEntity.setChapterNumber(chapterNumber);
        chapterEntity.setTitle(title);
        chapterEntity.setContent(content);
        
        return chapterRepository.save(chapterEntity);
    }
    
    public Chapter createChapterWithDirection(Long novelId, Integer chapterNumber, String title, String content, 
                                           String direction, String[] bannedElements, String mood, String generationPrompt) {
        // 验证小说是否存在
        if (!novelService.getNovelById(novelId).isPresent()) {
            throw new IllegalArgumentException("Novel not found with id: " + novelId);
        }
        
        // 检查是否已存在该章节号
        Optional<Chapter> existingChapter = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (existingChapter.isPresent()) {
            throw new IllegalArgumentException("Chapter " + chapterNumber + " already exists for novel " + novelId);
        }
        
        Chapter chapterEntity = new Chapter();
        chapterEntity.setNovelId(novelId);
        chapterEntity.setChapterNumber(chapterNumber);
        chapterEntity.setTitle(title);
        chapterEntity.setContent(content);
        chapterEntity.setGenerationDirection(direction);
        if (bannedElements != null) {
            chapterEntity.setBannedElements(String.join(",", bannedElements));
        }
        chapterEntity.setMood(mood);
        chapterEntity.setGenerationPrompt(generationPrompt);
        
        return chapterRepository.save(chapterEntity);
    }
}