package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.entity.Drink;
import com.example.usermanagement.entity.MealRecord;
import com.example.usermanagement.service.DrinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 饮品管理控制器
 */
@RestController
@RequestMapping("/api/drinks")
@CrossOrigin(origins = "*")
public class DrinkController {
    
    private static final Logger log = LoggerFactory.getLogger(DrinkController.class);
    
    @Autowired
    private DrinkService drinkService;
    
    /**
     * 获取所有饮品
     */
    @GetMapping
    public ApiResponse<List<Drink>> getAllDrinks() {
        try {
            List<Drink> drinks = drinkService.getAllDrinks();
            return ApiResponse.success("获取成功", drinks);
        } catch (Exception e) {
            log.error("获取饮品列表失败", e);
            return ApiResponse.error(500, "获取饮品列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取饮品详情
     */
    @GetMapping("/{drinkId}")
    public ApiResponse<Drink> getDrinkById(@PathVariable Integer drinkId) {
        try {
            Optional<Drink> drink = drinkService.getDrinkById(drinkId);
            if (drink.isPresent()) {
                return ApiResponse.success("获取成功", drink.get());
            } else {
                return ApiResponse.error(404, "饮品不存在");
            }
        } catch (Exception e) {
            log.error("获取饮品详情失败: drinkId={}", drinkId, e);
            return ApiResponse.error(500, "获取饮品详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索饮品
     * 
     * @param keyword 关键词（名称模糊搜索）
     * @param brand 品牌
     * @param category 类别
     */
    @GetMapping("/search")
    public ApiResponse<List<Drink>> searchDrinks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category) {
        try {
            log.info("搜索饮品: keyword={}, brand={}, category={}", keyword, brand, category);
            List<Drink> drinks = drinkService.searchDrinks(keyword, brand, category);
            return ApiResponse.success("搜索成功", drinks);
        } catch (Exception e) {
            log.error("搜索饮品失败", e);
            return ApiResponse.error(500, "搜索饮品失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有品牌
     */
    @GetMapping("/brands")
    public ApiResponse<List<String>> getAllBrands() {
        try {
            List<String> brands = drinkService.getAllBrands();
            return ApiResponse.success("获取成功", brands);
        } catch (Exception e) {
            log.error("获取品牌列表失败", e);
            return ApiResponse.error(500, "获取品牌列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有类别
     */
    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        try {
            List<String> categories = drinkService.getAllCategories();
            return ApiResponse.success("获取成功", categories);
        } catch (Exception e) {
            log.error("获取类别列表失败", e);
            return ApiResponse.error(500, "获取类别列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取饮品统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getDrinkStatistics() {
        try {
            Map<String, Object> stats = drinkService.getDrinkStatistics();
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ApiResponse.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动添加饮品记录
     * 
     * 请求体示例：
     * {
     *   "user_id": 1,
     *   "drink_id": 10,
     *   "meal_type": "snack",
     *   "portion_size": 500.0,
     *   "notes": "下午茶"
     * }
     */
    @PostMapping("/add-record")
    public ApiResponse<MealRecord> addDrinkRecord(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("user_id").toString());
            Integer drinkId = Integer.valueOf(request.get("drink_id").toString());
            String mealType = (String) request.get("meal_type");
            
            Float portionSize = null;
            if (request.containsKey("portion_size") && request.get("portion_size") != null) {
                portionSize = Float.valueOf(request.get("portion_size").toString());
            }
            
            String notes = request.containsKey("notes") ? (String) request.get("notes") : null;
            
            log.info("手动添加饮品记录: userId={}, drinkId={}, mealType={}, portionSize={}", 
                     userId, drinkId, mealType, portionSize);
            
            MealRecord record = drinkService.addDrinkRecord(userId, drinkId, mealType, portionSize, notes);
            
            return ApiResponse.success("添加成功", record);
        } catch (Exception e) {
            log.error("添加饮品记录失败", e);
            return ApiResponse.error(500, "添加饮品记录失败: " + e.getMessage());
        }
    }
}

