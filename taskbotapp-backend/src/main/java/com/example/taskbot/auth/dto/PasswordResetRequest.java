package com.example.taskbot.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "メールアドレスは必須です。")
        @Email(message = "メールアドレスの形式が不正です。")
        String email
) {
}