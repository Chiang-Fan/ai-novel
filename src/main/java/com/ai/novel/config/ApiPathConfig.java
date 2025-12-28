package com.ai.novel.config;

import org.springframework.context.annotation.Configuration;

/**
 * API路径优先级配置
 * 根本问题已通过application.yml中的static-path-pattern配置解决
 */
@Configuration
public class ApiPathConfig {
    // 此类保留以避免破坏现有导入，但不需要任何配置
}