package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

@Data
public class CharacterRecommendationResponse {
    private String name;
    private String roleType;
    private String gender;
    private Integer age;
    private String personality;
    private String background;
    private String appearance;
    private String abilities;
    private String motivation;
    private String arc;
    private Integer importanceLevel;
    private Boolean isGlobalProtagonist;
}
