package com.aiwriter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 情节推演模拟实体
 */
@Data
@Entity
@Table(name = "plot_simulations")
public class PlotSimulation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "simulation_name", nullable = false, length = 200)
    private String simulationName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Lob
    @Column(name = "starting_point", nullable = false, columnDefinition = "TEXT")
    private String startingPoint;
    
    @Lob
    @Column(name = "current_state", columnDefinition = "TEXT")
    private String currentState;
    
    @Column(name = "status", length = 50)
    private String status = "ACTIVE"; // ACTIVE, ARCHIVED, COMPLETED
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
