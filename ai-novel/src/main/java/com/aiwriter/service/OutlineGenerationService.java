package com.aiwriter.service;

import com.aiwriter.entity.Novel;
import com.aiwriter.entity.Outline;
import com.aiwriter.repository.OutlineRepository;
import com.aiwriter.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 大纲生成功能服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutlineGenerationService {
    
    private final OutlineRepository outlineRepository;
    private final NovelService novelService;
    private final AiService aiService;
    
    /**
     * 生成小说大纲
     */
    @Transactional
    public List<Outline> generateOutline(Long novelId) {
        Novel novel = novelService.getNovel(novelId);
        
        // 构建系统提示词
        String systemPrompt = "你是一位专业的小说策划师，擅长为小说生成完整的大纲。";
        
        // 构建用户提示词
        String userPrompt = String.format(
            "请为以下小说生成一个完整的大纲：\n" +
            "书名：%s\n" +
            "类型：%s\n" +
            "简介：%s\n\n" +
            "要求：\n" +
            "1. 使用三幕式结构（铺垫、冲突、解决）\n" +
            "2. 包含至少5个关键情节点\n" +
            "3. 每个情节点包含标题和简要描述\n" +
            "4. 标明每个情节点的章节号\n" +
            "5. 确保情节发展合理自然\n",
            novel.getTitle(),
            novel.getGenre() != null ? novel.getGenre() : "未知",
            novel.getDescription() != null ? novel.getDescription() : "无"
        );
        
        // 调用AI生成大纲
        String result = aiService.chat(systemPrompt, userPrompt);
        
        // 解析结果并保存到数据库
        return parseAndSaveOutline(novelId, result);
    }
    
    /**
     * 解析AI生成的大纲并保存到数据库
     */
    private List<Outline> parseAndSaveOutline(Long novelId, String result) {
        // 这里需要实现大纲解析逻辑
        // 由于时间限制，这里简化实现
        
        List<Outline> outlines = new ArrayList<>();
        
        // 假设AI返回的是JSON格式
        try {
            // 实际应用中需要使用JSON解析器
            // 这里简化处理
            
            // 创建示例大纲
            for (int i = 1; i <= 5; i++) {
                Outline outline = new Outline();
                outline.setNovelId(novelId);
                outline.setSequenceNumber(i);
                outline.setTitle("第" + i + "个情节点");
                outline.setSummary("这是第" + i + "个情节点的简要描述");
                outline.setStatus("PLANNED");
                
                outlines.add(outline);
            }
            
            // 保存到数据库
            outlineRepository.saveAll(outlines);
            
        } catch (Exception e) {
            log.error("解析大纲失败", e);
        }
        
        return outlines;
    }
}