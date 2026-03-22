package com.example.taskbot.task.dto;

import java.time.OffsetDateTime;

public record TaskResponse(
        Long id,
        Long userId,
        String title,
        OffsetDateTime startAt,
        OffsetDateTime dueAt,
        String priority,
        String status,
        String detail,
        String locationUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
