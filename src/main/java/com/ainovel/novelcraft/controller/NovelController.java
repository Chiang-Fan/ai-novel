package com.ainovel.novelcraft.controller;

import com.ainovel.novelcraft.dto.CreateNovelRequest;
import com.ainovel.novelcraft.entity.Novel;
import com.ainovel.novelcraft.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/novels")
public class NovelController {
    
    @Autowired
    private NovelService novelService;
    
    @GetMapping
    public ResponseEntity<List<Novel>> getAllNovels() {
        List<Novel> novels = novelService.getAllNovels();
        return ResponseEntity.ok(novels);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Novel> getNovelById(@PathVariable Long id) {
        Optional<Novel> novel = novelService.getNovelById(id);
        if (novel.isPresent()) {
            return ResponseEntity.ok(novel.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Novel> createNovel(@RequestBody CreateNovelRequest request) {
        Novel novel = novelService.createNovel(request.getTitle(), request.getOutline());
        return ResponseEntity.ok(novel);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Novel> updateNovel(@PathVariable Long id, @RequestBody Novel novel) {
        novel.setId(id);
        Novel updatedNovel = novelService.updateNovel(novel);
        return ResponseEntity.ok(updatedNovel);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNovel(@PathVariable Long id) {
        novelService.deleteNovel(id);
        return ResponseEntity.noContent().build();
    }
}