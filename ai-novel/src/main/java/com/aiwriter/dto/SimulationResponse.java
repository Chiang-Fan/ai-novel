package com.aiwriter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模拟响应
 */
@Data
public class SimulationResponse {
    private Long id;
    private Long novelId;
    private Long chapterId;
    private String simulationName;
    private String description;
    private String startingPoint;
    private String status;
    private Integer branchCount;
    private Integer endingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
