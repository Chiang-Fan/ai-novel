package com.aiwriter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场景字数目标DTO
 * 用于设置和管理场景的字数范围目标
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneWordCountTarget {
    
    /** 场景ID */
    private Long sceneId;
    
    /** 场景名称 */
    private String sceneName;
    
    /** 字数最小值 */
    private Integer minWords;
    
    /** 字数最大值 */
    private Integer maxWords;
    
    /** 目标平均字数 */
    private Integer targetAverageWords;
    
    /** 是否强制执行 */
    private Boolean enforceStrict;
    
    /** 备注说明 */
    private String notes;
}
