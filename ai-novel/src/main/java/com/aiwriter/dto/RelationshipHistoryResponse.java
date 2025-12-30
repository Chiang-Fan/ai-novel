package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关系历史响应DTO
 */
@Data
public class RelationshipHistoryResponse {
    private Long id;
    private Long relationshipId;
    private String changeType;
    private Integer oldStrength;
    private Integer newStrength;
    private String oldType;
    private String newType;
    private String eventDesc;
    private Long relatedChapterId;
    private LocalDateTime changeTime;
}
