package com.aiwriter.service;

import com.aiwriter.config.AutoExtractionConfig;
import com.aiwriter.entity.Chapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自动提取服务 - 在章节保存后自动提取各类元数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoExtractionService {
    
    private final AutoExtractionConfig config;
    private final WritingStyleExtractionService writingStyleService;
    private final SceneExtractionService sceneExtractionService;
    private final OutlineExtractionService outlineExtractionService;
    private final PlotHookExtractionService plotHookService;
    private final CharacterExtractionService characterExtractionService;
    
    /**
     * 章节保存后的自动提取入口
     * @param chapter 已保存的章节
     */
    @Async("autoExtractionExecutor")
    @Transactional
    public void extractAllFromChapter(Chapter chapter) {
        if (!config.isEnabled()) {
            log.info("自动提取功能已禁用");
            return;
        }
        
        log.info("开始自动提取章节 {} 的元数据", chapter.getId());
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 提取文风特征
            if (config.getFeatures().isExtractWritingStyle()) {
                writingStyleService.extractAndSyncWritingStyle(chapter, 
                        config.getSimilarity().getWritingStyle());
            }
            
            // 2. 识别场景
            if (config.getFeatures().isExtractScene()) {
                sceneExtractionService.extractAndSyncScene(chapter, 
                        config.getSimilarity().getScene());
            }
            
            // 3. 生成大纲
            if (config.getFeatures().isExtractOutline()) {
                outlineExtractionService.extractAndSyncOutline(chapter, 
                        config.getSimilarity().getOutline());
            }
            
            // 4. 识别伏笔
            if (config.getFeatures().isExtractPlotHook()) {
                plotHookService.extractAndSyncPlotHook(chapter, 
                        config.getSimilarity().getPlotHook());
            }
            
            // 5. 提取角色
            if (config.getFeatures().isExtractCharacter()) {
                characterExtractionService.extractAndSyncCharacter(chapter, 
                        config.getSimilarity().getCharacter());
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("章节 {} 自动提取完成，耗时 {}ms", chapter.getId(), duration);
            
        } catch (Exception e) {
            log.error("章节 {} 自动提取失败", chapter.getId(), e);
            // 静默失败，不影响主流程
        }
    }
}
