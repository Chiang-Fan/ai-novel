package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.ChapterAnalysisDto;
import com.aiwriter.dto.ChapterAnalysisStatistics;
import com.aiwriter.service.ChapterAnalyzerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节深度分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chapter-analysis")
@CrossOrigin
public class ChapterAnalysisController {
    
    @Autowired
    private ChapterAnalyzerService analyzerService;
    
    /**
     * 深度分析章节
     * POST /api/chapter-analysis/analyze/{chapterId}
     */
    @PostMapping("/analyze/{chapterId}")
    public ApiResponse<ChapterAnalysisDto> analyzeChapter(@PathVariable Long chapterId) {
        try {
            log.info("开始深度分析章节: {}", chapterId);
            ChapterAnalysisDto result = analyzerService.analyzeChapter(chapterId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("深度分析章节失败: {}", chapterId, e);
            return ApiResponse.error("深度分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取章节分析结果
     * GET /api/chapter-analysis/chapter/{chapterId}
     */
    @GetMapping("/chapter/{chapterId}")
    public ApiResponse<ChapterAnalysisDto> getChapterAnalysis(@PathVariable Long chapterId) {
        try {
            ChapterAnalysisDto result = analyzerService.getChapterAnalysis(chapterId);
            if (result == null) {
                return ApiResponse.error("该章节尚未分析");
            }
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取章节分析失败: {}", chapterId, e);
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取小说所有章节分析
     * GET /api/chapter-analysis/novel/{novelId}
     */
    @GetMapping("/novel/{novelId}")
    public ApiResponse<List<ChapterAnalysisDto>> getNovelAnalyses(@PathVariable Long novelId) {
        try {
            List<ChapterAnalysisDto> results = analyzerService.getNovelAnalyses(novelId);
            return ApiResponse.success(results);
        } catch (Exception e) {
            log.error("获取小说章节分析失败: {}", novelId, e);
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取小说分析统计
     * GET /api/chapter-analysis/novel/{novelId}/statistics
     */
    @GetMapping("/novel/{novelId}/statistics")
    public ApiResponse<ChapterAnalysisStatistics> getStatistics(@PathVariable Long novelId) {
        try {
            ChapterAnalysisStatistics stats = analyzerService.getStatistics(novelId);
            if (stats == null) {
                return ApiResponse.error("该小说尚无分析数据");
            }
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", novelId, e);
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量分析小说所有章节
     * POST /api/chapter-analysis/batch-analyze/{novelId}
     */
    @PostMapping("/batch-analyze/{novelId}")
    public ApiResponse<String> batchAnalyze(@PathVariable Long novelId) {
        try {
            log.info("开始批量分析小说: {}", novelId);
            // TODO: 异步批量分析
            return ApiResponse.success("批量分析任务已启动");
        } catch (Exception e) {
            log.error("批量分析失败: {}", novelId, e);
            return ApiResponse.error("批量分析失败: " + e.getMessage());
        }
    }
}
