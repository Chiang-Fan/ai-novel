package com.aiwriter.service;

import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.OutlineNode;
import com.aiwriter.repository.OutlineNodeRepository;
import com.aiwriter.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 大纲自动提取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutlineExtractionService {
    
    private final OutlineNodeRepository outlineRepository;
    private final AiService aiService;
    
    /**
     * 异步提取大纲
     */
    @Async("autoExtractionExecutor")
    @Transactional
    public void extractAndSyncOutline(Chapter chapter, double similarityThreshold) {
        try {
            Long novelId = chapter.getNovelId();
            
            // 获取已有大纲
            List<OutlineNode> existingNodes = outlineRepository.findByNovelIdOrderBySortOrderAsc(novelId);
            
            // AI提取大纲信息
            String outlineTitle = "第" + chapter.getChapterNumber() + "章 - " + chapter.getTitle();
            String outlineContent = extractOutlineContent(chapter.getContent());
            
            // 计算相似度
            OutlineNode mostSimilar = null;
            double maxSimilarity = 0.0;
            
            for (OutlineNode node : existingNodes) {
                String nodeDesc = node.getTitle() + " " + (node.getContent() != null ? node.getContent() : "");
                double similarity = calculateSimilarity(outlineTitle, nodeDesc);
                
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilar = node;
                }
            }
            
            if (maxSimilarity >= similarityThreshold && mostSimilar != null) {
                // 更新已有大纲
                mostSimilar.setContent(outlineContent);
                mostSimilar.setIsCompleted(true);
                outlineRepository.save(mostSimilar);
                log.info("更新大纲: id={}, title={}, 相似度={}", 
                        mostSimilar.getId(), mostSimilar.getTitle(), maxSimilarity);
            } else {
                // 创建新大纲节点
                OutlineNode newNode = new OutlineNode();
                newNode.setNovelId(novelId);
                newNode.setTitle(outlineTitle);
                newNode.setContent(outlineContent);
                newNode.setNodeType("chapter");
                newNode.setLevel(1);
                newNode.setSortOrder(chapter.getChapterNumber());
                newNode.setIsCompleted(true);
                
                OutlineNode saved = outlineRepository.save(newNode);
                log.info("创建新大纲: id={}, title={}", saved.getId(), saved.getTitle());
            }
            
        } catch (Exception e) {
            log.error("大纲提取失败: chapterId={}", chapter.getId(), e);
        }
    }
    
    private String extractOutlineContent(String content) {
        // 简化实现：提取前200字作为摘要
        if (content == null || content.isEmpty()) return "";
        return content.substring(0, Math.min(200, content.length())) + "...";
    }
    
    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        return str1.contains(str2) || str2.contains(str1) ? 0.8 : 0.0;
    }
}
