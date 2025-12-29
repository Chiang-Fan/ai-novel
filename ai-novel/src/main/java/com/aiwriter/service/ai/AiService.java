package com.aiwriter.service.ai;

import com.aiwriter.config.AiConfig;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * AI服务 - 通义千问集成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
    
    private final AiConfig aiConfig;
    
    /**
     * 调用通义千问API - 文本生成
     * 
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return AI生成的文本
     */
    public String chat(String systemPrompt, String userPrompt) {
        try {
            // 构建消息列表
            List<Message> messages = Arrays.asList(
                Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build(),
                Message.builder()
                    .role(Role.USER.getValue())
                    .content(userPrompt)
                    .build()
            );
            
            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                .apiKey(aiConfig.getApiKey())
                .model(aiConfig.getModel())
                .messages(messages)
                .temperature(aiConfig.getTemperature().floatValue())
                .maxTokens(aiConfig.getMaxTokens())
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
            
            // 调用API
            Generation generation = new Generation();
            GenerationResult result = generation.call(param);
            
            // 提取返回内容
            String content = result.getOutput().getChoices().get(0)
                .getMessage().getContent();
            
            // log.info("AI调用成功，生成内容长度: {}", content.length());
            return content;
            
        } catch (NoApiKeyException e) {
            // log.error("API Key未配置", e);
            throw new RuntimeException("AI服务配置错误：未找到API Key", e);
        } catch (ApiException | InputRequiredException e) {
            // log.error("AI调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用通义千问API - JSON格式返回
     * 用于智能推荐等结构化数据生成
     * 
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return JSON格式的AI响应
     */
    public String chatJson(String systemPrompt, String userPrompt) {
        String enhancedSystemPrompt = systemPrompt + "\n\n请以JSON格式返回结果。";
        return chat(enhancedSystemPrompt, userPrompt);
    }
    
    /**
     * 提取章节内容（去除XML标签等）
     * 
     * @param aiResponse AI原始响应
     * @return 清理后的内容
     */
    public String extractContent(String aiResponse) {
        if (aiResponse == null || aiResponse.isEmpty()) {
            return "";
        }
        
        // 去除可能的XML标签
        String content = aiResponse
            .replaceAll("<chapter>.*?</chapter>", "")
            .replaceAll("<content>|</content>", "")
            .trim();
        
        return content;
    }
    
    /**
     * 统计字数（中文字符）
     * 
     * @param text 文本内容
     * @return 字数
     */
    public int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // 去除空格和换行符
        String cleaned = text.replaceAll("\\s+", "");
        
        // 统计中文字符数（包括中文标点）
        int chineseChars = 0;
        for (char c : cleaned.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
                chineseChars++;
            }
        }
        
        return chineseChars;
    }
}
