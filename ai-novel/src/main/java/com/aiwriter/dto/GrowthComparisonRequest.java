package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GrowthComparisonRequest {
    private Long characterId;
    private LocalDateTime timePoint1;
    private LocalDateTime timePoint2;
}
