package com.ai.novel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理 - 简化版本
     * 只处理 /static/** 路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理 /static/** 下的资源（JS、CSS、Images等）
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/")
                .setCachePeriod(3600);
        
        // 处理根目录其他资源（CSS、JS等直接在static根目录的文件）
        registry.addResourceHandler("/*.css", "/*.js", "/*.svg", "/*.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
