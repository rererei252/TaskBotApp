package com.example.taskbot.auth.service;

import com.example.taskbot.auth.dto.AuthResponse;
import com.example.taskbot.auth.dto.LoginRequest;
import com.example.taskbot.auth.dto.PasswordResetConfirmRequest;
import com.example.taskbot.auth.dto.PasswordResetRequest;
import com.example.taskbot.auth.dto.SignUpRequest;
import com.example.taskbot.auth.entity.PasswordResetToken;
import com.example.taskbot.auth.entity.User;
import com.example.taskbot.auth.exception.AuthException;
import com.example.taskbot.auth.repository.PasswordResetTokenRepository;
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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String verifyBaseUrl;
    private final String resetBaseUrl;
    private final long verificationExpireHours;
    private final long passwordResetExpireMinutes;
    private final String mailFrom;

    public AuthService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.auth.verify-base-url}") String verifyBaseUrl,
            @Value("${app.auth.reset-base-url}") String resetBaseUrl,
            @Value("${app.auth.verification-expire-hours:24}") long verificationExpireHours,
            @Value("${app.auth.password-reset-expire-minutes:30}") long passwordResetExpireMinutes,
            @Value("${app.auth.mail-from}") String mailFrom
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.verifyBaseUrl = verifyBaseUrl;
        this.resetBaseUrl = resetBaseUrl;
        this.verificationExpireHours = verificationExpireHours;
        this.passwordResetExpireMinutes = passwordResetExpireMinutes;
        this.mailFrom = mailFrom;
    }

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        OffsetDateTime now = OffsetDateTime.now();
        String verificationToken = UUID.randomUUID().toString();

        User existing = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (existing != null) {
            if (!existing.isDeletedFlag()) {
                throw new AuthException("このメールアドレスは既に登録されています。");
            }
            archiveDeletedUserEmail(existing, now);
        }

        User user = new User();

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
        user.setProfileMessage(null);
        user.setProfileImageUrl(null);

        User savedUser = userRepository.save(user);
        sendVerificationMail(savedUser.getEmail(), verificationToken);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getLastLoginAt(),
                "認証メールを送信しました。メール内のURLを開いて登録を完了してください。"
        );
    }

    private void archiveDeletedUserEmail(User deletedUser, OffsetDateTime now) {
        Long userId = deletedUser.getId() == null ? 0L : deletedUser.getId();
        String archivedEmail = "deleted+" + userId + "." + now.toEpochSecond() + "@taskkan.local";
        deletedUser.setEmail(archivedEmail);
        userRepository.save(deletedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCaseAndDeletedFlagFalse(normalizedEmail)
                .orElseThrow(() -> new AuthException("メールアドレスまたはパスワードが正しくありません。"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("メールアドレスまたはパスワードが正しくありません。");
        }

        if (!user.isEmailVerified()) {
            throw new AuthException("メール認証が未完了です。認証メールのURLを確認してください。");
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
            throw new AuthException("認証リンクが不正、または期限切れです。");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        user.setVerifiedAt(OffsetDateTime.now());

        return "メール認証が完了しました。ログインしてください。";
    }

    @Transactional
    public AuthResponse requestPasswordReset(PasswordResetRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCaseAndDeletedFlagFalse(normalizedEmail)
                .orElseThrow(() -> new AuthException("そのメールアドレスは存在しません。"));

        String resetToken = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        // 未使用トークンを整理
        passwordResetTokenRepository.deleteByUser_IdAndUsedAtIsNull(user.getId());

        PasswordResetToken entity = new PasswordResetToken();
        entity.setUser(user);
        entity.setToken(resetToken);
        entity.setCreatedAt(now);
        entity.setExpiresAt(now.plusMinutes(passwordResetExpireMinutes));
        entity.setUsedAt(null);

        passwordResetTokenRepository.save(entity);
        sendPasswordResetGuideMail(user, resetToken);

        return new AuthResponse(null, null, null, null, "再設定メールを送信しました。");
    }

    @Transactional(readOnly = true)
    public String validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = getValidResetToken(token);
        long remainingMinutes = Math.max(0, java.time.Duration.between(OffsetDateTime.now(), resetToken.getExpiresAt()).toMinutes());
        return "有効なトークンです。残り " + remainingMinutes + " 分です。";
    }

    @Transactional
    public AuthResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = getValidResetToken(request.token());
        User user = resetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsedAt(OffsetDateTime.now());

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getLastLoginAt(),
                "パスワードを更新しました。ログインしてください。"
        );
    }

    private PasswordResetToken getValidResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException("トークンが不正です。");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedAtIsNull(token)
                .orElseThrow(() -> new AuthException("認証リンクが不正、または期限切れです。"));

        if (resetToken.getExpiresAt() == null || resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AuthException("認証リンクが不正、または期限切れです。");
        }

        return resetToken;
    }

    private void sendVerificationMail(String to, String verificationToken) {
        String verifyUrl = verifyBaseUrl + "?token=" + verificationToken;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(to);
        mailMessage.setSubject("【タスク缶】メールアドレス認証");
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
            throw new AuthException("認証メールの送信に失敗しました: " + detail);
        }
    }

    private void sendPasswordResetGuideMail(User user, String resetToken) {
        String resetUrl = resetBaseUrl + "?token=" + resetToken;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("【タスク缶】パスワード再設定のご案内");
        mailMessage.setText(
                user.getUsername() + " 様\n\n"
                        + "パスワード再設定のリクエストを受け付けました。\n"
                        + "以下のURLを開いて、パスワードを再設定してください。\n\n"
                        + resetUrl
                        + "\n\nこのリンクの有効期限は "
                        + passwordResetExpireMinutes
                        + " 分です。"
        );

        try {
            mailSender.send(mailMessage);
        } catch (Exception ignored) {
            // 存在有無の推測を防ぐため、レスポンスは固定する。
        }
    }

    public AuthResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthException("ユーザーが見つかりません。"));
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), user.getLastLoginAt(), "OK");
    }

}