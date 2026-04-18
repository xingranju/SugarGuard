package com.example.usermanagement.service;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.DailyHealthRecordDto;
import com.example.usermanagement.entity.DailyHealthRecord;
import com.example.usermanagement.entity.MealRecord;
import com.example.usermanagement.repository.DailyHealthRecordRepository;
import com.example.usermanagement.repository.MealRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 每日健康记录服务类
 */
@Service
@Transactional
public class DailyHealthRecordService {

    private static final Logger logger = LoggerFactory.getLogger(DailyHealthRecordService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DailyHealthRecordRepository recordRepository;
    
    @Autowired
    private MealRecordRepository mealRecordRepository;

    /**
     * 获取用户指定日期的健康记录
     */
    public ApiResponse<DailyHealthRecordDto> getRecordByDate(Long userId, String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            Optional<DailyHealthRecord> recordOpt = recordRepository.findByUserIdAndRecordDate(userId, date);
            
            if (recordOpt.isPresent()) {
                DailyHealthRecordDto dto = convertToDto(recordOpt.get());
                return ApiResponse.success("获取健康记录成功", dto);
            } else {
                return ApiResponse.error(404, "该日期没有健康记录");
            }
        } catch (Exception e) {
            logger.error("获取健康记录失败", e);
            return ApiResponse.error("获取健康记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近N天的健康记录
     */
    public ApiResponse<List<DailyHealthRecordDto>> getRecentRecords(Long userId, int days) {
        try {
            LocalDate startDate = LocalDate.now().minusDays(days - 1);
            List<DailyHealthRecord> records = recordRepository.findRecentRecords(userId, startDate);
            List<DailyHealthRecordDto> dtos = new ArrayList<>();
            
            for (DailyHealthRecord record : records) {
                dtos.add(convertToDto(record));
            }
            
            return ApiResponse.success("获取健康记录成功", dtos);
        } catch (Exception e) {
            logger.error("获取健康记录失败", e);
            return ApiResponse.error("获取健康记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户指定日期范围的健康记录
     */
    public ApiResponse<List<DailyHealthRecordDto>> getRecordsByDateRange(
            Long userId, String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
            
            List<DailyHealthRecord> records = recordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
            List<DailyHealthRecordDto> dtos = new ArrayList<>();
            
            for (DailyHealthRecord record : records) {
                dtos.add(convertToDto(record));
            }
            
            return ApiResponse.success("获取健康记录成功", dtos);
        } catch (Exception e) {
            logger.error("获取健康记录失败", e);
            return ApiResponse.error("获取健康记录失败: " + e.getMessage());
        }
    }

    /**
     * 创建或更新健康记录
     * 注意：total_sugar_intake 和 total_calories 由系统根据餐食记录自动计算，
     * 用户手动更新时不应覆盖这两个字段
     */
    public ApiResponse<DailyHealthRecordDto> createOrUpdateRecord(Long userId, DailyHealthRecordDto dto) {
        try {
            LocalDate date = LocalDate.parse(dto.getRecordDate(), DATE_FORMATTER);
            Optional<DailyHealthRecord> existingRecord = recordRepository.findByUserIdAndRecordDate(userId, date);
            
            DailyHealthRecord record;
            boolean isNew = false;
            
            if (existingRecord.isPresent()) {
                record = existingRecord.get();
                logger.info("更新用户 {} 在 {} 的健康记录", userId, date);
            } else {
                record = new DailyHealthRecord();
                record.setUserId(userId);
                record.setRecordDate(date);
                isNew = true;
                logger.info("创建用户 {} 在 {} 的健康记录", userId, date);
            }
            
            // 更新字段（不包括 total_sugar_intake 和 total_calories，这两个字段由餐食记录自动计算）
            // 只有在创建新记录或明确提供了非null值时才更新这两个字段
            if (isNew || dto.getTotalSugarIntake() != null) {
                record.setTotalSugarIntake(dto.getTotalSugarIntake());
            }
            if (isNew || dto.getTotalCalories() != null) {
                record.setTotalCalories(dto.getTotalCalories());
            }
            
            // 其他字段正常更新
            if (dto.getWaterIntake() != null) {
                record.setWaterIntake(dto.getWaterIntake());
            }
            if (dto.getExerciseMinutes() != null) {
                record.setExerciseMinutes(dto.getExerciseMinutes());
            }
            if (dto.getSleepHours() != null) {
                record.setSleepHours(dto.getSleepHours());
            }
            if (dto.getSystolicBp() != null) {
                record.setSystolicBp(dto.getSystolicBp());
            }
            if (dto.getDiastolicBp() != null) {
                record.setDiastolicBp(dto.getDiastolicBp());
            }
            if (dto.getBloodGlucose() != null) {
                record.setBloodGlucose(dto.getBloodGlucose());
            }
            if (dto.getWeight() != null) {
                record.setWeight(dto.getWeight());
            }
            if (dto.getMood() != null) {
                record.setMood(dto.getMood());
            }
            if (dto.getNotes() != null) {
                record.setNotes(dto.getNotes());
            }
            
            record = recordRepository.save(record);
            
            DailyHealthRecordDto resultDto = convertToDto(record);
            String message = isNew ? "健康记录创建成功" : "健康记录更新成功";
            return ApiResponse.success(message, resultDto);
            
        } catch (Exception e) {
            logger.error("保存健康记录失败", e);
            return ApiResponse.error("保存健康记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除健康记录
     */
    public ApiResponse<Void> deleteRecord(Long userId, String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            
            if (!recordRepository.existsByUserIdAndRecordDate(userId, date)) {
                return ApiResponse.error(404, "健康记录不存在");
            }
            
            recordRepository.deleteByUserIdAndRecordDate(userId, date);
            logger.info("删除用户 {} 在 {} 的健康记录", userId, date);
            
            return ApiResponse.success("健康记录删除成功", null);
        } catch (Exception e) {
            logger.error("删除健康记录失败", e);
            return ApiResponse.error("删除健康记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的记录统计
     */
    public ApiResponse<Long> getRecordCount(Long userId) {
        try {
            long count = recordRepository.countByUserId(userId);
            return ApiResponse.success("获取记录统计成功", count);
        } catch (Exception e) {
            logger.error("获取记录统计失败", e);
            return ApiResponse.error("获取记录统计失败: " + e.getMessage());
        }
    }

    /**
     * 根据饮食记录自动更新指定日期的热量和糖分
     * 此方法从meal_records表中计算当天的总热量和总糖分，并更新到daily_health_records表
     */
    @Transactional
    public void updateCaloriesAndSugarFromMeals(Long userId, LocalDate date) {
        try {
            logger.info("自动更新用户 {} 在 {} 的热量和糖分数据", userId, date);
            
            // 从饮食记录中获取当天的总热量和总糖分
            List<MealRecord> meals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, date);
            
            Float totalSugar = 0.0f;
            Float totalCalories = 0.0f;
            
            // 累加所有餐食记录的糖分和热量
            for (MealRecord meal : meals) {
                if (meal.getSugarContent() != null) {
                    totalSugar += meal.getSugarContent();
                }
                if (meal.getCalories() != null) {
                    totalCalories += meal.getCalories();
                }
            }
            
            logger.info("从 {} 条餐食记录中计算出: 总糖分={}, 总热量={}", meals.size(), totalSugar, totalCalories);
            
            logger.info("计算得到: 总糖分={}, 总热量={}", totalSugar, totalCalories);
            
            // 查找或创建健康记录
            Optional<DailyHealthRecord> existingRecord = recordRepository.findByUserIdAndRecordDate(userId, date);
            
            DailyHealthRecord record;
            if (existingRecord.isPresent()) {
                record = existingRecord.get();
                logger.info("更新现有健康记录");
            } else {
                record = new DailyHealthRecord();
                record.setUserId(userId);
                record.setRecordDate(date);
                logger.info("创建新的健康记录");
            }
            
            // 更新热量和糖分
            record.setTotalCalories(totalCalories);
            record.setTotalSugarIntake(totalSugar);
            
            recordRepository.save(record);
            logger.info("成功更新用户 {} 在 {} 的健康记录: 总糖分={}, 总热量={}", 
                       userId, date, totalSugar, totalCalories);
            
        } catch (Exception e) {
            logger.error("更新热量和糖分失败", e);
            throw new RuntimeException("更新健康记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换实体为DTO
     */
    private DailyHealthRecordDto convertToDto(DailyHealthRecord record) {
        DailyHealthRecordDto dto = new DailyHealthRecordDto();
        dto.setRecordId(record.getRecordId());
        dto.setUserId(record.getUserId());
        dto.setRecordDate(record.getRecordDate().format(DATE_FORMATTER));
        dto.setTotalSugarIntake(record.getTotalSugarIntake());
        dto.setTotalCalories(record.getTotalCalories());
        dto.setWaterIntake(record.getWaterIntake());
        dto.setExerciseMinutes(record.getExerciseMinutes());
        dto.setSleepHours(record.getSleepHours());
        dto.setSystolicBp(record.getSystolicBp());
        dto.setDiastolicBp(record.getDiastolicBp());
        dto.setBloodGlucose(record.getBloodGlucose());
        dto.setWeight(record.getWeight());
        dto.setMood(record.getMood());
        dto.setNotes(record.getNotes());
        
        if (record.getCreatedAt() != null) {
            dto.setCreatedAt(record.getCreatedAt().format(DATETIME_FORMATTER));
        }
        
        return dto;
    }
}

