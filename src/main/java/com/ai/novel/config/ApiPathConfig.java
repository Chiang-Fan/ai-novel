package com.ai.novel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API路径优先级配置
 * 确保API路径不被静态资源处理器拦截
 */
@Configuration
public class ApiPathConfig implements WebMvcConfigurer, Ordered {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 确保API路径优先级高于静态资源
        // 这里不做任何处理，只是为了确保顺序
    }

    @Override
    public int getOrder() {
        // 设置最高优先级，确保在静态资源处理之前
        return Ordered.HIGHEST_PRECEDENCE;
    }
}