package com.ai.novel.dto.response;

import com.ai.novel.entity.enums.CharacterImportance;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色响应
 */
@Data
public class CharacterResponse {
    
    private Long id;
    private Long novelId;
    private String name;
    private String description;
    private String personality;
    private String backstory;
    private CharacterImportance importance;
    private Integer age;
    private String gender;
    private String appearance;
    private String abilities;
    private String goals;
    private String currentState;
    private Integer appearanceCount;
    private LocalDateTime firstAppearance;
    private LocalDateTime lastAppearance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
