package com.ainovel.novelcraft.service;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    
    @Autowired
    private OpenAiChatClient chatClient;
    
    public String generateChapterContent(String prompt) {
        try {
            // 使用Spring AI的OpenAI ChatClient生成内容
            return chatClient.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
        } catch (Exception e) {
            throw new RuntimeException("AI章节生成失败: " + e.getMessage(), e);
        }
    }
}