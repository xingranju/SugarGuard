package com.example.usermanagement.repository;

import com.example.usermanagement.entity.FoodNutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 食品营养数据仓库
 */
@Repository
public interface FoodNutritionRepository extends JpaRepository<FoodNutrition, Long> {
    
    /**
     * 精确匹配食品名称
     */
    Optional<FoodNutrition> findByFoodName(String foodName);
    
    /**
     * 模糊匹配食品名称（LIKE查询）
     * 返回最相似的前N条结果
     */
    @Query("SELECT f FROM FoodNutrition f WHERE LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY LENGTH(f.foodName) ASC")
    List<FoodNutrition> findByFoodNameContaining(@Param("keyword") String keyword);
    
    /**
     * 模糊匹配食品名称（支持多个关键词）
     */
    @Query("SELECT f FROM FoodNutrition f WHERE " +
           "LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword1, '%')) OR " +
           "LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword2, '%')) " +
           "ORDER BY LENGTH(f.foodName) ASC")
    List<FoodNutrition> findByMultipleKeywords(@Param("keyword1") String keyword1, 
                                                @Param("keyword2") String keyword2);
}

