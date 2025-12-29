package com.aiwriter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data; /**
 * AI续写请求DTO
 */
@Data
public class ChapterContinueRequest {
    
    private Long novelId;
    private Long sceneId;
    
    @Size(max = 1000, message = "续写方向描述不能超过1000字")
    private String direction;
    
    @Min(value = 500, message = "目标字数不能少于500")
    @Max(value = 10000, message = "目标字数不能超过10000")
    private Integer targetWordCount = 2000;
}
