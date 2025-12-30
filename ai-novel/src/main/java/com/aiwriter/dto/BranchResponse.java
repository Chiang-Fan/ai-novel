package com.aiwriter.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分支响应
 */
@Data
public class BranchResponse {
    private Long id;
    private Long simulationId;
    private Long parentBranchId;
    private String branchName;
    private String decisionPoint;
    private String decisionContent;
    private String predictedOutcome;
    private String characterImpact;
    private String plotImpact;
    private BigDecimal probabilityScore;
    private BigDecimal qualityScore;
    private Integer depthLevel;
    private Boolean isEnding;
    private LocalDateTime createdAt;
}
