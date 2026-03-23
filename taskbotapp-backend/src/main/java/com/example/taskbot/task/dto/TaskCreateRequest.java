package com.example.taskbot.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record TaskCreateRequest(
        @NotBlank(message = "タスク名は必須です。")
        @Size(max = 100, message = "タスク名は100文字以内で入力してください。")
        String title,

        @NotNull(message = "タスク開始日時は必須です。")
        OffsetDateTime startAt,

        @NotNull(message = "タスク締切日時は必須です。")
        OffsetDateTime dueAt,

        @NotBlank(message = "優先度は必須です。")
        @Pattern(regexp = "low|medium|high", message = "優先度は low / medium / high で入力してください。")
        String priority,

        @Pattern(regexp = "todo|doing|done", message = "ステータスは todo / doing / done で入力してください。")
        String status,

        @Size(max = 2000, message = "タスク詳細は2000文字以内で入力してください。")
        String detail,

        @Size(max = 2000, message = "開催場所URLは2000文字以内で入力してください。")
        String locationUrl
) {
}
