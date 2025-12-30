package com.aiwriter.service;

import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.PlotHook;
import com.aiwriter.repository.PlotHookRepository;
import com.aiwriter.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 伏笔自动提取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlotHookExtractionService {
    
    private final PlotHookRepository plotHookRepository;
    private final AiService aiService;
    
    /**
     * 异步提取伏笔
     */
    @Async("autoExtractionExecutor")
    @Transactional
    public void extractAndSyncPlotHook(Chapter chapter, double similarityThreshold) {
        try {
            Long novelId = chapter.getNovelId();
            
            // 获取已有伏笔
            List<PlotHook> existingHooks = plotHookRepository.findByNovelIdOrderByPlantedInChapterAsc(novelId);
            
            // AI提取伏笔信息
            String hookTitle = detectForeshadowing(chapter.getContent());
            if (hookTitle == null || hookTitle.isEmpty()) {
                log.info("未检测到伏笔");
                return;
            }
            
            String hookDescription = "在第" + chapter.getChapterNumber() + "章检测到的伏笔";
            
            // 计算相似度
            PlotHook mostSimilar = null;
            double maxSimilarity = 0.0;
            
            for (PlotHook hook : existingHooks) {
                String hookDesc = hook.getTitle() + " " + hook.getDescription();
                double similarity = calculateSimilarity(hookTitle, hookDesc);
                
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilar = hook;
                }
            }
            
            if (maxSimilarity >= similarityThreshold && mostSimilar != null) {
                // 更新已有伏笔
                mostSimilar.setStatus(PlotHook.Status.HINTED);
                plotHookRepository.save(mostSimilar);
                log.info("更新伏笔: id={}, title={}, 相似度={}", 
                        mostSimilar.getId(), mostSimilar.getTitle(), maxSimilarity);
            } else {
                // 创建新伏笔
                PlotHook newHook = new PlotHook();
                newHook.setNovelId(novelId);
                newHook.setTitle(hookTitle);
                newHook.setDescription(hookDescription);
                newHook.setPlantedInChapter(chapter.getChapterNumber());
                newHook.setStatus(PlotHook.Status.PENDING);
                newHook.setType(PlotHook.ForeshadowingType.IMPLICIT);  // 使用setType而不是setForeshadowingType
                
                PlotHook saved = plotHookRepository.save(newHook);
                log.info("创建新伏笔: id={}, title={}", saved.getId(), saved.getTitle());
            }
            
        } catch (Exception e) {
            log.error("伏笔提取失败: chapterId={}", chapter.getId(), e);
        }
    }
    
    private String detectForeshadowing(String content) {
        try {
            // 使用AI分析章节内容，提取伏笔
            String systemPrompt = "你是一位专业的小说分析师，擅长识别故事中的伏笔。伏笔是指作者在前文埋下的暗示或线索，为后续情节发展做铺垫。";
            String userPrompt = String.format(
                "请分析以下章节内容，识别其中的伏笔。如果存在伏笔，请用10字以内简要概括伏笔的核心内容。如果没有明显伏笔，请返回\"无\"。\n\n章节内容：\n%s",
                content.length() > 1000 ? content.substring(0, 1000) : content
            );
            
            String result = aiService.chat(systemPrompt, userPrompt).trim();
            
            if ("无".equals(result) || result.isEmpty() || result.length() > 50) {
                log.info("未检测到明显伏笔或AI返回格式异常");
                return null;
            }
            
            return result;
        } catch (Exception e) {
            log.warn("AI伏笔检测失败，回退到关键词检测: {}", e.getMessage());
            // 回退：简化实现
            if (content.contains("神秘") || content.contains("奇怪")) return "神秘事件";
            if (content.contains("约定") || content.contains("承诺")) return "约定伏笔";
            if (content.contains("秘密") || content.contains("隐藏")) return "秘密线索";
            return null;
        }
    }
    
    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        return str1.contains(str2) || str2.contains(str1) ? 0.8 : 0.0;
    }
}
