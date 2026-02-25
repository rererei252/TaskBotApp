package com.example.taskbot.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレス形式が不正です")
        String email,

        @NotBlank(message = "パスワードは必須です")
        String password
) {
}
