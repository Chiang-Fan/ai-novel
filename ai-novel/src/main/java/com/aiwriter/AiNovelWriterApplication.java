package com.aiwriter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI智能小说创作系统 - 主应用入口
 * 
 * @author AI Novel Writer Team
 * @version 2.1.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class AiNovelWriterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiNovelWriterApplication.class, args);
        System.out.println("""
                
                ========================================
                🎉 AI智能小说创作系统启动成功！
                ========================================
                📚 前端页面: http://localhost:8080
                📖 API文档: http://localhost:8080/docs
                🔍 健康检查: http://localhost:8080/actuator/health
                ========================================
                """);
    }
}
