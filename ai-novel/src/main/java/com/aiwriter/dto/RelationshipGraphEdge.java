package com.aiwriter.dto;

import lombok.Data;

/**
 * 关系图谱边DTO
 */
@Data
public class RelationshipGraphEdge {
    private Long id;
    private Long source;  // 源节点ID
    private Long target;  // 目标节点ID
    private String type;  // 关系类型
    private Integer strength;  // 关系强度
    private String description;
}
