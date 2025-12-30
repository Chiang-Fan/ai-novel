package com.aiwriter.dto;

import lombok.Data;

/**
 * 关系历史请求DTO
 */
@Data
public class RelationshipHistoryRequest {
    private Long relationshipId;
    private String changeType;
    private Integer oldStrength;
    private Integer newStrength;
    private String oldType;
    private String newType;
    private String eventDesc;
    private Long relatedChapterId;
}
