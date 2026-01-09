package com.aiwriter.controller;

import com.aiwriter.entity.PlotForeshadowing;
import com.aiwriter.service.PlotForeshadowingService;
import com.aiwriter.dto.PlotForeshadowingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 伏笔管理系统控制器
 * 提供伏笔管理的REST API接口
 */
@RestController
@RequestMapping("/api/plot-foreshadowing")
@RequiredArgsConstructor
public class PlotForeshadowingController {
    
    private final PlotForeshadowingService plotForeshadowingService;
    
    /**
     * 获取小说的所有伏笔
     */
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<List<PlotForeshadowing>> getPlotForeshadowings(@PathVariable Long novelId) {
        List<PlotForeshadowing> foreshadowings = plotForeshadowingService.getPlotForeshadowings(novelId);
        return ResponseEntity.ok(foreshadowings);
    }
    
    /**
     * 获取特定章节的伏笔
     */
    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<List<PlotForeshadowing>> getPlotForeshadowingsByChapter(@PathVariable Long chapterId) {
        List<PlotForeshadowing> foreshadowings = plotForeshadowingService.getPlotForeshadowingsByChapter(chapterId);
        return ResponseEntity.ok(foreshadowings);
    }
    
    /**
     * 创建新伏笔
     */
    @PostMapping
    public ResponseEntity<PlotForeshadowing> createPlotForeshadowing(@RequestBody PlotForeshadowing foreshadowing) {
        PlotForeshadowing created = plotForeshadowingService.createPlotForeshadowing(foreshadowing);
        return ResponseEntity.ok(created);
    }
    
    /**
     * 更新伏笔
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlotForeshadowing> updatePlotForeshadowing(@PathVariable Long id, @RequestBody PlotForeshadowing foreshadowing) {
        PlotForeshadowing updated = plotForeshadowingService.updatePlotForeshadowing(id, foreshadowing);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 删除伏笔
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlotForeshadowing(@PathVariable Long id) {
        plotForeshadowingService.deletePlotForeshadowing(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 标记伏笔为已触发
     */
    @PostMapping("/{id}/trigger")
    public ResponseEntity<PlotForeshadowing> triggerPlotForeshadowing(@PathVariable Long id, @RequestParam(required = false) Integer chapterNumber) {
        PlotForeshadowing triggered = plotForeshadowingService.triggerPlotForeshadowing(id, chapterNumber);
        return ResponseEntity.ok(triggered);
    }
    
    /**
     * 标记伏笔为已解决
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<PlotForeshadowing> resolvePlotForeshadowing(@PathVariable Long id, 
                                                                      @RequestParam(required = false) Integer chapterNumber,
                                                                      @RequestParam(required = false) String resolutionNote) {
        PlotForeshadowing resolved = plotForeshadowingService.resolvePlotForeshadowing(id, chapterNumber, resolutionNote);
        return ResponseEntity.ok(resolved);
    }
}