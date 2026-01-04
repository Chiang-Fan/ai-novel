package com.ainovel.novelcraft.dto;

import com.ainovel.novelcraft.entity.Chapter;
import lombok.Data;

@Data
public class CreateChapterResponse {
    private Chapter chapter;
    private boolean success;
    private String message;
}