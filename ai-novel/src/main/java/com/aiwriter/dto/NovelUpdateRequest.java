package com.aiwriter.dto;

import lombok.Data; /**
 * 小说更新请求DTO
 */
@Data
public class NovelUpdateRequest {
    private String title;
    private String description;
    private String genre;
    private String targetAudience;
    private String writingStyle;
    private String status;
}
