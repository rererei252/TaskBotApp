package com.example.taskbot.ai.dto;

import java.util.List;

public record AiTaskRecommendationResponse(
        String model,
        List<AiTaskRecommendationItem> recommendations
) {
}
