package com.aiwriter.dto;

import lombok.Data;
import java.util.List;

@Data
public class OutlineRecommendationResponse {
    private String nodeType;
    private String title;
    private String summary;
    private Integer targetWordCount;
    private List<String> keyEvents;
    private List<String> plotPoints;
    private List<String> themes;
}
