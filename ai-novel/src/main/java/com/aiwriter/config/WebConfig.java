package com.aiwriter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置CORS、静态资源、视图控制器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // 1天缓存
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将所有非API、非静态资源请求都转发到index.html（支持Vue Router的history模式）
        registry.addViewController("/")
                .setViewName("forward:/index.html");
        // 排除静态资源文件（包含点号的路径）
        registry.addViewController("/{spring:\\w+}")
                .setViewName("forward:/index.html");
    }
}