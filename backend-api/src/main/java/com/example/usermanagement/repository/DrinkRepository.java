package com.example.usermanagement.repository;

import com.example.usermanagement.entity.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 饮品数据仓库
 */
@Repository
public interface DrinkRepository extends JpaRepository<Drink, Integer> {
    
    /**
     * 根据饮品名称模糊搜索
     */
    @Query("SELECT d FROM Drink d WHERE LOWER(d.drinkName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Drink> searchByName(@Param("keyword") String keyword);
    
    /**
     * 根据品牌搜索
     */
    List<Drink> findByBrand(String brand);
    
    /**
     * 根据类别搜索
     */
    List<Drink> findByCategory(String category);
    
    /**
     * 根据品牌和类别搜索
     */
    List<Drink> findByBrandAndCategory(String brand, String category);
    
    /**
     * 获取所有品牌（去重）
     */
    @Query("SELECT DISTINCT d.brand FROM Drink d WHERE d.brand IS NOT NULL ORDER BY d.brand")
    List<String> findAllBrands();
    
    /**
     * 获取所有类别（去重）
     */
    @Query("SELECT DISTINCT d.category FROM Drink d WHERE d.category IS NOT NULL ORDER BY d.category")
    List<String> findAllCategories();
    
    /**
     * 根据健康评分排序获取饮品
     */
    @Query("SELECT d FROM Drink d ORDER BY d.healthScore DESC")
    List<Drink> findAllOrderByHealthScore();
}
