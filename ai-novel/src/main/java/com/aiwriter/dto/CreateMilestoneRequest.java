package com.aiwriter.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateMilestoneRequest {
    private Long characterId;
    private Long chapterId;
    private String milestoneType; // POSITIVE, NEGATIVE, NEUTRAL
    private String eventName;
    private String description;
    private Integer impactLevel; // 1-10
    private Map<String, Integer> affectedAttributes;
}
