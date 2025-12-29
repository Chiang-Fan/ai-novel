package com.aiwriter.dto;

import lombok.Data;

@Data
public class SceneRecommendationRequest {
    private Long novelId;
    private Integer count = 3; // 推荐数量，默认3个
}
