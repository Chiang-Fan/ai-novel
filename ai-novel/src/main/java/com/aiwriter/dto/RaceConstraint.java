package com.aiwriter.dto;

import lombok.Data;

/**
 * 种族约束条件
 */
@Data
public class RaceConstraint {
    
    /**
     * 种族名称
     */
    private String raceName;
    
    /**
     * 种族特性描述
     */
    private String characteristics;
    
    /**
     * 种族能力限制
     */
    private String abilityLimitations;
    
    /**
     * 种族文化特征
     */
    private String culturalTraits;
    
    /**
     * 与其他种族的关系
     */
    private String interracialRelations;
}