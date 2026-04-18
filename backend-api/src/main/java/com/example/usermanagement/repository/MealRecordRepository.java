package com.example.usermanagement.repository;

import com.example.usermanagement.entity.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 餐食记录数据访问层
 */
@Repository
public interface MealRecordRepository extends JpaRepository<MealRecord, Integer> {
    
    /**
     * 查询指定用户指定日期的所有餐食记录
     */
    @Query("SELECT m FROM MealRecord m WHERE m.userId = :userId AND m.mealDate = :mealDate ORDER BY m.mealTime DESC")
    List<MealRecord> findByUserIdAndMealDateOrderByMealTimeDesc(@Param("userId") Long userId, @Param("mealDate") LocalDate mealDate);
    
    /**
     * 查询指定用户某个日期范围的餐食记录
     */
    @Query("SELECT m FROM MealRecord m WHERE m.userId = :userId AND m.mealDate BETWEEN :startDate AND :endDate ORDER BY m.mealDate DESC, m.mealTime DESC")
    List<MealRecord> findByUserIdAndMealDateBetweenOrderByMealTimeDesc(
        @Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 查询指定用户某个日期的总糖分和总热量
     */
    @Query("SELECT COALESCE(SUM(m.sugarContent), 0) as totalSugar, " +
           "COALESCE(SUM(m.calories), 0) as totalCalories " +
           "FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate = :mealDate")
    Object[] getDailySummary(@Param("userId") Long userId, @Param("mealDate") LocalDate mealDate);
    
    /**
     * 查询指定用户最近N天的餐食记录
     */
    @Query("SELECT m FROM MealRecord m " +
           "WHERE m.userId = :userId AND m.mealDate >= :startDate " +
           "ORDER BY m.mealDate DESC, m.mealTime DESC")
    List<MealRecord> findRecentMeals(@Param("userId") Long userId, 
                                     @Param("startDate") LocalDate startDate);
    
    /**
     * 检查同一用户同一天同一食物在指定时间之后是否已有记录（用于去重）
     */
    @Query("SELECT COUNT(m) FROM MealRecord m WHERE m.userId = :userId AND m.foodName = :foodName " +
           "AND m.mealDate = :mealDate AND m.mealTime >= :sinceTime")
    long countRecentDuplicates(@Param("userId") Long userId, @Param("foodName") String foodName,
                               @Param("mealDate") LocalDate mealDate, @Param("sinceTime") java.time.LocalDateTime sinceTime);

    /**
     * 删除指定用户的餐食记录
     */
    void deleteByMealIdAndUserId(Integer mealId, Long userId);
    
    /**
     * 检查餐食记录是否属于指定用户
     */
    boolean existsByMealIdAndUserId(Integer mealId, Long userId);
}

