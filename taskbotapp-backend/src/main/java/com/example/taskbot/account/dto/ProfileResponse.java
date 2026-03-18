package com.example.taskbot.account.dto;

public record ProfileResponse(
        Long userId,
        String username,
        String email,
        String profileMessage,
        String profileImageUrl
) {
}
