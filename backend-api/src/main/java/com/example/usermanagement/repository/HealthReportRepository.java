package com.example.usermanagement.repository;

import com.example.usermanagement.entity.HealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {

    @Query("SELECT r FROM HealthReport r WHERE r.userId = :userId AND r.periodType = :periodType " +
           "ORDER BY r.startDate DESC")
    List<HealthReport> findByUserIdAndPeriodType(@Param("userId") Long userId,
                                                  @Param("periodType") String periodType);

    @Query("SELECT r FROM HealthReport r WHERE r.userId = :userId " +
           "AND r.periodType = :periodType AND r.startDate = :startDate")
    Optional<HealthReport> findByUserIdAndPeriodTypeAndStartDate(
            @Param("userId") Long userId,
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate);

    @Query("SELECT r FROM HealthReport r WHERE r.userId = :userId " +
           "AND r.startDate >= :from AND r.endDate <= :to " +
           "ORDER BY r.startDate DESC")
    List<HealthReport> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                 @Param("from") LocalDate from,
                                                 @Param("to") LocalDate to);

    @Query("SELECT r FROM HealthReport r WHERE r.userId = :userId " +
           "AND r.periodType = :periodType " +
           "AND r.startDate >= :from AND r.endDate <= :to " +
           "ORDER BY r.startDate DESC")
    List<HealthReport> findByUserIdAndPeriodTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("periodType") String periodType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT r FROM HealthReport r WHERE r.userId = :userId ORDER BY r.startDate DESC")
    List<HealthReport> findAllByUserId(@Param("userId") Long userId);
}
