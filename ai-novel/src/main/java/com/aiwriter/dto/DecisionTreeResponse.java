package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

/**
 * 决策树响应
 */
@Data
public class DecisionTreeResponse {
    private SimulationResponse simulation;
    private List<TreeNode> nodes;
    private List<TreeEdge> edges;
    
    @Data
    public static class TreeNode {
        private Long id;
        private Long parentId;
        private String name;
        private String type; // ROOT, BRANCH, ENDING
        private String content;
        private Integer level;
        private Double score;
    }
    
    @Data
    public static class TreeEdge {
        private Long from;
        private Long to;
        private String label;
    }
}
