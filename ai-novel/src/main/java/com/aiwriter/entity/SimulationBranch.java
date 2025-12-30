package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟分支实体
 */
@Data
@Entity
@Table(name = "simulation_branches")
public class SimulationBranch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "simulation_id", nullable = false)
    private Long simulationId;
    
    @Column(name = "parent_branch_id")
    private Long parentBranchId;
    
    @Column(name = "branch_name", nullable = false, length = 200)
    private String branchName;
    
    @Lob
    @Column(name = "decision_point", nullable = false, columnDefinition = "TEXT")
    private String decisionPoint;
    
    @Lob
    @Column(name = "decision_content", nullable = false, columnDefinition = "TEXT")
    private String decisionContent;
    
    @Lob
    @Column(name = "predicted_outcome", columnDefinition = "TEXT")
    private String predictedOutcome;
    
    @Lob
    @Column(name = "character_impact", columnDefinition = "TEXT")
    private String characterImpact;
    
    @Lob
    @Column(name = "plot_impact", columnDefinition = "TEXT")
    private String plotImpact;
    
    @Column(name = "probability_score", precision = 5, scale = 2)
    private BigDecimal probabilityScore;
    
    @Column(name = "quality_score", precision = 5, scale = 2)
    private BigDecimal qualityScore;
    
    @Column(name = "depth_level")
    private Integer depthLevel = 0;
    
    @Column(name = "is_ending")
    private Boolean isEnding = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
