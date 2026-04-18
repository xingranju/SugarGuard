package com.example.usermanagement.service;

import com.example.usermanagement.entity.Drink;
import com.example.usermanagement.entity.MealRecord;
import com.example.usermanagement.repository.DrinkRepository;
import com.example.usermanagement.repository.MealRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 餐食记录业务层
 */
@Service
public class MealService {
    
    private static final Logger logger = LoggerFactory.getLogger(MealService.class);
    
    // 图片存储路径（可以配置到application.properties）
    private static final String UPLOAD_DIR = "uploads/meals/";
    
    @Autowired
    private MealRecordRepository mealRecordRepository;
    
    @Autowired
    private DrinkRepository drinkRepository;
    
    @Autowired
    private DrinkPreferenceService drinkPreferenceService;
    
    @Autowired
    private DailyHealthRecordService dailyHealthRecordService;
    
    /**
     * 添加餐食记录（带图片）
     */
    @Transactional
    public synchronized MealRecord addMealWithImage(Long userId, String foodName, Float sugarContent, 
                                       Float calories, Float protein, Float fat, 
                                       Float carbohydrate, Float portionSize, 
                                       String notes, String mealType, MultipartFile image) {
        
        logger.info("添加餐食记录：用户{}, 食物{}, 餐次{}", userId, foodName, mealType);
        
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        long dupeCount = mealRecordRepository.countRecentDuplicates(userId, foodName, LocalDate.now(), twoMinutesAgo);
        if (dupeCount > 0) {
            logger.warn("服务端去重拦截(带图片)：用户{} 食物「{}」2分钟内已有{}条记录", userId, foodName, dupeCount);
            return mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, LocalDate.now())
                .stream().filter(m -> m.getFoodName().equals(foodName)).findFirst()
                .orElseThrow(() -> new RuntimeException("去重查询异常"));
        }
        
        // 创建餐食记录
        MealRecord meal = new MealRecord();
        meal.setUserId(userId);
        meal.setFoodName(foodName);
        meal.setSugarContent(sugarContent);
        meal.setCalories(calories);
        meal.setPortionSize(portionSize);
        meal.setNotes(notes);
        meal.setMealDate(LocalDate.now());  
        meal.setMealTime(LocalDateTime.now());
        meal.setMealType(MealRecord.MealType.valueOf(mealType.toLowerCase()));
        
        // 处理图片上传
        if (image != null && !image.isEmpty()) {
            try {
                String imagePath = saveImage(image, userId);
                meal.setImagePath(imagePath);
                logger.info("图片保存成功：{}", imagePath);
            } catch (IOException e) {
                logger.error("图片保存失败", e);
                // 即使图片保存失败，仍然保存餐食记录
            }
        }
        
        MealRecord savedMeal = mealRecordRepository.save(meal);
        logger.info("餐食记录添加成功，ID: {}", savedMeal.getMealId());
        
        // 如果有关联的饮品ID，自动更新饮品偏好
        if (savedMeal.getDrinkId() != null) {
            try {
                drinkPreferenceService.recordConsumption(userId, savedMeal.getDrinkId());
                logger.info("已自动更新用户 {} 对饮品 {} 的偏好记录", userId, savedMeal.getDrinkId());
            } catch (Exception e) {
                logger.warn("更新饮品偏好失败，但餐食记录已保存", e);
            }
        }
        
        // 自动更新每日健康记录中的热量和糖分
        try {
            dailyHealthRecordService.updateCaloriesAndSugarFromMeals(userId, savedMeal.getMealDate());
            logger.info("已自动更新用户 {} 在 {} 的每日健康记录", userId, savedMeal.getMealDate());
        } catch (Exception e) {
            logger.warn("更新每日健康记录失败，但餐食记录已保存", e);
        }
        
        return savedMeal;
    }
    
    /**
     * 添加餐食记录（不带图片）
     */
    @Transactional
    public synchronized MealRecord addMeal(Long userId, String foodName, Float sugarContent, 
                             Float calories, Float protein, Float fat, 
                             Float carbohydrate, Float portionSize, 
                             String notes, String mealType) {
        
        logger.info("添加餐食记录（无图片）：用户{}, 食物{}", userId, foodName);
        
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        long dupeCount = mealRecordRepository.countRecentDuplicates(userId, foodName, LocalDate.now(), twoMinutesAgo);
        if (dupeCount > 0) {
            logger.warn("服务端去重拦截：用户{} 食物「{}」2分钟内已有{}条记录", userId, foodName, dupeCount);
            return mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, LocalDate.now())
                .stream().filter(m -> m.getFoodName().equals(foodName)).findFirst()
                .orElseThrow(() -> new RuntimeException("去重查询异常"));
        }
        
        MealRecord meal = new MealRecord();
        meal.setUserId(userId);
        meal.setFoodName(foodName);
        meal.setSugarContent(sugarContent);
        meal.setCalories(calories);
        meal.setPortionSize(portionSize);
        meal.setNotes(notes);
        meal.setMealDate(LocalDate.now());  
        meal.setMealTime(LocalDateTime.now());
        meal.setMealType(MealRecord.MealType.valueOf(mealType.toLowerCase()));
        
        MealRecord savedMeal = mealRecordRepository.save(meal);
        logger.info("餐食记录添加成功，ID: {}", savedMeal.getMealId());
        
        // 如果有关联的饮品ID，自动更新饮品偏好
        if (savedMeal.getDrinkId() != null) {
            try {
                drinkPreferenceService.recordConsumption(userId, savedMeal.getDrinkId());
                logger.info("已自动更新用户 {} 对饮品 {} 的偏好记录", userId, savedMeal.getDrinkId());
            } catch (Exception e) {
                logger.warn("更新饮品偏好失败，但餐食记录已保存", e);
            }
        }
        
        // 自动更新每日健康记录中的热量和糖分
        try {
            dailyHealthRecordService.updateCaloriesAndSugarFromMeals(userId, savedMeal.getMealDate());
            logger.info("已自动更新用户 {} 在 {} 的每日健康记录", userId, savedMeal.getMealDate());
        } catch (Exception e) {
            logger.warn("更新每日健康记录失败，但餐食记录已保存", e);
        }
        
        return savedMeal;
    }
    
    /**
     * 直接保存餐食记录（用于更新 imagePath 等字段）
     */
    @Transactional
    public MealRecord saveMealRecord(MealRecord meal) {
        return mealRecordRepository.save(meal);
    }

    /**
     * 修改餐食记录
     */
    @Transactional
    public MealRecord updateMeal(Integer mealId, Long userId, String foodName, Float sugarContent, 
                                 Float calories, Float protein, Float fat, 
                                 Float carbohydrate, Float portionSize, 
                                 String notes, String mealType) {
        
        logger.info("修改餐食记录：ID={}, 用户={}", mealId, userId);
        
        // 检查记录是否存在且属于该用户
        Optional<MealRecord> mealOpt = mealRecordRepository.findById(mealId);
        if (!mealOpt.isPresent() || !mealOpt.get().getUserId().equals(userId)) {
            logger.warn("餐食记录不存在或不属于该用户：ID={}, 用户={}", mealId, userId);
            throw new RuntimeException("餐食记录不存在或无权修改");
        }
        
        MealRecord meal = mealOpt.get();
        LocalDate originalDate = meal.getMealDate();
        
        // 更新字段
        if (foodName != null) meal.setFoodName(foodName);
        if (sugarContent != null) meal.setSugarContent(sugarContent);
        if (calories != null) meal.setCalories(calories);
        if (portionSize != null) meal.setPortionSize(portionSize);
        if (notes != null) meal.setNotes(notes);
        if (mealType != null) {
            meal.setMealType(MealRecord.MealType.valueOf(mealType.toLowerCase()));
        }
        
        MealRecord updatedMeal = mealRecordRepository.save(meal);
        logger.info("餐食记录修改成功，ID: {}", updatedMeal.getMealId());
        
        // 自动更新每日健康记录中的热量和糖分
        try {
            dailyHealthRecordService.updateCaloriesAndSugarFromMeals(userId, originalDate);
            logger.info("已自动更新用户 {} 在 {} 的每日健康记录", userId, originalDate);
        } catch (Exception e) {
            logger.warn("更新每日健康记录失败，但餐食记录已保存", e);
        }
        
        return updatedMeal;
    }
    
    /**
     * 获取指定日期的餐食记录
     */
    public List<Map<String, Object>> getDailyMeals(Long userId, LocalDate date) {
        logger.info("查询用户{}在{}的餐食记录", userId, date);
        
        List<MealRecord> meals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, date);
        
        return meals.stream().map(this::convertToMap).collect(Collectors.toList());
    }
    
    /**
     * 获取每日汇总（总糖分、总热量）
     */
    public Map<String, Object> getDailySummary(Long userId, LocalDate date) {
        Object[] result = mealRecordRepository.getDailySummary(userId, date);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date.toString());
        
        // 处理空值或数组长度不足的情况
        if (result != null && result.length >= 2) {
            summary.put("totalSugar", result[0] != null ? result[0] : 0.0);
            summary.put("totalCalories", result[1] != null ? result[1] : 0.0);
        } else {
            summary.put("totalSugar", 0.0);
            summary.put("totalCalories", 0.0);
        }
        
        return summary;
    }
    
    /**
     * 获取最近N天的餐食记录
     */
    public List<Map<String, Object>> getRecentMeals(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        
        List<MealRecord> meals = mealRecordRepository.findRecentMeals(userId, startDate);
        
        return meals.stream().map(this::convertToMap).collect(Collectors.toList());
    }
    
    /**
     * 删除餐食记录
     */
    @Transactional
    public boolean deleteMeal(Integer mealId, Long userId) {
        logger.info("删除餐食记录：ID={}, 用户={}", mealId, userId);
        
        // 检查记录是否存在且属于该用户
        if (!mealRecordRepository.existsByMealIdAndUserId(mealId, userId)) {
            logger.warn("餐食记录不存在或不属于该用户：ID={}, 用户={}", mealId, userId);
            return false;
        }
        
        // 获取记录以删除图片和获取日期
        Optional<MealRecord> mealOpt = mealRecordRepository.findById(mealId);
        if (mealOpt.isPresent()) {
            MealRecord meal = mealOpt.get();
            LocalDate mealDate = meal.getMealDate();
            
            // 删除图片文件
            if (meal.getImagePath() != null && !meal.getImagePath().isEmpty()) {
                try {
                    deleteImage(meal.getImagePath());
                } catch (Exception e) {
                    logger.error("删除图片失败", e);
                }
            }
            
            // 删除数据库记录
            mealRecordRepository.deleteByMealIdAndUserId(mealId, userId);
            logger.info("餐食记录删除成功：ID={}", mealId);
            
            // 自动更新每日健康记录中的热量和糖分
            try {
                dailyHealthRecordService.updateCaloriesAndSugarFromMeals(userId, mealDate);
                logger.info("已自动更新用户 {} 在 {} 的每日健康记录", userId, mealDate);
            } catch (Exception e) {
                logger.warn("更新每日健康记录失败，但餐食记录已删除", e);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 保存图片
     */
    private String saveImage(MultipartFile file, Long userId) throws IOException {
        // 创建上传目录
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = String.format("user%d_%s_%s%s", 
            userId, timestamp, UUID.randomUUID().toString().substring(0, 8), extension);
        
        // 保存文件
        Path filePath = Paths.get(UPLOAD_DIR + filename);
        Files.copy(file.getInputStream(), filePath);
        
        return UPLOAD_DIR + filename;
    }
    
    /**
     * 删除图片
     */
    private void deleteImage(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path);
            logger.info("图片删除成功：{}", imagePath);
        } catch (IOException e) {
            logger.error("删除图片失败：{}", imagePath, e);
        }
    }
    
    /**
     * 将MealRecord转换为Map
     * 智能图片URL处理：
     * - 如果有drink_id，优先使用drinks表的image_url（HTTP链接）
     * - 如果没有drink_id，使用本地的image_path
     */
    private Map<String, Object> convertToMap(MealRecord meal) {
        Map<String, Object> map = new HashMap<>();
        map.put("mealId", meal.getMealId());
        map.put("userId", meal.getUserId());
        map.put("mealDate", meal.getMealDate().toString());
        map.put("mealTime", meal.getMealTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        map.put("mealType", meal.getMealType().name());
        map.put("foodName", meal.getFoodName());
        map.put("sugarContent", meal.getSugarContent());
        map.put("calories", meal.getCalories());
        map.put("portionSize", meal.getPortionSize());
        map.put("notes", meal.getNotes());
        map.put("drinkId", meal.getDrinkId());
        
        // 智能选择图片URL
        String imageUrl = getImageUrl(meal);
        map.put("imagePath", imageUrl);
        
        map.put("createdAt", meal.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return map;
    }
    
    /**
     * 获取餐食记录的图片URL
     * 逻辑：如果有drink_id，使用饮品的image_url；否则使用本地image_path
     */
    private String getImageUrl(MealRecord meal) {
        // 如果有关联的饮品ID
        if (meal.getDrinkId() != null) {
            try {
                Optional<Drink> drinkOpt = drinkRepository.findById(meal.getDrinkId());
                if (drinkOpt.isPresent() && drinkOpt.get().getImageUrl() != null) {
                    String drinkImageUrl = drinkOpt.get().getImageUrl();
                    logger.debug("使用饮品图片URL: {}", drinkImageUrl);
                    return drinkImageUrl;
                }
            } catch (Exception e) {
                logger.warn("获取饮品图片失败，使用本地图片: drink_id={}", meal.getDrinkId(), e);
            }
        }
        
        if (meal.getImagePath() != null) {
            String path = meal.getImagePath();
            if (path.startsWith("http://") || path.startsWith("https://")) {
                return path;
            }
            if (path.startsWith("/")) {
                return path;
            }
            return "/" + path;
        }
        
        // 没有任何图片
        return null;
    }
}

