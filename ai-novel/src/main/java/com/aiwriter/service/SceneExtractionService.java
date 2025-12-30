package com.aiwriter.service;

import com.aiwriter.entity.Chapter;
import com.aiwriter.repository.SceneRepository;
import com.aiwriter.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 场景自动提取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneExtractionService {
    
    private final SceneRepository sceneRepository;
    private final AiService aiService;
    
    /**
     * 异步提取场景
     */
    @Async("autoExtractionExecutor")
    @Transactional
    public void extractAndSyncScene(Chapter chapter, double similarityThreshold) {
        try {
            Long novelId = chapter.getNovelId();
            
            // 获取已有场景
            List<com.aiwriter.entity.Scene> existingScenes = 
                    sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
            
            // AI提取场景信息
            String sceneName = extractSceneName(chapter.getContent());
            if ("未知场景".equals(sceneName)) {
                log.info("未识别到明确场景，跳过创建");
                return;
            }
            
            String sceneType = detectSceneType(chapter.getContent());
            
            // 计算相似度
            com.aiwriter.entity.Scene mostSimilar = null;
            double maxSimilarity = 0.0;
            
            for (com.aiwriter.entity.Scene scene : existingScenes) {
                double similarity = calculateSimilarity(sceneName, scene.getName());
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilar = scene;
                }
            }
            
            if (maxSimilarity >= similarityThreshold && mostSimilar != null) {
                // 更新已有场景（增加notes记录）
                String notes = mostSimilar.getNotes() != null ? mostSimilar.getNotes() : "";
                mostSimilar.setNotes(notes + "\n[重复出现] 第" + chapter.getChapterNumber() + "章");
                sceneRepository.save(mostSimilar);
                log.info("更新场景: id={}, name={}, 相似度={}", 
                        mostSimilar.getId(), mostSimilar.getName(), maxSimilarity);
            } else {
                // 创建新场景（这里简化处理，实际应调用SceneService）
                log.info("发现新场景: name={}, type={}", sceneName, sceneType);
            }
            
        } catch (Exception e) {
            log.error("场景提取失败: chapterId={}", chapter.getId(), e);
        }
    }
    
    private String extractSceneName(String content) {
        try {
            // 使用AI识别场景
            String systemPrompt = "你是一位专业的小说分析师，擅长识别故事场景。";
            String userPrompt = String.format(
                "请识别以下章节内容的主要场景地点，用2-5个字简要概括场景名称。例如：皇宫、客栈、森林、山洞等。\n\n章节内容：\n%s",
                content.length() > 800 ? content.substring(0, 800) : content
            );
            
            String result = aiService.chat(systemPrompt, userPrompt).trim();
            
            if (result.isEmpty() || result.length() > 20) {
                log.info("AI场景识别返回异常，使用默认场景");
                return fallbackExtractSceneName(content);
            }
            
            return result;
        } catch (Exception e) {
            log.warn("AI场景识别失败，回退到关键词检测: {}", e.getMessage());
            return fallbackExtractSceneName(content);
        }
    }
    
    private String fallbackExtractSceneName(String content) {
        // 简化实现：查找常见场景关键词
        if (content.contains("客栈") || content.contains("酒楼")) return "客栈";
        if (content.contains("山洞") || content.contains("洞穴")) return "山洞";
        if (content.contains("森林") || content.contains("树林")) return "森林";
        if (content.contains("宫殿") || content.contains("皇宫")) return "宫殿";
        return "未知场景";
    }
    
    private String detectSceneType(String content) {
        try {
            // 使用AI识别场景类型
            String systemPrompt = "你是一位专业的小说分析师，擅长识别场景类型。";
            String userPrompt = String.format(
                "请识别以下章节内容的场景类型（战斗/对话/内心独白/描述/其他），只返回类型名称：\n\n%s",
                content.length() > 600 ? content.substring(0, 600) : content
            );
            
            String result = aiService.chat(systemPrompt, userPrompt).trim();
            
            if (result.isEmpty() || result.length() > 10) {
                return fallbackDetectSceneType(content);
            }
            
            return result;
        } catch (Exception e) {
            log.warn("AI场景类型识别失败: {}", e.getMessage());
            return fallbackDetectSceneType(content);
        }
    }
    
    private String fallbackDetectSceneType(String content) {
        if (content.contains("战斗") || content.contains("打斗")) return "战斗";
        if (content.contains("对话") || content.contains("交谈")) return "对话";
        return "普通";
    }
    
    private double calculateSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) return 0.0;
        return name1.equals(name2) ? 1.0 : 0.0;
    }
}
