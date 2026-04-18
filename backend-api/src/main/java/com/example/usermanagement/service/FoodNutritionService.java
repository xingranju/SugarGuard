package com.example.usermanagement.service;

import com.example.usermanagement.entity.FoodNutrition;
import com.example.usermanagement.repository.FoodNutritionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 食品营养查询服务
 * 提供智能模糊匹配功能
 */
@Service
public class FoodNutritionService {
    
    private static final Logger log = LoggerFactory.getLogger(FoodNutritionService.class);
    
    @Autowired
    private FoodNutritionRepository foodNutritionRepository;
    
    /**
     * 智能搜索食品营养信息
     * 使用多种策略进行模糊匹配
     * 
     * @param foodName 食品名称（可能是识别结果）
     * @return 最匹配的食品营养信息，如果找不到返回null
     */
    public FoodNutrition searchFoodNutrition(String foodName) {
        if (foodName == null || foodName.trim().isEmpty()) {
            log.warn("食品名称为空");
            return null;
        }
        
        String cleanedName = foodName.trim().toLowerCase();
        log.info("搜索食品营养信息: {}", cleanedName);
        
        // 策略1: 精确匹配
        Optional<FoodNutrition> exactMatch = foodNutritionRepository.findByFoodName(cleanedName);
        if (exactMatch.isPresent()) {
            log.info("精确匹配成功: {}", exactMatch.get().getFoodName());
            return exactMatch.get();
        }
        
        // 策略2: 模糊匹配（包含关键词）
        List<FoodNutrition> fuzzyMatches = foodNutritionRepository.findByFoodNameContaining(cleanedName);
        if (!fuzzyMatches.isEmpty()) {
            FoodNutrition bestMatch = fuzzyMatches.get(0);
            log.info("模糊匹配成功: {} (输入: {})", bestMatch.getFoodName(), cleanedName);
            return bestMatch;
        }
        
        // 策略3: 提取关键词进行匹配
        // 例如："新鲜苹果" -> 搜索 "apple" 或 "苹果"
        List<String> keywords = extractKeywords(cleanedName);
        for (String keyword : keywords) {
            if (keyword.length() >= 2) { // 至少2个字符
                fuzzyMatches = foodNutritionRepository.findByFoodNameContaining(keyword);
                if (!fuzzyMatches.isEmpty()) {
                    FoodNutrition bestMatch = fuzzyMatches.get(0);
                    log.info("关键词匹配成功: {} (关键词: {}, 输入: {})", 
                             bestMatch.getFoodName(), keyword, cleanedName);
                    return bestMatch;
                }
            }
        }
        
        log.warn("未找到匹配的食品: {}", cleanedName);
        return null;
    }
    
    /**
     * 从食品名称中提取关键词
     * 移除常见的修饰词
     */
    private List<String> extractKeywords(String foodName) {
        List<String> keywords = new ArrayList<>();
        
        // 移除常见修饰词
        String[] stopWords = {"fresh", "raw", "cooked", "fried", "boiled", "steamed",
                              "新鲜", "生", "熟", "炸", "煮", "蒸", "的", "了"};
        
        String cleaned = foodName;
        for (String stopWord : stopWords) {
            cleaned = cleaned.replace(stopWord, " ");
        }
        
        // 分割成单词
        String[] parts = cleaned.trim().split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                keywords.add(part.trim());
            }
        }
        
        // 如果没有提取到关键词，返回原始名称
        if (keywords.isEmpty()) {
            keywords.add(foodName);
        }
        
        return keywords;
    }
    
    /**
     * 获取营养数据的简化版本（仅包含常用字段）
     */
    public Map<String, Object> getNutritionSummary(FoodNutrition nutrition) {
        Map<String, Object> summary = new HashMap<>();
        
        if (nutrition == null) {
            return summary;
        }
        
        summary.put("food_name", nutrition.getFoodName());
        summary.put("calories", getValueOrZero(nutrition.getCaloricValue()));
        summary.put("sugars", getValueOrZero(nutrition.getSugars()));
        summary.put("protein", getValueOrZero(nutrition.getProtein()));
        summary.put("fat", getValueOrZero(nutrition.getFat()));
        summary.put("carbohydrates", getValueOrZero(nutrition.getCarbohydrates()));
        summary.put("dietary_fiber", getValueOrZero(nutrition.getDietaryFiber()));
        
        return summary;
    }
    
    /**
     * 获取BigDecimal值，如果为null则返回0
     */
    private float getValueOrZero(BigDecimal value) {
        return value != null ? value.floatValue() : 0.0f;
    }
    
    /**
     * 批量搜索食品营养信息
     */
    public List<FoodNutrition> searchMultipleFoods(List<String> foodNames) {
        List<FoodNutrition> results = new ArrayList<>();
        
        for (String foodName : foodNames) {
            FoodNutrition nutrition = searchFoodNutrition(foodName);
            if (nutrition != null) {
                results.add(nutrition);
            }
        }
        
        return results;
    }
    
    /**
     * 根据关键词搜索，返回多个匹配结果
     */
    public List<FoodNutrition> searchFoodsByKeyword(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<FoodNutrition> results = foodNutritionRepository.findByFoodNameContaining(keyword.trim());
        
        // 限制返回数量
        if (results.size() > limit) {
            return results.subList(0, limit);
        }
        
        return results;
    }
}

