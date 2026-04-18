package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.MealRequestDto;
import com.example.usermanagement.entity.MealRecord;
import com.example.usermanagement.service.MealService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

/**
 * 饮食日记控制器
 */
@RestController
@RequestMapping("/api/meals")
public class MealController {
    
    private static final Logger logger = LoggerFactory.getLogger(MealController.class);
    
    @Autowired
    private MealService mealService;
    
    /**
     * 添加饮食记录(带图片)
     */
    @PostMapping("/with-image")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMealWithImage(
            @RequestParam("user_id") Long userId,
            @RequestParam("food_name") String foodName,
            @RequestParam("sugar_content") Float sugarContent,
            @RequestParam("calories") Float calories,
            @RequestParam(value = "protein", required = false) Float protein,
            @RequestParam(value = "fat", required = false) Float fat,
            @RequestParam(value = "carbohydrate", required = false) Float carbohydrate,
            @RequestParam(value = "portion_size", required = false) Float portionSize,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam("meal_type") String mealType,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            logger.info("收到添加饮食记录请求（带图片）: 用户={}, 食物={}", userId, foodName);
            
            MealRecord meal = mealService.addMealWithImage(
                userId, foodName, sugarContent, calories, 
                protein, fat, carbohydrate, portionSize, 
                notes, mealType, image
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("meal_id", meal.getMealId());
            response.put("message", "添加成功");
            
            return ResponseEntity.ok(ApiResponse.success("添加成功", response));
        } catch (Exception e) {
            logger.error("添加饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("添加失败: " + e.getMessage()));
        }
    }
    
    /**
     * 添加饮食记录(不带图片)，表单参数
     */
    @PostMapping("/form")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMeal(
            @RequestParam("user_id") Long userId,
            @RequestParam("food_name") String foodName,
            @RequestParam("sugar_content") Float sugarContent,
            @RequestParam("calories") Float calories,
            @RequestParam(value = "protein", required = false) Float protein,
            @RequestParam(value = "fat", required = false) Float fat,
            @RequestParam(value = "carbohydrate", required = false) Float carbohydrate,
            @RequestParam(value = "portion_size", required = false) Float portionSize,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam("meal_type") String mealType
    ) {
        try {
            logger.info("收到添加饮食记录请求（无图片）: 用户={}, 食物={}", userId, foodName);
            
            MealRecord meal = mealService.addMeal(
                userId, foodName, sugarContent, calories, 
                protein, fat, carbohydrate, portionSize, 
                notes, mealType
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("meal_id", meal.getMealId());
            response.put("message", "添加成功");
            
            return ResponseEntity.ok(ApiResponse.success("添加成功", response));
        } catch (Exception e) {
            logger.error("添加饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("添加失败: " + e.getMessage()));
        }
    }

    /**
     * 添加饮食记录(不带图片)，JSON
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMealJson(
            @RequestBody MealRequestDto dto) {
        try {
            logger.info("收到添加饮食记录请求(JSON): 用户={}, 食物={}, imagePath={}", dto.getUserId(), dto.getFoodName(), dto.getImagePath());
            
            MealRecord meal = mealService.addMeal(
                dto.getUserId(), dto.getFoodName(), dto.getSugarContent(), dto.getCalories(),
                dto.getProtein(), dto.getFat(), dto.getCarbohydrate(),
                dto.getPortionSizeAsFloat(),
                dto.getNotes(), dto.getMealType()
            );

            if (dto.getImagePath() != null && !dto.getImagePath().isEmpty()) {
                meal.setImagePath(dto.getImagePath());
                meal = mealService.saveMealRecord(meal);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("meal_id", meal.getMealId());
            response.put("message", "添加成功");
            
            return ResponseEntity.ok(ApiResponse.success("添加成功", response));
        } catch (Exception e) {
            logger.error("添加饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("添加失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取指定日期的饮食记录
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyMeals(
            @RequestParam("user_id") Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            logger.info("查询用户{}在{}的饮食记录", userId, date);
            
            // 获取餐食记录列表
            List<Map<String, Object>> meals = mealService.getDailyMeals(userId, date);
            
            // 获取每日汇总
            Map<String, Object> summary = mealService.getDailySummary(userId, date);
            
            // 组装响应
            Map<String, Object> response = new HashMap<>();
            response.put("date", date.toString());
            response.put("meals", meals);
            response.put("total_sugar", summary.get("totalSugar"));
            response.put("total_calories", summary.get("totalCalories"));
            response.put("meal_count", meals.size());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", response));
        } catch (Exception e) {
            logger.error("获取饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最近N天的饮食记录
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentMeals(
            @RequestParam("user_id") Long userId,
            @RequestParam(value = "days", defaultValue = "7") int days
    ) {
        try {
            logger.info("查询用户{}最近{}天的饮食记录", userId, days);
            
            List<Map<String, Object>> meals = mealService.getRecentMeals(userId, days);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", meals));
        } catch (Exception e) {
            logger.error("获取最近饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除饮食记录
     */
    @DeleteMapping("/{meal_id}")
    public ResponseEntity<ApiResponse<String>> deleteMeal(
            @PathVariable("meal_id") Integer mealId,
            @RequestParam("user_id") Long userId
    ) {
        try {
            logger.info("删除饮食记录: ID={}, 用户={}", mealId, userId);
            
            boolean success = mealService.deleteMeal(mealId, userId);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("删除成功", "success"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("记录不存在或无权删除"));
            }
        } catch (Exception e) {
            logger.error("删除饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 修改饮食记录，表单参数
     */
    @PutMapping("/{meal_id}/form")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMeal(
            @PathVariable("meal_id") Integer mealId,
            @RequestParam("user_id") Long userId,
            @RequestParam(value = "food_name", required = false) String foodName,
            @RequestParam(value = "sugar_content", required = false) Float sugarContent,
            @RequestParam(value = "calories", required = false) Float calories,
            @RequestParam(value = "protein", required = false) Float protein,
            @RequestParam(value = "fat", required = false) Float fat,
            @RequestParam(value = "carbohydrate", required = false) Float carbohydrate,
            @RequestParam(value = "portion_size", required = false) Float portionSize,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "meal_type", required = false) String mealType
    ) {
        try {
            logger.info("收到修改饮食记录请求: ID={}, 用户={}", mealId, userId);
            
            MealRecord meal = mealService.updateMeal(
                mealId, userId, foodName, sugarContent, calories, 
                protein, fat, carbohydrate, portionSize, 
                notes, mealType
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("meal_id", meal.getMealId());
            response.put("message", "修改成功");
            
            return ResponseEntity.ok(ApiResponse.success("修改成功", response));
        } catch (RuntimeException e) {
            logger.error("修改饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("修改失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("修改饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("修改失败: " + e.getMessage()));
        }
    }

    /**
     * 修改饮食记录，JSON
     */
    @PutMapping("/{meal_id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMealJson(
            @PathVariable("meal_id") Integer mealId,
            @RequestBody MealRequestDto dto) {
        try {
            logger.info("收到修改饮食记录请求(JSON): ID={}, 用户={}", mealId, dto.getUserId());
            
            MealRecord meal = mealService.updateMeal(
                mealId, dto.getUserId(), dto.getFoodName(), dto.getSugarContent(), dto.getCalories(),
                dto.getProtein(), dto.getFat(), dto.getCarbohydrate(),
                dto.getPortionSizeAsFloat(),
                dto.getNotes(), dto.getMealType()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("meal_id", meal.getMealId());
            response.put("message", "修改成功");
            
            return ResponseEntity.ok(ApiResponse.success("修改成功", response));
        } catch (RuntimeException e) {
            logger.error("修改饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("修改失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("修改饮食记录失败", e);
            return ResponseEntity.ok(ApiResponse.error("修改失败: " + e.getMessage()));
        }
    }
}

