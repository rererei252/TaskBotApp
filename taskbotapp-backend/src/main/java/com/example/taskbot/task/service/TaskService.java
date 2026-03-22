package com.example.taskbot.task.service;

import com.example.taskbot.auth.entity.User;
import com.example.taskbot.auth.exception.AuthException;
import com.example.taskbot.auth.repository.UserRepository;
import com.example.taskbot.task.dto.TaskCreateRequest;
import com.example.taskbot.task.dto.TaskResponse;
import com.example.taskbot.task.entity.Task;
import com.example.taskbot.task.repository.TaskRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskCreateRequest request) {
        validateTaskRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("ユーザーが見つかりません。"));

        OffsetDateTime now = OffsetDateTime.now();
        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.title().trim());
        task.setStartAt(request.startAt());
        task.setDueAt(request.dueAt());
        task.setPriority(request.priority().trim().toLowerCase());
        task.setStatus("todo");
        task.setDetail(normalizeNullable(request.detail()));
        task.setLocationUrl(normalizeNullable(request.locationUrl()));
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskCreateRequest request) {
        validateTaskRequest(request);

        Task task = taskRepository.findOwnedTask(taskId, userId)
                .orElseThrow(() -> new AuthException("対象のタスクが見つかりません。"));

        task.setTitle(request.title().trim());
        task.setStartAt(request.startAt());
        task.setDueAt(request.dueAt());
        task.setPriority(request.priority().trim().toLowerCase());
        task.setDetail(normalizeNullable(request.detail()));
        task.setLocationUrl(normalizeNullable(request.locationUrl()));
        task.setUpdatedAt(OffsetDateTime.now());

        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMonthlyTasks(Long userId, int year, int month) {
        if (year < 2000 || year > 2100) {
            throw new AuthException("年の指定が不正です。");
        }
        if (month < 1 || month > 12) {
            throw new AuthException("月の指定が不正です。");
        }

        ZoneId zone = ZoneId.of("Asia/Tokyo");
        OffsetDateTime from = LocalDate.of(year, month, 1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime to = from.plusMonths(1);

        return taskRepository.findMonthlyTasks(userId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateTaskRequest(TaskCreateRequest request) {
        if (request.dueAt().isBefore(request.startAt())) {
            throw new AuthException("タスク締切日時は開始日時以降で入力してください。");
        }

        if (request.locationUrl() != null && !request.locationUrl().isBlank()) {
            String url = request.locationUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new AuthException("開催場所URLは http:// または https:// で入力してください。");
            }
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getUser().getId(),
                task.getTitle(),
                task.getStartAt(),
                task.getDueAt(),
                task.getPriority(),
                task.getStatus(),
                task.getDetail(),
                task.getLocationUrl(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
