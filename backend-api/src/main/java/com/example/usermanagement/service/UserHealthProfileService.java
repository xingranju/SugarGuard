package com.example.usermanagement.service;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.UserHealthProfileDto;
import com.example.usermanagement.entity.UserHealthProfile;
import com.example.usermanagement.repository.UserHealthProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 用户健康档案服务类
 */
@Service
@Transactional
public class UserHealthProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserHealthProfileService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserHealthProfileRepository healthProfileRepository;

    /**
     * 获取用户健康档案
     */
    public ApiResponse<UserHealthProfileDto> getUserHealthProfile(Long userId) {
        try {
            Optional<UserHealthProfile> profileOpt = healthProfileRepository.findByUserId(userId);
            
            if (profileOpt.isPresent()) {
                UserHealthProfileDto dto = convertToDto(profileOpt.get());
                return ApiResponse.success("获取健康档案成功", dto);
            } else {
                return ApiResponse.error(404, "健康档案不存在");
            }
        } catch (Exception e) {
            logger.error("获取健康档案失败", e);
            return ApiResponse.error("获取健康档案失败: " + e.getMessage());
        }
    }

    /**
     * 创建或更新用户健康档案
     */
    public ApiResponse<UserHealthProfileDto> createOrUpdateHealthProfile(Long userId, UserHealthProfileDto dto) {
        try {
            Optional<UserHealthProfile> existingProfile = healthProfileRepository.findByUserId(userId);
            
            UserHealthProfile profile;
            boolean isNew = false;
            
            if (existingProfile.isPresent()) {
                profile = existingProfile.get();
                logger.info("更新用户 {} 的健康档案", userId);
            } else {
                profile = new UserHealthProfile();
                profile.setUserId(userId);
                isNew = true;
                logger.info("创建用户 {} 的健康档案", userId);
            }
            
            // 更新字段
            profile.setAge(dto.getAge());
            profile.setGender(dto.getGender());
            profile.setHeight(dto.getHeight());
            profile.setWeight(dto.getWeight());
            profile.setHealthConditions(dto.getHealthConditions());
            profile.setAllergies(dto.getAllergies());
            profile.setMedications(dto.getMedications());
            profile.setActivityLevel(dto.getActivityLevel());
            profile.setSugarLimit(dto.getSugarLimit());
            profile.setCalorieLimit(dto.getCalorieLimit());
            profile.setWaterGoal(dto.getWaterGoal());
            
            profile = healthProfileRepository.save(profile);
            
            UserHealthProfileDto resultDto = convertToDto(profile);
            String message = isNew ? "健康档案创建成功" : "健康档案更新成功";
            return ApiResponse.success(message, resultDto);
            
        } catch (Exception e) {
            logger.error("保存健康档案失败", e);
            return ApiResponse.error("保存健康档案失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户健康档案
     */
    public ApiResponse<Void> deleteHealthProfile(Long userId) {
        try {
            if (!healthProfileRepository.existsByUserId(userId)) {
                return ApiResponse.error(404, "健康档案不存在");
            }
            
            healthProfileRepository.deleteByUserId(userId);
            logger.info("删除用户 {} 的健康档案", userId);
            
            return ApiResponse.success("健康档案删除成功", null);
        } catch (Exception e) {
            logger.error("删除健康档案失败", e);
            return ApiResponse.error("删除健康档案失败: " + e.getMessage());
        }
    }

    /**
     * 计算BMI
     */
    public ApiResponse<Double> calculateBMI(Long userId) {
        try {
            Optional<UserHealthProfile> profileOpt = healthProfileRepository.findByUserId(userId);
            
            if (!profileOpt.isPresent()) {
                return ApiResponse.error(404, "健康档案不存在");
            }
            
            UserHealthProfile profile = profileOpt.get();
            Float height = profile.getHeight();
            Float weight = profile.getWeight();
            
            if (height == null || weight == null || height <= 0) {
                return ApiResponse.error("身高或体重数据无效");
            }
            
            // BMI = 体重(kg) / 身高²(m²)
            double heightInMeters = height / 100.0;
            double bmi = weight / (heightInMeters * heightInMeters);
            bmi = Math.round(bmi * 100.0) / 100.0; // 保留两位小数
            
            return ApiResponse.success("BMI计算成功", bmi);
            
        } catch (Exception e) {
            logger.error("计算BMI失败", e);
            return ApiResponse.error("计算BMI失败: " + e.getMessage());
        }
    }

    /**
     * 转换实体为DTO
     */
    private UserHealthProfileDto convertToDto(UserHealthProfile profile) {
        UserHealthProfileDto dto = new UserHealthProfileDto();
        dto.setProfileId(profile.getProfileId());
        dto.setUserId(profile.getUserId());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setHeight(profile.getHeight());
        dto.setWeight(profile.getWeight());
        dto.setHealthConditions(profile.getHealthConditions());
        dto.setAllergies(profile.getAllergies());
        dto.setMedications(profile.getMedications());
        dto.setActivityLevel(profile.getActivityLevel());
        dto.setSugarLimit(profile.getSugarLimit());
        dto.setCalorieLimit(profile.getCalorieLimit());
        dto.setWaterGoal(profile.getWaterGoal());
        
        if (profile.getCreatedAt() != null) {
            dto.setCreatedAt(profile.getCreatedAt().format(formatter));
        }
        if (profile.getUpdatedAt() != null) {
            dto.setUpdatedAt(profile.getUpdatedAt().format(formatter));
        }
        
        return dto;
    }
}

