package com.example.taskbot.auth.repository;

import com.example.taskbot.auth.entity.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);

    long countByUser_IdAndUsedAtIsNull(Long userId);

    void deleteByUser_IdAndUsedAtIsNull(Long userId);
}
