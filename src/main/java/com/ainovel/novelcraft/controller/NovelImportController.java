package com.ainovel.novelcraft.controller;

import com.ainovel.novelcraft.dto.ImportNovelRequest;
import com.ainovel.novelcraft.entity.Novel;
import com.ainovel.novelcraft.service.NovelImporterService;
import com.ainovel.novelcraft.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/novels")
public class NovelImportController {
    
    @Autowired
    private NovelImporterService novelImporterService;
    
    @Autowired
    private NovelService novelService;
    
    @PostMapping("/import")
    public ResponseEntity<Novel> importNovel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "outline", required = false) String outline) {
        
        try {
            // 验证文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || !(fileName.toLowerCase().endsWith(".txt") || fileName.toLowerCase().endsWith(".docx"))) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // 创建导入请求对象
            ImportNovelRequest request = new ImportNovelRequest();
            request.setTitle(title != null ? title : "导入的小说");
            request.setOutline(outline);
            
            // 执行导入
            Novel importedNovel = novelImporterService.importNovel(file, request);
            Novel savedNovel = novelService.createNovel(importedNovel.getTitle(), importedNovel.getOutline());
            
            return ResponseEntity.ok(savedNovel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}