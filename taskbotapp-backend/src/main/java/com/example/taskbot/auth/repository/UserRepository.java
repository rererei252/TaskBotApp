package com.example.taskbot.auth.repository;

import com.example.taskbot.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCaseAndDeletedFlagFalse(String email);
    
    boolean existsByEmailIgnoreCaseAndDeletedFlagFalseAndIdNot(String email, Long id);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCaseAndDeletedFlagFalse(String email);

    Optional<User> findByVerificationTokenAndDeletedFlagFalse(String verificationToken);
}
