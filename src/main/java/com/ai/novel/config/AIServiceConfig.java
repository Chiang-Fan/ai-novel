package com.ai.novel.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置
 */
@Configuration
public class AIServiceConfig {

    /**
     * 创建ChatClient Bean
     * Spring AI会自动注入配置好的OpenAiChatModel
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
