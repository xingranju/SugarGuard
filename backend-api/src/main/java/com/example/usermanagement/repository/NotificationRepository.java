package com.example.usermanagement.repository;

import com.example.usermanagement.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    @Modifying
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = :now WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT n FROM UserNotification n WHERE n.userId = :userId AND n.type = :type AND n.createdAt > :since")
    List<UserNotification> findRecentByType(@Param("userId") Long userId,
                                            @Param("type") String type,
                                            @Param("since") LocalDateTime since);
}
