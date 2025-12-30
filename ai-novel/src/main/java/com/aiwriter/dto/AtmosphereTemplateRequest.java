package com.aiwriter.dto;

import lombok.Data;

import java.util.List;

/**
 * 氛围模板请求
 */
@Data
public class AtmosphereTemplateRequest {
    private String name;
    private String category;  // TIME, WEATHER, EMOTION, ACTION
    private String atmosphereType;
    private String description;
    private List<String> keywords;
    private SensoryDetails sensoryDetails;
    private String exampleText;
    
    @Data
    public static class SensoryDetails {
        private String visual;    // 视觉
        private String auditory;  // 听觉
        private String olfactory; // 嗅觉
        private String tactile;   // 触觉
        private String gustatory; // 味觉
    }
}
