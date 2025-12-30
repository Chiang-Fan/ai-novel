package com.aiwriter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文本导入请求DTO
 */
@Data
public class TextImportRequest {
    /**
     * 小说ID
     */
    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /**
     * 是否自动分章
     */
    private Boolean autoChapterize = true;

    /**
     * 是否提取关键要素
     */
    private Boolean extractKeyElements = true;
}
