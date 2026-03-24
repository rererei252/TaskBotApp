package com.example.taskbot.ai.controller;

import com.example.taskbot.ai.dto.AiTaskRecommendationResponse;
import com.example.taskbot.ai.service.AiRecommendationService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(
        originPatterns = {
                "http://localhost:*",
                "http://127.0.0.1:*"
        },
        allowCredentials = "true"
)
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    public AiRecommendationController(AiRecommendationService aiRecommendationService) {
        this.aiRecommendationService = aiRecommendationService;
    }

    @GetMapping("/task-recommendations")
    public ResponseEntity<AiTaskRecommendationResponse> getTaskRecommendations(
            HttpSession session,
            @RequestParam LocalDate date
    ) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(aiRecommendationService.getTaskRecommendations(userId, date));
    }
}
