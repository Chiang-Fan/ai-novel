package com.aiwriter.dto;

import lombok.Data;

/**
 * 创建分支请求
 */
@Data
public class CreateBranchRequest {
    private Long simulationId;
    private Long parentBranchId;
    private String branchName;
    private String decisionPoint;
    private String decisionContent;
    private Boolean isEnding;
}
