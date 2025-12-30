package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.TextImportRequest;
import com.aiwriter.dto.TextImportResponse;
import com.aiwriter.entity.ImportedChapter;
import com.aiwriter.entity.TextImport;
import com.aiwriter.service.TextImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文本导入控制器
 */
@RestController
@RequestMapping("/api/text-imports")
@RequiredArgsConstructor
public class TextImportController {

    private final TextImportService textImportService;

    /**
     * 上传文本文件
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<TextImport>> uploadText(
            @RequestParam Long novelId,
            @RequestParam MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件不能为空"));
        }

        TextImport textImport = textImportService.uploadAndParseText(novelId, file);

        if ("failed".equals(textImport.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(textImport.getErrorMessage()));
        }

        return ResponseEntity.ok(ApiResponse.success(textImport, "文本文件已上传并解析"));
    }

    /**
     * 获取导入记录的详情和预览
     */
    @GetMapping("/{importId}")
    public ResponseEntity<ApiResponse<TextImportResponse>> getImportDetails(@PathVariable Long importId) {
        // Implementation would fetch the TextImport and build response
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 获取小说的所有导入记录
     */
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<ApiResponse<List<TextImport>>> getNovelImports(@PathVariable Long novelId) {
        // Implementation would fetch imports for novel
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 提取关键要素（地点、人物、事件）
     */
    @PostMapping("/{importId}/extract-elements")
    public ResponseEntity<ApiResponse<TextImport>> extractKeyElements(@PathVariable Long importId) {
        textImportService.extractKeyElements(importId);
        return ResponseEntity.ok(ApiResponse.success(null, "关键要素提取成功"));
    }

    /**
     * 获取导入记录识别的章节列表
     */
    @GetMapping("/{importId}/chapters")
    public ResponseEntity<ApiResponse<List<ImportedChapter>>> getImportedChapters(@PathVariable Long importId) {
        List<ImportedChapter> chapters = textImportService.getImportedChapters(importId);
        return ResponseEntity.ok(ApiResponse.success(chapters));
    }

    /**
     * 确认导入并将章节转换为正式章节
     */
    @PostMapping("/{importId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmImport(@PathVariable Long importId) {
        textImportService.confirmImport(importId);
        return ResponseEntity.ok(ApiResponse.success(null, "导入已确认"));
    }
}
