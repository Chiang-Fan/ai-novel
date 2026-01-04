package com.ainovel.novelcraft.dto;

import lombok.Data;

@Data
public class GenerateChapterRequest {
    private Integer chapterNumber;
    private String direction;
    private String[] bannedElements;
    private String mood;
    private Boolean overrideHighStakes;
}