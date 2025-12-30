package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关系响应DTO
 */
@Data
public class RelationshipResponse {
    private Long id;
    private Long characterId;
    private String characterName;
    private Long relatedCharacterId;
    private String relatedCharacterName;
    private String relationshipType;
    private String description;
    private Integer strength;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
