package com.example.taskbot.auth.dto;

import java.time.OffsetDateTime;

public record AuthResponse(
        Long userId,
        String username,
        String email,
        OffsetDateTime lastLoginAt,
        String message
) {
}
