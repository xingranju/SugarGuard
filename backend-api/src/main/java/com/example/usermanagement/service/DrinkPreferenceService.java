package com.example.usermanagement.service;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.DrinkDto;
import com.example.usermanagement.dto.UserDrinkPreferenceDto;
import com.example.usermanagement.entity.Drink;
import com.example.usermanagement.entity.UserDrinkPreference;
import com.example.usermanagement.repository.DrinkRepository;
import com.example.usermanagement.repository.UserDrinkPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 饮品偏好服务类
 */
@Service
@Transactional
public class DrinkPreferenceService {

    private static final Logger logger = LoggerFactory.getLogger(DrinkPreferenceService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserDrinkPreferenceRepository preferenceRepository;

    @Autowired
    private DrinkRepository drinkRepository;

    /**
     * 获取用户所有饮品偏好
     */
    public ApiResponse<List<UserDrinkPreferenceDto>> getUserDrinkPreferences(Long userId) {
        try {
            List<UserDrinkPreference> preferences = preferenceRepository.findAll();
            List<UserDrinkPreferenceDto> dtos = new ArrayList<>();
            
            for (UserDrinkPreference preference : preferences) {
                if (preference.getUserId().equals(userId)) {
                    dtos.add(convertToDto(preference));
                }
            }
            
            return ApiResponse.success("获取饮品偏好成功", dtos);
        } catch (Exception e) {
            logger.error("获取饮品偏好失败", e);
            return ApiResponse.error("获取饮品偏好失败: " + e.getMessage());
        }
    }

    /**
     * 添加或更新饮品偏好
     */
    public ApiResponse<UserDrinkPreferenceDto> addOrUpdatePreference(Long userId, Integer drinkId, Integer preferenceScore) {
        try {
            // 验证饮品是否存在
            Optional<Drink> drinkOpt = drinkRepository.findById(drinkId);
            if (!drinkOpt.isPresent()) {
                return ApiResponse.error(404, "饮品不存在");
            }
            
            // 查找是否已有偏好记录
            Optional<UserDrinkPreference> existingPref = preferenceRepository.findByUserIdAndDrinkId(userId, drinkId);
            
            UserDrinkPreference preference;
            boolean isNew = false;
            
            if (existingPref.isPresent()) {
                preference = existingPref.get();
                logger.info("更新用户 {} 对饮品 {} 的偏好", userId, drinkId);
            } else {
                preference = new UserDrinkPreference();
                preference.setUserId(userId);
                preference.setDrinkId(drinkId);
                preference.setConsumptionCount(0);
                preference.setCreatedAt(LocalDateTime.now());
                isNew = true;
                logger.info("创建用户 {} 对饮品 {} 的偏好", userId, drinkId);
            }
            
            if (preferenceScore != null) {
                preference.setPreferenceScore(preferenceScore);  // 直接使用Integer值（1-5）
            }
            preference.setLastConsumedAt(LocalDateTime.now());
            preference.setUpdatedAt(LocalDateTime.now());
            
            preference = preferenceRepository.save(preference);
            
            UserDrinkPreferenceDto dto = convertToDto(preference);
            String message = isNew ? "饮品偏好创建成功" : "饮品偏好更新成功";
            return ApiResponse.success(message, dto);
            
        } catch (Exception e) {
            logger.error("保存饮品偏好失败", e);
            return ApiResponse.error("保存饮品偏好失败: " + e.getMessage());
        }
    }

    /**
     * 记录饮用（增加饮用次数）
     */
    public ApiResponse<UserDrinkPreferenceDto> recordConsumption(Long userId, Integer drinkId) {
        try {
            Optional<UserDrinkPreference> prefOpt = preferenceRepository.findByUserIdAndDrinkId(userId, drinkId);
            
            UserDrinkPreference preference;
            if (prefOpt.isPresent()) {
                preference = prefOpt.get();
                preference.setConsumptionCount(preference.getConsumptionCount() + 1);
            } else {
                // 如果不存在偏好记录，创建一个默认的
                preference = new UserDrinkPreference();
                preference.setUserId(userId);
                preference.setDrinkId(drinkId);
                preference.setPreferenceScore(3); // 默认中等偏好（1-5评分）
                preference.setConsumptionCount(1);
                preference.setCreatedAt(LocalDateTime.now());
            }
            
            preference.setLastConsumedAt(LocalDateTime.now());
            preference.setUpdatedAt(LocalDateTime.now());
            preference = preferenceRepository.save(preference);
            
            UserDrinkPreferenceDto dto = convertToDto(preference);
            return ApiResponse.success("记录饮用成功", dto);
            
        } catch (Exception e) {
            logger.error("记录饮用失败", e);
            return ApiResponse.error("记录饮用失败: " + e.getMessage());
        }
    }

    /**
     * 删除饮品偏好
     */
    public ApiResponse<Void> deletePreference(Long userId, Integer drinkId) {
        try {
            Optional<UserDrinkPreference> prefOpt = preferenceRepository.findByUserIdAndDrinkId(userId, drinkId);
            
            if (!prefOpt.isPresent()) {
                return ApiResponse.error(404, "饮品偏好不存在");
            }
            
            preferenceRepository.delete(prefOpt.get());
            logger.info("删除用户 {} 对饮品 {} 的偏好", userId, drinkId);
            
            return ApiResponse.success("饮品偏好删除成功", null);
        } catch (Exception e) {
            logger.error("删除饮品偏好失败", e);
            return ApiResponse.error("删除饮品偏好失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有饮品列表
     */
    public ApiResponse<List<DrinkDto>> getAllDrinks() {
        try {
            List<Drink> drinks = drinkRepository.findAll();
            List<DrinkDto> dtos = new ArrayList<>();
            
            for (Drink drink : drinks) {
                dtos.add(convertDrinkToDto(drink));
            }
            
            return ApiResponse.success("获取饮品列表成功", dtos);
        } catch (Exception e) {
            logger.error("获取饮品列表失败", e);
            return ApiResponse.error("获取饮品列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据类别获取饮品
     */
    public ApiResponse<List<DrinkDto>> getDrinksByCategory(String category) {
        try {
            List<Drink> drinks = drinkRepository.findByCategory(category);
            List<DrinkDto> dtos = new ArrayList<>();
            
            for (Drink drink : drinks) {
                dtos.add(convertDrinkToDto(drink));
            }
            
            return ApiResponse.success("获取饮品列表成功", dtos);
        } catch (Exception e) {
            logger.error("获取饮品列表失败", e);
            return ApiResponse.error("获取饮品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有饮品类别
     */
    public ApiResponse<List<String>> getAllCategories() {
        try {
            List<String> categories = drinkRepository.findAllCategories();
            return ApiResponse.success("获取类别列表成功", categories);
        } catch (Exception e) {
            logger.error("获取类别列表失败", e);
            return ApiResponse.error("获取类别列表失败: " + e.getMessage());
        }
    }

    /**
     * 转换偏好实体为DTO
     */
    private UserDrinkPreferenceDto convertToDto(UserDrinkPreference preference) {
        UserDrinkPreferenceDto dto = new UserDrinkPreferenceDto();
        dto.setPreferenceId(preference.getPreferenceId().longValue());
        dto.setUserId(preference.getUserId());
        dto.setDrinkId(preference.getDrinkId());
        dto.setPreferenceScore(preference.getPreferenceScore()); // 直接使用1-5评分
        dto.setTimesConsumed(preference.getConsumptionCount());
        
        if (preference.getLastConsumedAt() != null) {
            dto.setLastConsumed(preference.getLastConsumedAt().format(formatter));
        }
        if (preference.getCreatedAt() != null) {
            dto.setCreatedAt(preference.getCreatedAt().format(formatter));
        }
        if (preference.getUpdatedAt() != null) {
            dto.setUpdatedAt(preference.getUpdatedAt().format(formatter));
        }
        
        // 获取关联的饮品信息
        Optional<Drink> drinkOpt = drinkRepository.findById(preference.getDrinkId());
        if (drinkOpt.isPresent()) {
            Drink drink = drinkOpt.get();
            dto.setDrinkName(drink.getDrinkName());
            dto.setBrand(drink.getBrand());
            dto.setCategory(drink.getCategory());
            dto.setImageUrl(drink.getImageUrl());
            dto.setSugarContent(drink.getSugarContent());
            dto.setCalories(drink.getCalories());
            dto.setHealthScore(drink.getHealthScore());
        }
        
        return dto;
    }

    /**
     * 转换饮品实体为DTO
     */
    private DrinkDto convertDrinkToDto(Drink drink) {
        DrinkDto dto = new DrinkDto();
        dto.setDrinkId(drink.getDrinkId());
        dto.setDrinkName(drink.getDrinkName());
        dto.setBrand(drink.getBrand());
        dto.setCategory(drink.getCategory());
        dto.setSugarContent(drink.getSugarContent());
        dto.setCalories(drink.getCalories());
        dto.setVolume(drink.getVolume());
        dto.setCaffeine(drink.getCaffeine());
        dto.setFat(drink.getFat());
        dto.setProtein(drink.getProtein());
        dto.setSodium(drink.getSodium());
        dto.setHealthScore(drink.getHealthScore());
        dto.setIngredients(drink.getIngredients());
        dto.setAllergens(drink.getAllergens());
        dto.setImageUrl(drink.getImageUrl());
        dto.setSourceUrl(drink.getSourceUrl());
        return dto;
    }
}
