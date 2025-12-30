package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

/**
 * 风格模板 DTO
 */
@Data
public class StyleTemplateDto {
    private String id;
    private String name;
    private String description;
    private String perspective;
    private String tone;
    private String sentenceStyle;
    private List<String> keywords;
    private String descriptionDensity;
    private String example;
}
