package com.aiwriter.dto;

import lombok.Data;

/**
 * 创建/更新关系请求DTO
 */
@Data
public class RelationshipRequest {
    private Long characterId;
    private Long relatedCharacterId;
    private String relationshipType;
    private String description;
    private Integer strength;
}
