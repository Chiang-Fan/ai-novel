package com.aiwriter.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 * 配置通义千问API参数
 */
@Configuration
@ConfigurationProperties(prefix = "ai.qianwen")
public class AiConfig {
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * 模型名称
     */
    private String model = "qwen-plus";
    
    /**
     * 温度参数（0-1）
     */
    private Double temperature = 0.7;
    
    /**
     * 最大token数
     */
    private Integer maxTokens = 4000;
    
    /**
     * 超时时间（毫秒）
     */
    private Long timeout = 120000L;

    // Getters and Setters
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    
    public Long getTimeout() { return timeout; }
    public void setTimeout(Long timeout) { this.timeout = timeout; }
}
