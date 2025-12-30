package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字数验证DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordCountValidation {
    
    /** 章节ID */
    private Long chapterId;
    
    /** 实际字数 */
    private Integer actualWordCount;
    
    /** 最小目标字数 */
    private Integer minTarget;
    
    /** 最大目标字数 */
    private Integer maxTarget;
    
    /** 理想目标字数 */
    private Integer idealTarget;
    
    /** 验证状态 (ACCEPTABLE/TOO_SHORT/TOO_LONG) */
    private String status;
    
    /** 偏差值 */
    private Integer deviation;
    
    /** 验证信息 */
    private String message;
}
