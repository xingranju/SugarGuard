package com.example.usermanagement.controller;

import com.example.usermanagement.entity.FoodNutrition;
import com.example.usermanagement.service.FoodNutritionService;
import com.example.usermanagement.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 食品营养信息控制器
 */
@RestController
@RequestMapping("/api/food-nutrition")
@CrossOrigin(origins = "*")
public class FoodNutritionController {
    
    private static final Logger log = LoggerFactory.getLogger(FoodNutritionController.class);
    
    @Autowired
    private FoodNutritionService foodNutritionService;
    
    /**
     * 搜索食品营养信息（智能模糊匹配）
     * GET /api/food-nutrition/search?name=apple
     */
    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> searchFood(@RequestParam("name") String foodName) {
        try {
            log.info("搜索食品营养信息: {}", foodName);
            
            FoodNutrition nutrition = foodNutritionService.searchFoodNutrition(foodName);
            
            if (nutrition == null) {
                return ApiResponse.error("未找到匹配的食品: " + foodName);
            }
            
            Map<String, Object> summary = foodNutritionService.getNutritionSummary(nutrition);
            return ApiResponse.success("查询成功", summary);
            
        } catch (Exception e) {
            log.error("搜索食品营养信息失败", e);
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据关键词搜索，返回多个结果
     * GET /api/food-nutrition/search-multiple?keyword=apple&limit=10
     */
    @GetMapping("/search-multiple")
    public ApiResponse<List<FoodNutrition>> searchMultiple(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            log.info("关键词搜索: {}, 限制: {}", keyword, limit);
            
            List<FoodNutrition> results = foodNutritionService.searchFoodsByKeyword(keyword, limit);
            return ApiResponse.success("查询成功，找到 " + results.size() + " 条结果", results);
            
        } catch (Exception e) {
            log.error("关键词搜索失败", e);
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }
}

