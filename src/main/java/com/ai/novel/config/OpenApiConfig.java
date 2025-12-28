package com.ai.novel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI小说写作助手 API")
                        .version("1.0.0")
                        .description("AI驱动的小说创作和管理系统")
                        .contact(new Contact()
                                .name("AI Novel Writer")
                                .email("support@ainovel.com")));
    }
}