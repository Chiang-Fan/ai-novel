package com.aiwriter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 * 配置Swagger文档
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI智能小说创作系统 API")
                        .version("2.1.0")
                        .description("""
                                AI智能小说创作系统的RESTful API文档
                                
                                核心功能：
                                - 小说管理：创建、编辑、删除小说
                                - AI智能续写：基于上下文的章节生成
                                - 场景管理：场景规划和进度追踪
                                - 大纲树视图：多层级大纲结构管理
                                - 角色管理：完整角色信息和关系网络
                                - 智能建议：场景、大纲、情节、关系推荐
                                - 编辑历史：完整操作记录和撤销功能
                                """)
                        .contact(new Contact()
                                .name("AI Novel Writer Team")
                                .email("support@aiwriter.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
