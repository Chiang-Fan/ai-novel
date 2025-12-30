package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

/**
 * 版本对比响应
 */
@Data
public class CompareVersionResponse {
    private VersionInfo versionFrom;
    private VersionInfo versionTo;
    private DiffStatistics statistics;
    private List<DiffBlock> diffs;
    
    @Data
    public static class VersionInfo {
        private Long id;
        private Integer versionNumber;
        private String versionTag;
        private Integer wordCount;
    }
    
    @Data
    public static class DiffStatistics {
        private Integer addedCount;
        private Integer deletedCount;
        private Integer modifiedCount;
        private Integer unchangedCount;
        private Integer totalChanges;
        private Integer wordDifference;
    }
    
    @Data
    public static class DiffBlock {
        private String type; // EQUAL, INSERT, DELETE
        private String content;
        private Integer lineNumber;
    }
}
