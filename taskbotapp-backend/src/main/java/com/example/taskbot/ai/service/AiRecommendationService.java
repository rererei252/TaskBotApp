package com.example.taskbot.ai.service;

import com.example.taskbot.ai.dto.AiTaskRecommendationItem;
import com.example.taskbot.ai.dto.AiTaskRecommendationResponse;
import com.example.taskbot.auth.exception.AuthException;
import com.example.taskbot.task.entity.Task;
import com.example.taskbot.task.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(AiRecommendationService.class);
    private static final ZoneId TOKYO_ZONE = ZoneId.of("Asia/Tokyo");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_REASON_LENGTH = 60;
    private static final int MAX_NEXT_ACTION_LENGTH = 30;

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String openAiApiKey;
    private final String openAiModel;
    private final String openAiBaseUrl;
    private final int recommendationWindowDays;

    public AiRecommendationService(
            TaskRepository taskRepository,
            ObjectMapper objectMapper,
            @Value("${app.ai.openai.api-key:}") String openAiApiKey,
            @Value("${app.ai.openai.model:gpt-5-mini}") String openAiModel,
            @Value("${app.ai.openai.base-url:https://api.openai.com/v1}") String openAiBaseUrl,
            @Value("${app.ai.recommendation-window-days:14}") int recommendationWindowDays
    ) {
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
        this.openAiApiKey = openAiApiKey;
        this.openAiModel = openAiModel;
        this.openAiBaseUrl = openAiBaseUrl;
        this.recommendationWindowDays = recommendationWindowDays;
        this.httpClient = HttpClient.newHttpClient();
    }

    public AiTaskRecommendationResponse getTaskRecommendations(Long userId, LocalDate date) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new AuthException(
                    "\u004f\u0070\u0065\u006e\u0041\u0049\u0020\u0041\u0050\u0049\u30ad\u30fc\u304c\u8a2d\u5b9a\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002"
                            + "\u0020\u0061\u0070\u0070\u006c\u0069\u0063\u0061\u0074\u0069\u006f\u006e\u002d\u006c\u006f\u0063\u0061\u006c\u002e\u0070\u0072\u006f\u0070\u0065\u0072\u0074\u0069\u0065\u0073"
                            + "\u0020\u3092\u78ba\u8a8d\u3057\u3066\u304f\u3060\u3055\u3044\u3002"
            );
        }

        OffsetDateTime from = date.atStartOfDay(TOKYO_ZONE).toOffsetDateTime();
        OffsetDateTime to = from.plusDays(Math.max(1, recommendationWindowDays));

        List<Task> tasks = taskRepository.findTasksForRecommendation(userId, from, to).stream()
                .filter(task -> !"done".equalsIgnoreCase(task.getStatus()))
                .limit(20)
                .toList();

        if (tasks.isEmpty()) {
            return new AiTaskRecommendationResponse(openAiModel, List.of());
        }

        try {
            String prompt = buildPrompt(date, tasks);
            String responseText = requestRecommendations(prompt);
            return new AiTaskRecommendationResponse(openAiModel, normalizeTaskTitles(parseRecommendations(responseText), tasks));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AuthException("\u0041\u0049\u63d0\u6848\u306e\u53d6\u5f97\u4e2d\u306b\u51e6\u7406\u304c\u4e2d\u65ad\u3055\u308c\u307e\u3057\u305f\u3002");
        } catch (IOException ex) {
            throw new AuthException("\u0041\u0049\u63d0\u6848\u306e\u53d6\u5f97\u306b\u5931\u6557\u3057\u307e\u3057\u305f\u3002");
        }
    }

    private String buildPrompt(LocalDate date, List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("Target date: ").append(date).append('\n');
        sb.append("You are a task recommendation assistant for a Japanese task management app.").append('\n');
        sb.append("Return up to 3 recommendations that are actually useful on the target date.").append('\n');
        sb.append("taskTitle must be copied exactly from the original task title. Do not translate or rewrite it.").append('\n');
        sb.append("reason and nextAction must be written in Japanese.").append('\n');
        sb.append("reason must be short and concrete, within 60 Japanese characters.").append('\n');
        sb.append("nextAction must be short and concrete, within 30 Japanese characters.").append('\n');
        sb.append("recommendationType must be one of: focus_today, start_now, split_first_step.").append('\n');
        sb.append("Prioritize urgency, due date risk, and ease of immediate action.").append('\n');
        sb.append('\n');

        for (Task task : tasks) {
            sb.append("- taskId: ").append(task.getId()).append('\n');
            sb.append("  title: ").append(task.getTitle()).append('\n');
            sb.append("  startAt: ")
                    .append(DATE_TIME_FORMATTER.format(task.getStartAt().atZoneSameInstant(TOKYO_ZONE)))
                    .append('\n');
            sb.append("  dueAt: ")
                    .append(DATE_TIME_FORMATTER.format(task.getDueAt().atZoneSameInstant(TOKYO_ZONE)))
                    .append('\n');
            sb.append("  priority: ").append(task.getPriority()).append('\n');
            sb.append("  status: ").append(task.getStatus()).append('\n');
            sb.append("  detail: ").append(task.getDetail() == null ? "" : task.getDetail()).append('\n');
        }

        return sb.toString();
    }

    private String requestRecommendations(String prompt) throws IOException, InterruptedException {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "recommendations", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "taskId", Map.of("type", "integer"),
                                                "taskTitle", Map.of("type", "string", "maxLength", 100),
                                                "recommendationType", Map.of(
                                                        "type", "string",
                                                        "enum", List.of("focus_today", "start_now", "split_first_step")
                                                ),
                                                "reason", Map.of("type", "string", "maxLength", 60),
                                                "nextAction", Map.of("type", "string", "maxLength", 30),
                                                "priorityScore", Map.of("type", "integer")
                                        ),
                                        "required", List.of(
                                                "taskId",
                                                "taskTitle",
                                                "recommendationType",
                                                "reason",
                                                "nextAction",
                                                "priorityScore"
                                        )
                                )
                        )
                ),
                "required", List.of("recommendations")
        );

        Map<String, Object> requestBody = Map.of(
                "model", openAiModel,
                "instructions", """
                        You are a task recommendation assistant.
                        Read the provided task list and return up to 3 recommendations.
                        Output must strictly follow the provided JSON schema.
                        Keep the response concise and useful for a Japanese UI.
                        """,
                "input", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        )
                                )
                        )
                ),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "task_recommendations",
                                "schema", schema,
                                "strict", true
                        )
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openAiBaseUrl + "/responses"))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String responseBody = response.body();
            log.error("OpenAI API request failed. status={}, body={}", response.statusCode(), responseBody);

            if (response.statusCode() == 429 && responseBody != null && responseBody.contains("insufficient_quota")) {
                throw new AuthException(
                        "\u004f\u0070\u0065\u006e\u0041\u0049\u0020\u0041\u0050\u0049\u0020\u306e\u30af\u30ec\u30b8\u30c3\u30c8\u4e0d\u8db3\u3001"
                                + "\u307e\u305f\u306f\u8ab2\u91d1\u8a2d\u5b9a\u672a\u5b8c\u4e86\u306e\u53ef\u80fd\u6027\u304c\u3042\u308a\u307e\u3059\u3002"
                );
            }

            if (response.statusCode() == 401) {
                throw new AuthException(
                        "\u004f\u0070\u0065\u006e\u0041\u0049\u0020\u0041\u0050\u0049\u30ad\u30fc\u304c\u4e0d\u6b63\u3001"
                                + "\u307e\u305f\u306f\u7121\u52b9\u3067\u3059\u3002"
                );
            }

            if (response.statusCode() == 403) {
                throw new AuthException(
                        "\u004f\u0070\u0065\u006e\u0041\u0049\u0020\u0041\u0050\u0049\u0020\u306e\u5229\u7528\u304c\u8a31\u53ef\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002"
                );
            }

            throw new AuthException(
                    "\u0041\u0049\u63d0\u6848\u306e\u53d6\u5f97\u306b\u5931\u6557\u3057\u307e\u3057\u305f\u3002"
                            + "\u004f\u0070\u0065\u006e\u0041\u0049\u0020\u0041\u0050\u0049\u0020\u304b\u3089\u30a8\u30e9\u30fc\u304c\u8fd4\u3055\u308c\u3066\u3044\u307e\u3059\u3002"
            );
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode outputTextNode = root.get("output_text");
        if (outputTextNode != null && outputTextNode.isTextual()) {
            return outputTextNode.asText();
        }

        JsonNode outputNode = root.path("output");
        if (outputNode.isArray()) {
            for (JsonNode itemNode : outputNode) {
                JsonNode contentNode = itemNode.path("content");
                if (!contentNode.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : contentNode) {
                    JsonNode textNode = contentItem.get("text");
                    if (textNode != null && textNode.isTextual()) {
                        return textNode.asText();
                    }
                }
            }
        }

        throw new AuthException("\u0041\u0049\u63d0\u6848\u306e\u30ec\u30b9\u30dd\u30f3\u30b9\u3092\u89e3\u6790\u3067\u304d\u307e\u305b\u3093\u3067\u3057\u305f\u3002");
    }

    private List<AiTaskRecommendationItem> parseRecommendations(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode recommendationsNode = root.path("recommendations");
        if (!recommendationsNode.isArray()) {
            return List.of();
        }

        List<AiTaskRecommendationItem> items = new ArrayList<>();
        for (JsonNode node : recommendationsNode) {
            String reason = trimText(node.path("reason").asText(), MAX_REASON_LENGTH);
            String nextAction = trimText(node.path("nextAction").asText(), MAX_NEXT_ACTION_LENGTH);

            items.add(new AiTaskRecommendationItem(
                    node.path("taskId").asLong(),
                    node.path("taskTitle").asText(),
                    node.path("recommendationType").asText(),
                    reason,
                    nextAction,
                    node.path("priorityScore").asInt(),
                    null
            ));
        }
        return items;
    }

    private List<AiTaskRecommendationItem> normalizeTaskTitles(
            List<AiTaskRecommendationItem> recommendations,
            List<Task> tasks
    ) {
        Map<Long, String> titleByTaskId = new LinkedHashMap<>();
        for (Task task : tasks) {
            titleByTaskId.put(task.getId(), task.getTitle());
        }

        List<AiTaskRecommendationItem> normalized = new ArrayList<>();
        LocalDate today = LocalDate.now(TOKYO_ZONE);
        for (AiTaskRecommendationItem item : recommendations) {
            String originalTitle = titleByTaskId.getOrDefault(item.taskId(), item.taskTitle());
            Task matchedTask = tasks.stream()
                    .filter(task -> task.getId().equals(item.taskId()))
                    .findFirst()
                    .orElse(null);
            Long daysRemaining = null;
            if (matchedTask != null) {
                LocalDate dueDate = matchedTask.getDueAt().atZoneSameInstant(TOKYO_ZONE).toLocalDate();
                daysRemaining = ChronoUnit.DAYS.between(today, dueDate);
            }
            normalized.add(new AiTaskRecommendationItem(
                    item.taskId(),
                    originalTitle,
                    item.recommendationType(),
                    item.reason(),
                    item.nextAction(),
                    item.priorityScore(),
                    daysRemaining
            ));
        }
        return normalized;
    }

    private String trimText(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
