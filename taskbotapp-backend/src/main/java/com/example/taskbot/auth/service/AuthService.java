package com.example.taskbot.auth.service;

import com.example.taskbot.auth.dto.AuthResponse;
import com.example.taskbot.auth.dto.LoginRequest;
import com.example.taskbot.auth.dto.SignUpRequest;
import com.example.taskbot.auth.entity.User;
import com.example.taskbot.auth.exception.AuthException;
import com.example.taskbot.auth.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String verifyBaseUrl;
    private final long verificationExpireHours;
    private final String mailFrom;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.auth.verify-base-url}") String verifyBaseUrl,
            @Value("${app.auth.verification-expire-hours:24}") long verificationExpireHours,
            @Value("${app.auth.mail-from}") String mailFrom
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.verifyBaseUrl = verifyBaseUrl;
        this.verificationExpireHours = verificationExpireHours;
        this.mailFrom = mailFrom;
    }

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        OffsetDateTime now = OffsetDateTime.now();
        String verificationToken = UUID.randomUUID().toString();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(User::new);

        if (user.getId() != null && !user.isDeletedFlag() && user.isEmailVerified()) {
            throw new AuthException("このメールアドレスは既に登録されています。");
        }

        user.setUsername(request.username().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(now);
        user.setDeletedFlag(false);
        user.setDeletedAt(null);
        user.setEmailVerified(false);
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiresAt(now.plusHours(verificationExpireHours));
        user.setVerifiedAt(null);

        User savedUser = userRepository.save(user);
        sendVerificationMail(savedUser.getEmail(), verificationToken);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getLastLoginAt(),
                "確認メールを送信しました。メール内のURLを開いて登録を完了してください。"
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCaseAndDeletedFlagFalse(request.email())
                .orElseThrow(() -> new AuthException("メールアドレスまたはパスワードが正しくありません。"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("メールアドレスまたはパスワードが正しくありません。");
        }

        if (!user.isEmailVerified()) {
            throw new AuthException("メール認証が未完了です。受信した確認メールのURLを開いてください。");
        }

        OffsetDateTime loginTime = OffsetDateTime.now();
        user.setLastLoginAt(loginTime);

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                loginTime,
                "ログインに成功しました。"
        );
    }

    @Transactional
    public String verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException("認証トークンが不正です。");
        }

        User user = userRepository.findByVerificationTokenAndDeletedFlagFalse(token)
                .orElseThrow(() -> new AuthException("認証リンクが不正、または期限切れです。"));

        if (user.getVerificationTokenExpiresAt() == null
                || user.getVerificationTokenExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AuthException("認証リンクの有効期限が切れています。再度登録してください。");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        user.setVerifiedAt(OffsetDateTime.now());

        return "メール認証が完了しました。ログインしてください。";
    }

    private void sendVerificationMail(String to, String verificationToken) {
        String verifyUrl = verifyBaseUrl + "?token=" + verificationToken;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(to);
        mailMessage.setSubject("【タスク缶】メールアドレス確認");
        mailMessage.setText(
                "以下のURLを開いて登録を完了してください。\n\n"
                        + verifyUrl
                        + "\n\nこのリンクの有効期限は "
                        + verificationExpireHours
                        + " 時間です。"
        );

        try {
            mailSender.send(mailMessage);
        } catch (Exception ex) {
            String detail = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            throw new AuthException("確認メールの送信に失敗しました: " + detail);
        }
    }
}
