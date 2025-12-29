package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.entity.EditHistory;
import com.aiwriter.service.EditHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/edit-history")
@RequiredArgsConstructor
public class EditHistoryController {
    
    private final EditHistoryService editHistoryService;
    
    @GetMapping("/chapter/{chapterId}")
    public ApiResponse<List<EditHistory>> getChapterHistory(@PathVariable Long chapterId) {
        List<EditHistory> history = editHistoryService.getHistoryByChapter(chapterId);
        return ApiResponse.success(history);
    }
}
