package com.ainovel.novelcraft.controller;

import com.ainovel.novelcraft.dto.CreateChapterResponse;
import com.ainovel.novelcraft.dto.GenerateChapterRequest;
import com.ainovel.novelcraft.entity.Chapter;
import com.ainovel.novelcraft.service.ChapterService;
import com.ainovel.novelcraft.service.NovelChapterGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/novels")
public class ChapterController {
    
    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private NovelChapterGenerationService novelChapterGenerationService;
    
    @GetMapping("/{novelId}/chapters")
    public ResponseEntity<List<Chapter>> getChaptersByNovelId(@PathVariable Long novelId) {
        List<Chapter> chapters = chapterService.getChaptersByNovelId(novelId);
        return ResponseEntity.ok(chapters);
    }
    
    @GetMapping("/{novelId}/chapters/{chapterNumber}")
    public ResponseEntity<Chapter> getChapterByNovelIdAndChapterNumber(
            @PathVariable Long novelId, 
            @PathVariable Integer chapterNumber) {
        Optional<Chapter> chapter = chapterService.getChapterByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (chapter.isPresent()) {
            return ResponseEntity.ok(chapter.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{novelId}/chapters")
    public ResponseEntity<CreateChapterResponse> generateChapter(
            @PathVariable Long novelId,
            @RequestParam Integer chapterNumber) {
        try {
            Chapter generatedChapter = novelChapterGenerationService.generateChapter(novelId, chapterNumber);
            CreateChapterResponse response = new CreateChapterResponse();
            response.setChapter(generatedChapter);
            response.setSuccess(true);
            response.setMessage("章节生成成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CreateChapterResponse response = new CreateChapterResponse();
            response.setSuccess(false);
            response.setMessage("章节生成失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/{novelId}/chapters/directed")
    public ResponseEntity<CreateChapterResponse> generateChapterWithDirection(
            @PathVariable Long novelId,
            @RequestParam Integer chapterNumber,
            @RequestBody GenerateChapterRequest request) {
        try {
            Chapter generatedChapter = novelChapterGenerationService.generateChapterWithDirection(request, novelId, chapterNumber);
            CreateChapterResponse response = new CreateChapterResponse();
            response.setChapter(generatedChapter);
            response.setSuccess(true);
            response.setMessage("章节生成成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CreateChapterResponse response = new CreateChapterResponse();
            response.setSuccess(false);
            response.setMessage("章节生成失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}