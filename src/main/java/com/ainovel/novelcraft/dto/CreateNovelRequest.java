package com.ainovel.novelcraft.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateNovelRequest {
    private String title;
    private String outline;
}