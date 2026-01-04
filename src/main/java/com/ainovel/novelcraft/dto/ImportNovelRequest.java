package com.ainovel.novelcraft.dto;

import lombok.Data;

@Data
public class ImportNovelRequest {
    private String title;
    private String outline;
    private Boolean generateContent;
}