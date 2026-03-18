package com.example.taskbot.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "ユーザー名は必須です。")
        @Size(max = 20, message = "ユーザー名は20文字以内で入力してください。")
        @Pattern(
                regexp = "^[\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}A-Za-z\\s]+$",
                message = "ユーザー名は日本語・英字のみ入力できます。"
        )
        String username,
        @NotBlank(message = "メールアドレスは必須です。")
        @Email(message = "メールアドレスの形式が不正です。")
        @Size(max = 255, message = "メールアドレスは255文字以内で入力してください。")
        String email,
        @Size(max = 1000, message = "メッセージは1000文字以内で入力してください。")
        String profileMessage,
        @Size(max = 500, message = "画像URLは500文字以内で入力してください。")
        String profileImageUrl
) {
}
