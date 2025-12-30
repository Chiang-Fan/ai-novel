package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 节奏控制反馈DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaceFeedback {
    
    /** 章节ID */
    private Long chapterId;
    
    /** 章节号 */
    private Integer chapterNumber;
    
    /** 章节标题 */
    private String chapterTitle;
    
    /** 实际字数 */
    private Integer actualWordCount;
    
    /** 字数验证结果 */
    private WordCountValidation wordCountValidation;
    
    /** 节奏状态 (PACE_IDEAL/PACE_SLOW/PACE_FAST/PACE_UNSTABLE/DATA_INSUFFICIENT) */
    private String paceStatus;
    
    /** 内容质量分析 */
    private Map<String, Object> contentQuality;
    
    /** 改进建议 */
    private List<String> improvementSuggestions;
    
    /** 警告信息 */
    private List<String> warnings;
    
    /** 生成时间 */
    private Date generatedAt;
}
