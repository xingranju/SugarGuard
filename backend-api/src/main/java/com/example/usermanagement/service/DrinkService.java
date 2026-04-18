package com.example.usermanagement.service;

import com.example.usermanagement.entity.Drink;
import com.example.usermanagement.entity.MealRecord;
import com.example.usermanagement.entity.UserDrinkPreference;
import com.example.usermanagement.repository.DrinkRepository;
import com.example.usermanagement.repository.MealRecordRepository;
import com.example.usermanagement.repository.UserDrinkPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 饮品服务类
 */
@Service
public class DrinkService {
    
    private static final Logger log = LoggerFactory.getLogger(DrinkService.class);
    
    @Autowired
    private DrinkRepository drinkRepository;
    
    @Autowired
    private MealRecordRepository mealRecordRepository;
    
    @Autowired
    private UserDrinkPreferenceRepository userDrinkPreferenceRepository;
    
    @Autowired
    private DailyHealthRecordService dailyHealthRecordService;
    
    /**
     * 获取所有饮品
     */
    public List<Drink> getAllDrinks() {
        return drinkRepository.findAll();
    }
    
    /**
     * 根据ID获取饮品
     */
    public Optional<Drink> getDrinkById(Integer drinkId) {
        return drinkRepository.findById(drinkId);
    }
    
    /**
     * 搜索饮品
     */
    public List<Drink> searchDrinks(String keyword, String brand, String category) {
        if (brand != null && !brand.isEmpty() && category != null && !category.isEmpty()) {
            return drinkRepository.findByBrandAndCategory(brand, category);
        } else if (brand != null && !brand.isEmpty()) {
            return drinkRepository.findByBrand(brand);
        } else if (category != null && !category.isEmpty()) {
            return drinkRepository.findByCategory(category);
        } else if (keyword != null && !keyword.isEmpty()) {
            return drinkRepository.searchByName(keyword);
        } else {
            return drinkRepository.findAllOrderByHealthScore();
        }
    }
    
    /**
     * 获取所有品牌
     */
    public List<String> getAllBrands() {
        return drinkRepository.findAllBrands();
    }
    
    /**
     * 获取所有类别
     */
    public List<String> getAllCategories() {
        return drinkRepository.findAllCategories();
    }
    
    /**
     * 手动添加饮品记录
     * 
     * @param userId 用户ID
     * @param drinkId 饮品ID
     * @param mealType 餐次类型
     * @param portionSize 份量（如果为null，使用标准容量）
     * @param notes 备注
     * @return 新增的餐食记录
     */
    @Transactional
    public MealRecord addDrinkRecord(Long userId, Integer drinkId, String mealType, 
                                     Float portionSize, String notes) {
        log.info("用户 {} 手动添加饮品记录: drinkId={}", userId, drinkId);
        
        // 1. 获取饮品信息
        Optional<Drink> drinkOpt = drinkRepository.findById(drinkId);
        if (!drinkOpt.isPresent()) {
            log.error("饮品不存在: drinkId={}", drinkId);
            throw new RuntimeException("饮品不存在");
        }
        
        Drink drink = drinkOpt.get();
        
        // 2. 计算实际营养量（基于份量）
        float defaultVolume = drink.getVolume() != null ? drink.getVolume() : 500.0f;
        float actualPortionSize = portionSize != null ? portionSize : defaultVolume;
        float multiplier = actualPortionSize / 100.0f; // 营养数据是per 100ml
        
        float sugarContent = drink.getSugarContent() != null ? drink.getSugarContent() : 0.0f;
        float calories = drink.getCalories() != null ? drink.getCalories() : 0.0f;
        float actualSugar = sugarContent * multiplier;
        float actualCalories = calories * multiplier;
        
        // 3. 创建餐食记录
        MealRecord record = new MealRecord();
        record.setUserId(userId);
        record.setMealDate(LocalDate.now());
        record.setMealTime(LocalDateTime.now());
        record.setMealType(MealRecord.MealType.valueOf(mealType));
        record.setDrinkId(drinkId);
        record.setFoodName(drink.getDrinkName());
        record.setPortionSize(actualPortionSize);
        record.setCalories(actualCalories);
        record.setSugarContent(actualSugar);
        record.setNotes(notes);
        record.setCreatedAt(LocalDateTime.now());
        
        MealRecord savedRecord = mealRecordRepository.save(record);
        log.info("饮品记录已保存: mealId={}", savedRecord.getMealId());
        
        // 4. 更新用户饮品偏好
        updateDrinkPreference(userId, drinkId);
        
        // 5. 更新当日健康记录的总热量和总糖分
        dailyHealthRecordService.updateCaloriesAndSugarFromMeals(userId, LocalDate.now());
        
        return savedRecord;
    }
    
    /**
     * 更新用户饮品偏好
     * 如果存在记录则更新消费次数+1，否则创建新记录
     */
    private void updateDrinkPreference(Long userId, Integer drinkId) {
        Optional<UserDrinkPreference> prefOpt = userDrinkPreferenceRepository
            .findByUserIdAndDrinkId(userId, drinkId);
        
        UserDrinkPreference preference;
        if (prefOpt.isPresent()) {
            // 已存在，更新
            preference = prefOpt.get();
            preference.setConsumptionCount(preference.getConsumptionCount() + 1);
            preference.setLastConsumedAt(LocalDateTime.now());
            preference.setUpdatedAt(LocalDateTime.now());
            log.info("更新饮品偏好: userId={}, drinkId={}, 新消费次数={}", 
                     userId, drinkId, preference.getConsumptionCount());
        } else {
            // 不存在，新建
            preference = new UserDrinkPreference();
            preference.setUserId(userId);
            preference.setDrinkId(drinkId);
            preference.setConsumptionCount(1);
            preference.setLastConsumedAt(LocalDateTime.now());
            preference.setPreferenceScore(3);  // 默认中等偏好（1-5评分）
            preference.setCreatedAt(LocalDateTime.now());
            preference.setUpdatedAt(LocalDateTime.now());
            log.info("创建饮品偏好记录: userId={}, drinkId={}", userId, drinkId);
        }
        
        userDrinkPreferenceRepository.save(preference);
    }
    
    /**
     * 获取饮品统计信息（用于前端显示）
     */
    public Map<String, Object> getDrinkStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCount = drinkRepository.count();
        List<String> brands = drinkRepository.findAllBrands();
        List<String> categories = drinkRepository.findAllCategories();
        
        stats.put("total_drinks", totalCount);
        stats.put("total_brands", brands.size());
        stats.put("total_categories", categories.size());
        stats.put("brands", brands);
        stats.put("categories", categories);
        
        return stats;
    }
}

