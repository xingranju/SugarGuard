package com.example.usermanagement.entity;

import javax.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * 用户健康档案实体类
 */
@Entity
@Table(name = "user_health_profile")
@EntityListeners(AuditingEntityListener.class)
public class UserHealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "gender", nullable = false, length = 10)
    private String gender; // male, female, other

    @Column(name = "height", nullable = false)
    private Float height; // 身高(cm)

    @Column(name = "weight", nullable = false)
    private Float weight; // 体重(kg)

    @Column(name = "health_conditions", columnDefinition = "TEXT")
    private String healthConditions; // 健康状况（JSON格式）

    @Column(name = "allergies", length = 500)
    private String allergies; // 过敏史

    @Column(name = "medications", length = 500)
    private String medications; // 当前用药

    @Column(name = "activity_level", length = 20)
    private String activityLevel = "moderate"; // sedentary, light, moderate, active, very_active

    @Column(name = "sugar_limit")
    private Float sugarLimit = 50.0f; // 每日糖分限制(g)

    @Column(name = "calorie_limit")
    private Float calorieLimit = 2000.0f; // 每日热量限制(kcal)

    @Column(name = "water_goal")
    private Float waterGoal = 2000.0f; // 每日饮水目标(ml)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 默认构造函数
    public UserHealthProfile() {
    }

    // Getter 和 Setter 方法
    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public String getHealthConditions() {
        return healthConditions;
    }

    public void setHealthConditions(String healthConditions) {
        this.healthConditions = healthConditions;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Float getSugarLimit() {
        return sugarLimit;
    }

    public void setSugarLimit(Float sugarLimit) {
        this.sugarLimit = sugarLimit;
    }

    public Float getCalorieLimit() {
        return calorieLimit;
    }

    public void setCalorieLimit(Float calorieLimit) {
        this.calorieLimit = calorieLimit;
    }

    public Float getWaterGoal() {
        return waterGoal;
    }

    public void setWaterGoal(Float waterGoal) {
        this.waterGoal = waterGoal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

