package com.example.taskbot.account.service;

import com.example.taskbot.account.dto.ProfileImageUploadResponse;
import com.example.taskbot.account.dto.ProfileResponse;
import com.example.taskbot.account.dto.ProfileUpdateRequest;
import com.example.taskbot.auth.entity.User;
import com.example.taskbot.auth.exception.AuthException;
import com.example.taskbot.auth.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final Path profileImageDir;
    private final String profileImagePublicBaseUrl;

    public AccountService(
            UserRepository userRepository,
            @Value("${app.account.profile-image-dir:uploads/profile-images}") String profileImageDir,
            @Value("${app.account.profile-image-public-base-url:http://localhost:8080/api/account/profile-image/files}") String profileImagePublicBaseUrl
    ) {
        this.userRepository = userRepository;
        this.profileImageDir = Paths.get(profileImageDir).toAbsolutePath().normalize();
        this.profileImagePublicBaseUrl = profileImagePublicBaseUrl;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("ユーザーが見つかりません。"));

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileMessage(),
                user.getProfileImageUrl()
        );
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("ユーザーが見つかりません。"));

        String normalizedEmail = request.email().trim().toLowerCase();
        String currentEmail = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase();
        if (!normalizedEmail.equals(currentEmail)) {
            throw new AuthException("メールアドレスはこの画面から変更できません。");
        }

        user.setUsername(request.username().trim());
        user.setProfileMessage(request.profileMessage() == null ? null : request.profileMessage().trim());
        user.setProfileImageUrl(request.profileImageUrl() == null ? null : request.profileImageUrl().trim());

        User saved = userRepository.save(user);
        return new ProfileResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getProfileMessage(),
                saved.getProfileImageUrl()
        );
    }

    @Transactional(readOnly = true)
    public ProfileImageUploadResponse uploadProfileImage(Long userId, MultipartFile file) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("ユーザーが見つかりません。"));

        if (file == null || file.isEmpty()) {
            throw new AuthException("画像ファイルを選択してください。");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AuthException("画像サイズは5MB以下にしてください。");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new AuthException("画像ファイルのみアップロードできます。");
        }

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(profileImageDir);
            Path target = profileImageDir.resolve(fileName).normalize();
            if (!target.startsWith(profileImageDir)) {
                throw new AuthException("保存先パスが不正です。");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new AuthException("画像の保存に失敗しました。");
        }

        return new ProfileImageUploadResponse(profileImagePublicBaseUrl + "/" + fileName);
    }

    @Transactional(readOnly = true)
    public Resource loadProfileImage(String fileName) {
        try {
            Path filePath = profileImageDir.resolve(fileName).normalize();
            if (!filePath.startsWith(profileImageDir) || !Files.exists(filePath)) {
                throw new AuthException("画像が見つかりません。");
            }
            return new UrlResource(filePath.toUri());
        } catch (IOException ex) {
            throw new AuthException("画像の読み込みに失敗しました。");
        }
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("ユーザーが見つかりません。"));

        OffsetDateTime now = OffsetDateTime.now();
        String archivedEmail = "deleted+" + userId + "." + now.toEpochSecond() + "@taskkan.local";
        user.setEmail(archivedEmail);
        user.setDeletedFlag(true);
        user.setDeletedAt(now);
        user.setProfileImageUrl(null);
        user.setProfileMessage(null);
        userRepository.save(user);
    }

    private String getExtension(String originalFileName) {
        if (originalFileName == null) {
            return ".png";
        }
        int index = originalFileName.lastIndexOf('.');
        if (index < 0 || index == originalFileName.length() - 1) {
            return ".png";
        }
        String ext = originalFileName.substring(index).toLowerCase(Locale.ROOT);
        return ext.length() <= 10 ? ext : ".png";
    }
}
