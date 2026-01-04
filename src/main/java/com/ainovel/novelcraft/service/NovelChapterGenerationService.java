package com.ainovel.novelcraft.service;

import com.ainovel.novelcraft.dto.GenerateChapterRequest;
import com.ainovel.novelcraft.entity.Chapter;
import com.ainovel.novelcraft.entity.Novel;
import com.ainovel.novelcraft.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NovelChapterGenerationService {
    
    @Autowired
    private NovelService novelService;
    
    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private PromptBuilderService promptBuilderService;
    
    @Autowired
    private AIService aiService;
    
    public Chapter generateChapter(Long novelId, Integer chapterNumber) {
        // 验证小说是否存在
        if (!novelService.getNovelById(novelId).isPresent()) {
            throw new IllegalArgumentException("Novel not found with id: " + novelId);
        }
        
        // 检查章节是否已存在
        if (chapterService.getChapterByNovelIdAndChapterNumber(novelId, chapterNumber).isPresent()) {
            throw new IllegalArgumentException("Chapter " + chapterNumber + " already exists for novel " + novelId);
        }
        
        // 构建AI提示词
        String prompt = promptBuilderService.buildChapterGenerationPrompt(novelId, chapterNumber);
        
        // 使用AI生成章节内容
        String chapterContent;
        try {
            chapterContent = aiService.generateChapterContent(prompt);
        } catch (Exception e) {
            throw new RuntimeException("生成章节失败: " + e.getMessage(), e);
        }
        
        // 保存生成的章节
        String chapterTitle = "第" + chapterNumber + "章";
        Chapter newChapter = chapterService.createChapter(novelId, chapterNumber, chapterTitle, chapterContent);
        
        return newChapter;
    }
    
    public Chapter generateChapterWithDirection(GenerateChapterRequest request, Long novelId, Integer chapterNumber) {
        // 验证小说是否存在
        if (!novelService.getNovelById(novelId).isPresent()) {
            throw new IllegalArgumentException("Novel not found with id: " + novelId);
        }
        
        // 检查章节是否已存在
        if (chapterService.getChapterByNovelIdAndChapterNumber(novelId, chapterNumber).isPresent()) {
            throw new IllegalArgumentException("Chapter " + chapterNumber + " already exists for novel " + novelId);
        }
        
        // 构建AI提示词（包含作者方向）
        String prompt = promptBuilderService.buildChapterGenerationPrompt(
            novelId, 
            chapterNumber, 
            request.getDirection(), 
            request.getBannedElements(), 
            request.getMood(), 
            request.getOverrideHighStakes()
        );
        
        // 使用AI生成章节内容
        String chapterContent;
        try {
            chapterContent = aiService.generateChapterContent(prompt);
        } catch (Exception e) {
            throw new RuntimeException("生成章节失败: " + e.getMessage(), e);
        }
        
        // 保存生成的章节（包含方向信息）
        String chapterTitle = "第" + chapterNumber + "章";
        Chapter newChapter = chapterService.createChapterWithDirection(
            novelId, 
            chapterNumber, 
            chapterTitle, 
            chapterContent,
            request.getDirection(),
            request.getBannedElements(),
            request.getMood(),
            prompt
        );
        
        return newChapter;
    }
}