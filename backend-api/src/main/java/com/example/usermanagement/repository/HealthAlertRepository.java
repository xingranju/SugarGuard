package com.example.usermanagement.repository;

import com.example.usermanagement.entity.HealthAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByGroupIdOrderByCreatedAtDesc(Long groupId);
    List<HealthAlert> findByUserIdAndIsReadFalse(Long userId);
    long countByGroupIdAndIsReadFalse(Long groupId);
}
