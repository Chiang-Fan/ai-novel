package com.ai.novel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * AI智能小说创作系统 - Spring Boot启动类
 * 
 * @author AI Novel Writer Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class AiNovelApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiNovelApplication.class, args);
        System.out.println("""
            
            ========================================
            🚀 AI智能小说创作系统启动成功!
            ========================================
            📚 应用名称: AI Novel Writer
            🌐 访问地址: http://localhost:8080
            📖 API文档: http://localhost:8080/swagger-ui.html
            💚 健康检查: http://localhost:8080/actuator/health
            ========================================
            """);
    }
}
