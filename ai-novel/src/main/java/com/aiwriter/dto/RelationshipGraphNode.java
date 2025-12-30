package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

/**
 * 关系图谱节点DTO
 */
@Data
public class RelationshipGraphNode {
    private Long id;
    private String name;
    private String roleType;
    private Integer importance;  // 重要性，影响节点大小
    private String category;  // 节点分类
}
