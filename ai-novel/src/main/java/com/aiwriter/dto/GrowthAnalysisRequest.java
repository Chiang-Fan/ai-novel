package com.aiwriter.dto;

import lombok.Data;

@Data
public class GrowthAnalysisRequest {
    private Long characterId;
    private Long fromChapterId;
    private Long toChapterId;
}
