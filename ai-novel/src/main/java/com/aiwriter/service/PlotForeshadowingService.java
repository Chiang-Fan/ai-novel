package com.aiwriter.service;

import com.aiwriter.entity.PlotForeshadowing;
import com.aiwriter.entity.Novel;
import com.aiwriter.repository.PlotForeshadowingRepository;
import com.aiwriter.dto.PlotForeshadowingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 伏笔管理系统服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlotForeshadowingService {
    
    private final PlotForeshadowingRepository plotForeshadowingRepository;
    
    /**
     * 获取小说的所有伏笔
     */
    public List<PlotForeshadowing> getPlotForeshadowings(Long novelId) {
        return plotForeshadowingRepository.findByNovelIdOrderByPlantedInChapterAsc(novelId);
    }
    
    /**
     * 获取特定章节的伏笔
     */
    public List<PlotForeshadowing> getPlotForeshadowingsByChapter(Long chapterId) {
        return plotForeshadowingRepository.findByChapterId(chapterId);
    }
    
    /**
     * 创建新伏笔
     */
    @Transactional
    public PlotForeshadowing createPlotForeshadowing(PlotForeshadowing foreshadowing) {
        // 设置创建时间
        foreshadowing.setCreatedAt(java.time.LocalDateTime.now());
        foreshadowing.setUpdatedAt(java.time.LocalDateTime.now());
        return plotForeshadowingRepository.save(foreshadowing);
    }
    
    /**
     * 更新伏笔
     */
    @Transactional
    public PlotForeshadowing updatePlotForeshadowing(Long id, PlotForeshadowing foreshadowing) {
        PlotForeshadowing existing = plotForeshadowingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("伏笔不存在"));
        
        // 更新字段
        existing.setTitle(foreshadowing.getTitle());
        existing.setDescription(foreshadowing.getDescription());
        existing.setPlantedInChapter(foreshadowing.getPlantedInChapter());
        existing.setExpectedChapter(foreshadowing.getExpectedChapter());
        existing.setTriggeredInChapter(foreshadowing.getTriggeredInChapter());
        existing.setResolvedInChapter(foreshadowing.getResolvedInChapter());
        existing.setStatus(foreshadowing.getStatus());
        existing.setType(foreshadowing.getType());
        existing.setPriority(foreshadowing.getPriority());
        existing.setResolutionNote(foreshadowing.getResolutionNote());
        existing.setIsAutoDetected(foreshadowing.getIsAutoDetected());
        existing.setContentReference(foreshadowing.getContentReference());
        existing.setNotes(foreshadowing.getNotes());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        
        return plotForeshadowingRepository.save(existing);
    }
    
    /**
     * 删除伏笔
     */
    @Transactional
    public void deletePlotForeshadowing(Long id) {
        plotForeshadowingRepository.deleteById(id);
    }
    
    /**
     * 标记伏笔为已触发
     */
    @Transactional
    public PlotForeshadowing triggerPlotForeshadowing(Long id, Integer chapterNumber) {
        PlotForeshadowing foreshadowing = plotForeshadowingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("伏笔不存在"));
        
        foreshadowing.setStatus("TRIGGERED");
        foreshadowing.setTriggeredInChapter(chapterNumber);
        foreshadowing.setTriggeredAt(java.time.LocalDateTime.now());
        foreshadowing.setUpdatedAt(java.time.LocalDateTime.now());
        
        return plotForeshadowingRepository.save(foreshadowing);
    }
    
    /**
     * 标记伏笔为已解决
     */
    @Transactional
    public PlotForeshadowing resolvePlotForeshadowing(Long id, Integer chapterNumber, String resolutionNote) {
        PlotForeshadowing foreshadowing = plotForeshadowingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("伏笔不存在"));
        
        foreshadowing.setStatus("RESOLVED");
        foreshadowing.setResolvedInChapter(chapterNumber);
        foreshadowing.setResolutionNote(resolutionNote);
        foreshadowing.setResolvedAt(java.time.LocalDateTime.now());
        foreshadowing.setUpdatedAt(java.time.LocalDateTime.now());
        
        return plotForeshadowingRepository.save(foreshadowing);
    }
}