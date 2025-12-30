package com.aiwriter.dto;

import lombok.Data;

/**
 * 版本统计响应
 */
@Data
public class VersionStatistics {
    private Long chapterId;
    private Integer totalVersions;
    private Integer currentVersionNumber;
    private Integer totalEdits;
    private Integer totalWordsAdded;
    private Integer totalWordsDeleted;
    private Double averageEditSize;
    private String mostActiveEditTime;
}
