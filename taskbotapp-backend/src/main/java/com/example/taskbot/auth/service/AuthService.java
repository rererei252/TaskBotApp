package com.example.taskbot.auth.service;

import com.example.taskbot.auth.dto.AuthResponse;
import com.example.taskbot.auth.dto.LoginRequest;
import com.example.taskbot.auth.dto.SignUpRequest;
import com.example.taskbot.auth.entity.User;
import com.example.taskbot.auth.exception.AuthException;
import com.example.taskbot.auth.repository.UserRepository;
import java.time.OffsetDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw new AuthException("このメールアドレスは既に登録されています");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);
        return new AuthResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getLastLoginAt(),
                "ユーザー登録が完了しました"
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new AuthException("メールアドレスまたはパスワードが正しくありません"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("メールアドレスまたはパスワードが正しくありません");
        }

        OffsetDateTime loginTime = OffsetDateTime.now();
        user.setLastLoginAt(loginTime);

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                loginTime,
                "ログインに成功しました"
        );
    }
}
