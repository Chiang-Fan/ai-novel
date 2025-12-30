package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

/**
 * 关系图谱响应DTO
 */
@Data
public class RelationshipGraphResponse {
    private List<RelationshipGraphNode> nodes;
    private List<RelationshipGraphEdge> edges;
}
