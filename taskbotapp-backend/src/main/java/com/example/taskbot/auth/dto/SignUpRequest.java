package com.example.taskbot.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "ユーザー名は必須です")
        @Size(max = 50, message = "ユーザー名は50文字以内で入力してください")
        String username,

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレス形式が不正です")
        @Size(max = 255, message = "メールアドレスが長すぎます")
        String email,

        @NotBlank(message = "パスワードは必須です")
        @Size(min = 8, max = 20, message = "パスワードは8〜20文字で入力してください")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d._-]{8,20}$",
                message = "パスワードは英字と数字を含み、使用可能記号は . _ - のみです")
        String password
) {
}
