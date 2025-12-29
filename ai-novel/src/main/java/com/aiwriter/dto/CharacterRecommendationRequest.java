package com.aiwriter.dto;

import lombok.Data;

@Data
public class CharacterRecommendationRequest {
    private Long novelId;
    private Integer count = 3; // 推荐数量，默认3个
}
