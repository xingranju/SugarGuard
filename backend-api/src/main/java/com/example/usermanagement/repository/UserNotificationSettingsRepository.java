package com.example.usermanagement.repository;

import com.example.usermanagement.entity.UserNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNotificationSettingsRepository extends JpaRepository<UserNotificationSettings, Long> {
    Optional<UserNotificationSettings> findByUserId(Long userId);
}
