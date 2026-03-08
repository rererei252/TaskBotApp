package com.example.taskbot.auth.controller;

import com.example.taskbot.auth.dto.AuthResponse;
import com.example.taskbot.auth.dto.LoginRequest;
import com.example.taskbot.auth.dto.PasswordResetConfirmRequest;
import com.example.taskbot.auth.dto.PasswordResetRequest;
import com.example.taskbot.auth.dto.SignUpRequest;
import com.example.taskbot.auth.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        originPatterns = {
                "http://localhost:*",
                "http://127.0.0.1:*"
        }
)
public class AuthController {

    private final AuthService authService;
    private final String frontendVerifyUrl;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.frontend-verify-url:http://localhost:5173/auth/verify}") String frontendVerifyUrl
    ) {
        this.authService = authService;
        this.frontendVerifyUrl = frontendVerifyUrl;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        AuthResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyRedirect(@RequestParam("token") String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        URI target = URI.create(frontendVerifyUrl + "?token=" + encodedToken);
        return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
    }

    @GetMapping("/verify-token")
    public ResponseEntity<Map<String, String>> verifyToken(@RequestParam("token") String token) {
        String message = authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<AuthResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        AuthResponse response = authService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/password-reset/validate")
    public ResponseEntity<Map<String, String>> validatePasswordResetToken(@RequestParam("token") String token) {
        String message = authService.validatePasswordResetToken(token);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<AuthResponse> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        AuthResponse response = authService.confirmPasswordReset(request);
        return ResponseEntity.ok(response);
    }
}