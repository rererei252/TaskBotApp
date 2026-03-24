package com.example.taskbot.ai.dto;

public record AiTaskRecommendationItem(
        Long taskId,
        String taskTitle,
        String recommendationType,
        String reason,
        String nextAction,
        Integer priorityScore
) {
}
