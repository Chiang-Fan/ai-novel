package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CreateGrowthRecordRequest {
    private Long characterId;
    private Long chapterId;
    private LocalDateTime recordTime;
    private Map<String, Object> attributes;
    private String notes;
}
