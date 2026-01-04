package com.ainovel.novelcraft.controller;

import com.ainovel.novelcraft.entity.WritingStyle;
import com.ainovel.novelcraft.service.WritingStyleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/novels/{novelId}/writing-style")
public class WritingStyleController {
    
    @Autowired
    private WritingStyleService writingStyleService;
    
    @GetMapping
    public ResponseEntity<WritingStyle> getWritingStyleByNovelId(@PathVariable Long novelId) {
        WritingStyle writingStyle = writingStyleService.getWritingStyleByNovelId(novelId);
        if (writingStyle != null) {
            return ResponseEntity.ok(writingStyle);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<WritingStyle> createWritingStyle(@PathVariable Long novelId, @RequestBody WritingStyle writingStyle) {
        writingStyle.setNovelId(novelId);
        WritingStyle createdWritingStyle = writingStyleService.createWritingStyle(writingStyle);
        return ResponseEntity.ok(createdWritingStyle);
    }
    
    @PutMapping
    public ResponseEntity<WritingStyle> updateWritingStyle(@PathVariable Long novelId, @RequestBody WritingStyle writingStyle) {
        WritingStyle existingWritingStyle = writingStyleService.getWritingStyleByNovelId(novelId);
        if (existingWritingStyle != null) {
            writingStyle.setId(existingWritingStyle.getId());
            writingStyle.setNovelId(novelId);
            WritingStyle updatedWritingStyle = writingStyleService.updateWritingStyle(writingStyle);
            return ResponseEntity.ok(updatedWritingStyle);
        } else {
            // 如果不存在则创建
            writingStyle.setNovelId(novelId);
            WritingStyle createdWritingStyle = writingStyleService.createWritingStyle(writingStyle);
            return ResponseEntity.ok(createdWritingStyle);
        }
    }
    
    @DeleteMapping
    public ResponseEntity<Void> deleteWritingStyle(@PathVariable Long novelId) {
        WritingStyle writingStyle = writingStyleService.getWritingStyleByNovelId(novelId);
        if (writingStyle != null) {
            writingStyleService.deleteWritingStyle(writingStyle.getId());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}