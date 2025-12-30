package com.aiwriter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 氛围模板响应
 */
@Data
@Builder
public class AtmosphereTemplateResponse {
    private Long id;
    private String name;
    private String category;
    private String atmosphereType;
    private String description;
    private String keywords;
    private String sensoryDetails;
    private String exampleText;
    private Integer usageCount;
    private Boolean isSystem;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
