package com.example.taskbot.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "トークンは必須です。")
        @Size(max = 255, message = "トークンが不正です。")
        String token,

        @NotBlank(message = "新しいパスワードは必須です。")
        @Size(min = 8, max = 20, message = "パスワードは8〜20文字で入力してください。")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d._-]{8,20}$",
                message = "パスワードは英字・数字を含み、使用可能記号は . - _ のみです。"
        )
        String newPassword
) {
}