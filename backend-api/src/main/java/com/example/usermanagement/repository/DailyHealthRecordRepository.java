package com.example.usermanagement.repository;

import com.example.usermanagement.entity.DailyHealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 每日健康记录数据访问接口
 */
@Repository
public interface DailyHealthRecordRepository extends JpaRepository<DailyHealthRecord, Long> {
    
    /**
     * 根据用户ID和日期查询记录
     */
    Optional<DailyHealthRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);
    
    /**
     * 根据用户ID查询所有记录，按日期降序
     */
    List<DailyHealthRecord> findByUserIdOrderByRecordDateDesc(Long userId);
    
    /**
     * 根据用户ID查询指定日期范围的记录
     */
    @Query("SELECT d FROM DailyHealthRecord d WHERE d.userId = :userId AND d.recordDate BETWEEN :startDate AND :endDate ORDER BY d.recordDate DESC")
    List<DailyHealthRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 查询用户最近N天的记录
     */
    @Query("SELECT d FROM DailyHealthRecord d WHERE d.userId = :userId AND d.recordDate >= :startDate ORDER BY d.recordDate DESC")
    List<DailyHealthRecord> findRecentRecords(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate
    );
    
    /**
     * 检查用户在指定日期是否已有记录
     */
    boolean existsByUserIdAndRecordDate(Long userId, LocalDate recordDate);
    
    /**
     * 删除用户指定日期的记录
     */
    void deleteByUserIdAndRecordDate(Long userId, LocalDate recordDate);
    
    /**
     * 统计用户的记录总数
     */
    long countByUserId(Long userId);
}


