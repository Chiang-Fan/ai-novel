package com.aiwriter.dto;

import lombok.Data;

/**
 * 创建模拟请求
 */
@Data
public class CreateSimulationRequest {
    private Long novelId;
    private Long chapterId;
    private String simulationName;
    private String description;
    private String startingPoint;
}
