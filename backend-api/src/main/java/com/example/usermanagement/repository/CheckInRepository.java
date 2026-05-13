package com.example.usermanagement.repository;

import com.example.usermanagement.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);

    List<CheckIn> findByUserIdOrderByCheckInDateDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndWithinLimitTrue(Long userId);

    Optional<CheckIn> findTopByUserIdOrderByCheckInDateDesc(Long userId);

    @Query("SELECT DISTINCT c.userId FROM CheckIn c")
    List<Long> findAllDistinctUserIds();
}
