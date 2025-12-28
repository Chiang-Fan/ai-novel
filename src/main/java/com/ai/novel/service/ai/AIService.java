package com.ai.novel.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI服务 - Spring AI封装
 * 提供统一的AI调用接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * 调用AI生成文本
     *
     * @param systemMessage 系统消息
     * @param userPrompt    用户提示词
     * @param temperature   温度参数(0-1,越高越随机)
     * @param maxTokens     最大生成token数
     * @return AI生成的文本
     */
    public String chat(String systemMessage, String userPrompt, Double temperature, Integer maxTokens) {
        try {
            log.debug("调用AI服务 - 系统消息长度: {}, 提示词长度: {}", 
                     systemMessage != null ? systemMessage.length() : 0, 
                     userPrompt.length());

            String response = chatClient.prompt()
                    .system(systemMessage != null ? systemMessage : "你是一位专业的小说作家。")
                    .user(userPrompt)
                    .options(OpenAiChatOptions.builder()
                            .withTemperature(temperature != null ? temperature : 0.7)
                            .withMaxTokens(maxTokens != null ? maxTokens : 4000)
                            .build())
                    .call()
                    .content();

            log.debug("AI响应长度: {}", response != null ? response.length() : 0);
            return response;

        } catch (Exception e) {
            log.error("AI服务调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用AI生成文本(使用默认参数)
     */
    public String chat(String userPrompt) {
        return chat("你是一位专业的小说作家。", userPrompt, 0.7, 4000);
    }

    /**
     * 调用AI并解析JSON响应
     *
     * @param systemMessage 系统消息
     * @param userPrompt    用户提示词
     * @param temperature   温度参数
     * @param maxTokens     最大token数
     * @return 解析后的JSON节点
     */
    public JsonNode chatJson(String systemMessage, String userPrompt, Double temperature, Integer maxTokens) {
        String response = chat(systemMessage, userPrompt, temperature, maxTokens);
        return parseJsonFromResponse(response);
    }

    /**
     * 从AI响应中提取并解析JSON
     * 支持处理markdown代码块包裹的JSON
     */
    private JsonNode parseJsonFromResponse(String response) {
        try {
            // 移除可能的markdown代码块标记
            String jsonText = response.trim();

            // 尝试匹配```json ... ```格式
            Pattern jsonCodeBlock = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
            Matcher matcher = jsonCodeBlock.matcher(jsonText);
            if (matcher.find()) {
                jsonText = matcher.group(1).trim();
            } else {
                // 尝试匹配``` ... ```格式
                Pattern codeBlock = Pattern.compile("```\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
                matcher = codeBlock.matcher(jsonText);
                if (matcher.find()) {
                    jsonText = matcher.group(1).trim();
                }
            }

            // 如果没有找到代码块,尝试提取第一个{到最后一个}之间的内容
            if (!jsonText.startsWith("{") && !jsonText.startsWith("[")) {
                int start = jsonText.indexOf('{');
                int end = jsonText.lastIndexOf('}');
                if (start != -1 && end != -1 && end > start) {
                    jsonText = jsonText.substring(start, end + 1);
                }
            }

            return objectMapper.readTree(jsonText);

        } catch (JsonProcessingException e) {
            log.error("JSON解析失败,原始响应: {}", response, e);
            throw new RuntimeException("AI返回的内容不是有效的JSON格式: " + e.getMessage() + 
                                     "\n原始响应: " + response);
        }
    }

    /**
     * 批量调用AI(用于生成多个建议)
     */
    public String chatWithHigherTemperature(String systemMessage, String userPrompt, Integer maxTokens) {
        return chat(systemMessage, userPrompt, 0.9, maxTokens);
    }

    /**
     * 调用AI进行内容分析(使用较低温度以提高准确性)
     */
    public JsonNode analyzeContent(String systemMessage, String userPrompt) {
        return chatJson(systemMessage, userPrompt, 0.3, 2000);
    }
}
