package com.aiwriter.dto;

import lombok.Data;

@Data
public class OutlineRecommendationRequest {
    private Long novelId;
    private Long parentId; // 父节点ID，用于推荐子节点
    private String nodeType; // 要推荐的节点类型
    private Integer count = 3; // 推荐数量，默认3个
}
