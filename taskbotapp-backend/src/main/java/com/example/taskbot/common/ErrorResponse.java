package com.example.taskbot.common;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        OffsetDateTime timestamp
) {
}
